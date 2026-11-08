"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  useSyncExternalStore,
  type ReactNode,
} from "react";
import {
  productPresentation,
  type Address,
  type CartItem,
  type Product,
  type ShopUser,
  type Order,
  type PaymentMethod,
} from "@/lib/shop-data";

type PersistedData = {
  users: ShopUser[];
  sessionId: string | null;
  carts: Record<string, CartItem[]>;
  orders: Order[];
};

type PersistedShopUser = Omit<ShopUser, "role"> & {
  role?: ShopUser["role"];
  passwordHash?: string;
  passwordSalt?: string;
};

type Registration = Pick<ShopUser, "name" | "email" | "phone"> & { password: string };
type ShopContextValue = {
  ready: boolean;
  currentUser: ShopUser | null;
  products: Product[];
  catalogLoading: boolean;
  catalogError: string;
  refreshCatalog: () => Promise<void>;
  cart: CartItem[];
  orders: Order[];
  storageError: string;
  register: (input: Registration) => Promise<void>;
  login: (email: string, password: string) => Promise<ShopUser>;
  resetPassword: (email: string, password: string) => Promise<void>;
  logout: () => void;
  addToCart: (productId: string, quantity: number) => void;
  setCartQuantity: (productId: string, quantity: number) => void;
  removeFromCart: (productId: string) => void;
  saveProfile: (name: string, phone: string) => void;
  saveAddress: (address: Address) => Promise<void>;
  createOrder: (payment: PaymentMethod, changeFor: number | null) => Promise<Order>;
  getOrder: (id: string) => Order | undefined;
};

const STORAGE_KEY = "doce-miolo-store-v1";
const ShopContext = createContext<ShopContextValue | null>(null);
const STORE_EVENT = "doce-miolo-store-change";

const emptyData: PersistedData = { users: [], sessionId: null, carts: {}, orders: [] };
const paymentMethods: PaymentMethod[] = ["Dinheiro", "Cartão de débito", "Cartão de crédito"];
type BackendRequestOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  token?: string;
  body?: unknown;
};

async function backendRequest(path: string, options: BackendRequestOptions = {}): Promise<unknown> {
  const headers = new Headers();
  if (options.token) headers.set("Authorization", `Bearer ${options.token}`);
  if (options.body !== undefined) headers.set("Content-Type", "application/json");

  const response = await fetch(path, {
    method: options.method ?? "GET",
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
    cache: "no-store",
  });
  const responseText = await response.text();
  let payload: unknown = null;
  if (responseText) {
    try {
      payload = JSON.parse(responseText);
    } catch {
      payload = responseText;
    }
  }

  if (!response.ok) {
    const record = isRecord(payload) ? payload : null;
    const message = record && typeof record.detail === "string"
      ? record.detail
      : record && typeof record.message === "string"
        ? record.message
        : typeof payload === "string" && payload
          ? payload
          : `A API recusou a solicitação (HTTP ${response.status}).`;
    throw new Error(message);
  }

  return payload;
}

function mapBackendAddress(value: unknown) {
  if (!isRecord(value) ||
    typeof value.idEndereco !== "string" ||
    typeof value.destinatario !== "string" ||
    typeof value.cep !== "string" ||
    typeof value.logradouro !== "string" ||
    typeof value.numero !== "string" ||
    typeof value.bairro !== "string" ||
    typeof value.cidade !== "string" ||
    typeof value.uf !== "string") {
    throw new Error("A API retornou um endereço em formato inválido.");
  }
  return {
    id: value.idEndereco,
    address: {
      recipient: value.destinatario,
      cep: value.cep,
      street: value.logradouro,
      number: value.numero,
      complement: typeof value.complemento === "string" ? value.complemento : "",
      district: value.bairro,
      city: value.cidade,
      state: value.uf,
      notes: typeof value.referencia === "string" ? value.referencia : "",
    } satisfies Address,
    principal: value.principal === true,
  };
}

function normalizeProductName(value: string) {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").trim().toLocaleLowerCase("pt-BR");
}

function mapProductCatalog(payload: unknown): Product[] {
  const entries = Array.isArray(payload)
    ? payload
    : isRecord(payload) && Array.isArray(payload.content)
      ? payload.content
      : null;
  if (!entries) throw new Error("A API retornou o catálogo de produtos em formato inválido.");

  return entries.map((entry, index) => {
    if (!isRecord(entry) ||
      typeof entry.idProduto !== "string" ||
      typeof entry.nome !== "string" ||
      (typeof entry.preco !== "number" && typeof entry.preco !== "string") ||
      typeof entry.disponivel !== "boolean") {
      throw new Error(`O produto ${index + 1} retornado pela API possui dados inválidos.`);
    }

    const price = Number(entry.preco);
    if (!Number.isFinite(price) || price < 0) {
      throw new Error(`O produto ${entry.nome} possui um preço inválido.`);
    }

    const presentation = productPresentation.find(
      (item) => normalizeProductName(item.name) === normalizeProductName(entry.nome as string),
    );
    return {
      id: entry.idProduto,
      name: entry.nome,
      description: typeof entry.descricao === "string"
        ? entry.descricao
        : presentation?.description ?? "",
      price,
      category: presentation?.category ?? "Especiais",
      image: typeof entry.imagemUrl === "string" && entry.imagemUrl
        ? entry.imagemUrl
        : presentation?.image ?? "photo-1576618148400-f54bed99fcfd",
      available: entry.disponivel,
      ...(presentation?.tag ? { tag: presentation.tag } : {}),
    };
  });
}

function normalizeCartIds(items: CartItem[], catalog: Product[]) {
  const legacyProducts = new Map(
    productPresentation.map((product) => [
      normalizeProductName(product.name).replace(/[^a-z0-9]+/g, "-"),
      product.name,
    ]),
  );
  return items.map((item) => {
    const legacyName = legacyProducts.get(item.productId);
    const backendProduct = legacyName && catalog.find(
      (product) => normalizeProductName(product.name) === normalizeProductName(legacyName),
    );
    return backendProduct ? { ...item, productId: backendProduct.id } : item;
  });
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isAddress(value: unknown): value is Address {
  return isRecord(value) &&
    typeof value.recipient === "string" &&
    typeof value.cep === "string" &&
    typeof value.street === "string" &&
    typeof value.number === "string" &&
    typeof value.complement === "string" &&
    typeof value.district === "string" &&
    typeof value.city === "string" &&
    typeof value.state === "string" &&
    (value.notes === undefined || typeof value.notes === "string");
}

function isShopUser(value: unknown): value is PersistedShopUser {
  return isRecord(value) &&
    typeof value.id === "string" &&
    typeof value.name === "string" &&
    typeof value.email === "string" &&
    typeof value.phone === "string" &&
    (value.role === undefined || value.role === "CUSTOMER" || value.role === "ADMIN") &&
    (value.passwordSalt === undefined || typeof value.passwordSalt === "string") &&
    (value.passwordHash === undefined || typeof value.passwordHash === "string") &&
    (value.addressId === undefined || value.addressId === null || typeof value.addressId === "string") &&
    (value.token === undefined || typeof value.token === "string") &&
    (value.address === null || isAddress(value.address));
}

function isCartItem(value: unknown): value is CartItem {
  return isRecord(value) &&
    typeof value.productId === "string" &&
    Number.isInteger(value.quantity) &&
    Number(value.quantity) > 0;
}

function isOrder(value: unknown): value is Order {
  return isRecord(value) &&
    typeof value.id === "string" &&
    typeof value.userId === "string" &&
    Array.isArray(value.items) &&
    value.items.every((item) =>
      isRecord(item) &&
      typeof item.productId === "string" &&
      typeof item.name === "string" &&
      typeof item.image === "string" &&
      typeof item.price === "number" &&
      Number.isInteger(item.quantity) &&
      Number(item.quantity) > 0,
    ) &&
    isAddress(value.address) &&
    paymentMethods.some((method) => method === value.payment) &&
    (value.changeFor === null || typeof value.changeFor === "number") &&
    typeof value.subtotal === "number" &&
    typeof value.delivery === "number" &&
    typeof value.total === "number" &&
    typeof value.createdAt === "string";
}

function subscribeToStore(callback: () => void) {
  window.addEventListener(STORE_EVENT, callback);
  window.addEventListener("storage", callback);
  return () => {
    window.removeEventListener(STORE_EVENT, callback);
    window.removeEventListener("storage", callback);
  };
}

function getStoreSnapshot() {
  return localStorage.getItem(STORAGE_KEY);
}

function getServerSnapshot() {
  return null;
}

function getClientReadySnapshot() {
  return true;
}

function getServerReadySnapshot() {
  return false;
}

function parsePersistedData(value: string): PersistedData {
  const parsed: unknown = JSON.parse(value);
  if (!isRecord(parsed) || !Array.isArray(parsed.users) || !parsed.users.every(isShopUser) ||
    !isRecord(parsed.carts) || !Array.isArray(parsed.orders) || !parsed.orders.every(isOrder) ||
    !(parsed.sessionId === null || typeof parsed.sessionId === "string")) {
    throw new Error("Os dados locais da loja estão inválidos. Limpe os dados deste site para começar novamente.");
  }
  const carts: Record<string, CartItem[]> = {};
  for (const [userId, items] of Object.entries(parsed.carts)) {
    if (!Array.isArray(items) || !items.every(isCartItem)) {
      throw new Error("Os dados locais da loja estão inválidos. Limpe os dados deste site para começar novamente.");
    }
    carts[userId] = items;
  }
  return {
    users: parsed.users.map((user) => {
      const account: PersistedShopUser = { ...user };
      delete account.passwordHash;
      delete account.passwordSalt;
      return { ...account, role: account.role ?? "CUSTOMER" };
    }),
    sessionId: parsed.sessionId,
    carts,
    orders: parsed.orders,
  };
}

export function ShopProvider({ children }: { children: ReactNode }) {
  const snapshot = useSyncExternalStore(subscribeToStore, getStoreSnapshot, getServerSnapshot);
  const ready = useSyncExternalStore(subscribeToStore, getClientReadySnapshot, getServerReadySnapshot);
  const [writeError, setWriteError] = useState("");
  const [products, setProducts] = useState<Product[]>([]);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogError, setCatalogError] = useState("");
  const { data, parseError } = useMemo(() => {
    if (!snapshot) return { data: emptyData, parseError: "" };
    try {
      return { data: parsePersistedData(snapshot), parseError: "" };
    } catch (error) {
      return {
        data: emptyData,
        parseError: error instanceof Error ? error.message : "Não foi possível ler os dados salvos neste navegador.",
      };
    }
  }, [snapshot]);

  const commit = useCallback((next: PersistedData) => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      setWriteError("");
      window.dispatchEvent(new Event(STORE_EVENT));
    } catch (error) {
      const message = error instanceof Error ? error.message : "Não foi possível salvar os dados no navegador.";
      setWriteError(`Não foi possível salvar sua alteração: ${message}`);
      throw new Error(`Não foi possível salvar sua alteração: ${message}`);
    }
  }, []);

  const loadCatalog = useCallback(async () => {
    const catalog = mapProductCatalog(await backendRequest("/api/produtos"));
    setProducts(catalog);
    return catalog;
  }, []);

  const refreshCatalog = useCallback(async () => {
    setCatalogLoading(true);
    setCatalogError("");
    try {
      await loadCatalog();
    } catch (error) {
      setCatalogError(error instanceof Error ? error.message : "Não foi possível carregar o cardápio.");
    } finally {
      setCatalogLoading(false);
    }
  }, [loadCatalog]);

  useEffect(() => {
    let active = true;
    void backendRequest("/api/produtos")
      .then(mapProductCatalog)
      .then((catalog) => {
        if (active) setProducts(catalog);
      })
      .catch((error: unknown) => {
        if (active) setCatalogError(error instanceof Error ? error.message : "Não foi possível carregar o cardápio.");
      })
      .finally(() => {
        if (active) setCatalogLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const storageError = writeError || parseError;
  const currentUser = data.users.find((user) => user.id === data.sessionId) ?? null;
  const cart = useMemo(
    () => currentUser ? normalizeCartIds(data.carts[currentUser.id] ?? [], products) : [],
    [currentUser, data.carts, products],
  );
  const orders = useMemo(() => currentUser ? data.orders.filter((order) => order.userId === currentUser.id) : [], [currentUser, data.orders]);

  const authenticate = useCallback(
    async (email: string, password: string) => {
      const payload = await backendRequest("/api/auth/login", {
        method: "POST",
        body: { email: email.trim().toLowerCase(), senha: password },
      });
      const token = isRecord(payload) ? payload.token : null;
      const profile = isRecord(payload) ? payload.usuario : null;
      if (typeof token !== "string" || !isRecord(profile) ||
        typeof profile.idUsuario !== "string" ||
        typeof profile.nome !== "string" ||
        typeof profile.email !== "string" ||
        typeof profile.perfil !== "string") {
        throw new Error("A API retornou dados de autenticação inválidos.");
      }

      const role = profile.perfil === "ADMIN" ? "ADMIN" : "CUSTOMER";
      let address: Address | null = null;
      let addressId: string | null = null;
      if (role === "CUSTOMER") {
        const addressesPayload = await backendRequest(
          `/api/enderecos/${encodeURIComponent(profile.idUsuario)}`,
          { token },
        );
        if (!Array.isArray(addressesPayload)) {
          throw new Error("A API retornou a lista de endereços em formato inválido.");
        }
        const addresses = addressesPayload.map(mapBackendAddress);
        const savedAddress = addresses.find((item) => item.principal) ?? addresses[0];
        if (savedAddress) {
          address = savedAddress.address;
          addressId = savedAddress.id;
        }
      }

      const user: ShopUser = {
        id: profile.idUsuario,
        name: profile.nome,
        email: profile.email,
        phone: typeof profile.telefone === "string" ? profile.telefone : "",
        role,
        address,
        addressId,
        token,
      };
      const users = data.users.filter((candidate) =>
        candidate.id !== user.id && candidate.email.toLowerCase() !== user.email.toLowerCase(),
      );
      commit({
        ...data,
        users: [...users, user],
        sessionId: user.id,
        carts: { ...data.carts, [user.id]: data.carts[user.id] ?? [] },
      });
      return user;
    },
    [commit, data],
  );

  const register = useCallback(
    async ({ name, email, phone, password }: Registration) => {
      await backendRequest("/api/auth/register", {
        method: "POST",
        body: {
          nome: name.trim(),
          email: email.trim().toLowerCase(),
          senha: password,
          telefone: phone.trim(),
        },
      });
      await authenticate(email, password);
    },
    [authenticate],
  );

  const login = authenticate;

  const resetPassword = useCallback(
    async () => {
      throw new Error("A recuperação de senha ainda não está disponível na API.");
    },
    [],
  );

  const logout = useCallback(() => {
    commit({
      ...data,
      users: data.users.map((user) =>
        user.id === currentUser?.id ? { ...user, token: undefined } : user,
      ),
      sessionId: null,
    });
  }, [commit, currentUser, data]);

  const addToCart = useCallback(
    (productId: string, quantity: number) => {
      if (!currentUser) throw new Error("Entre na sua conta para adicionar produtos ao carrinho.");
      const product = products.find((candidate) => candidate.id === productId);
      if (!product || !product.available) {
        throw new Error("Este produto não está disponível no momento.");
      }
      if (!Number.isInteger(quantity) || quantity < 1) {
        throw new Error("Selecione uma quantidade válida.");
      }
      const previous = cart;
      const found = previous.find((item) => item.productId === productId);
      const next = found
        ? previous.map((item) => item.productId === productId ? { ...item, quantity: item.quantity + quantity } : item)
        : [...previous, { productId, quantity }];
      commit({ ...data, carts: { ...data.carts, [currentUser.id]: next } });
    },
    [cart, commit, currentUser, data, products],
  );

  const setCartQuantity = useCallback(
    (productId: string, quantity: number) => {
      if (!currentUser) throw new Error("Entre na sua conta para alterar o carrinho.");
      const previous = cart;
      const next = quantity <= 0
        ? previous.filter((item) => item.productId !== productId)
        : previous.map((item) => item.productId === productId ? { ...item, quantity } : item);
      commit({ ...data, carts: { ...data.carts, [currentUser.id]: next } });
    },
    [cart, commit, currentUser, data],
  );

  const removeFromCart = useCallback(
    (productId: string) => setCartQuantity(productId, 0),
    [setCartQuantity],
  );

  const saveProfile = useCallback(
    (name: string, phone: string) => {
      if (!currentUser) throw new Error("Entre na sua conta para editar seus dados.");
      commit({
        ...data,
        users: data.users.map((user) =>
          user.id === currentUser.id ? { ...user, name: name.trim(), phone: phone.trim() } : user,
        ),
      });
    },
    [commit, currentUser, data],
  );

  const saveAddress = useCallback(
    async (address: Address) => {
      if (!currentUser) throw new Error("Entre na sua conta para salvar o endereço.");
      if (!currentUser.token) throw new Error("Entre novamente para salvar seu endereço.");
      const savedAddress = mapBackendAddress(await backendRequest("/api/enderecos", {
        method: "POST",
        token: currentUser.token,
        body: {
          identificacao: "Entrega",
          destinatario: address.recipient,
          cep: address.cep,
          logradouro: address.street,
          numero: address.number,
          complemento: address.complement,
          bairro: address.district,
          cidade: address.city,
          uf: address.state.toUpperCase(),
          referencia: address.notes ?? "",
          principal: true,
        },
      }));
      commit({
        ...data,
        users: data.users.map((user) => user.id === currentUser.id
          ? { ...user, address: savedAddress.address, addressId: savedAddress.id }
          : user),
      });
    },
    [commit, currentUser, data],
  );

  const createOrder = useCallback(
    async (payment: PaymentMethod, changeFor: number | null) => {
      if (!currentUser) throw new Error("Entre na sua conta antes de confirmar o pedido.");
      if (!currentUser.address) throw new Error("Cadastre um endereço de entrega antes de continuar.");
      if (!currentUser.token) throw new Error("Sua sessão expirou. Entre novamente antes de confirmar o pedido.");
      if (!currentUser.addressId) throw new Error("Salve novamente o endereço de entrega antes de confirmar o pedido.");
      const items = cart;
      if (items.length === 0) throw new Error("Seu carrinho está vazio.");

      const catalog = mapProductCatalog(await backendRequest("/api/produtos"));
      const syncedItems = items.map((item) => {
        const product = products.find((candidate) => candidate.id === item.productId);
        if (!product) throw new Error("Um produto do carrinho não está mais disponível.");
        const backendProduct = catalog.find((candidate) => candidate.id === item.productId);
        if (!backendProduct?.available) {
          throw new Error(`O produto ${product.name} não está disponível no catálogo do servidor.`);
        }
        return { localProduct: product, backendProduct };
      });

      const backendCart = await backendRequest(
        `/api/carrinho/${encodeURIComponent(currentUser.id)}`,
        { token: currentUser.token },
      );
      if (!isRecord(backendCart) || !Array.isArray(backendCart.itens)) {
        throw new Error("A API retornou o carrinho em formato inválido.");
      }
      const desiredProductIds = new Set(syncedItems.map((item) => item.backendProduct.id));
      for (const cartItem of backendCart.itens) {
        if (isRecord(cartItem) && typeof cartItem.idProduto === "string" &&
          !desiredProductIds.has(cartItem.idProduto)) {
          await backendRequest(`/api/carrinho/itens/${encodeURIComponent(cartItem.idProduto)}`, {
            method: "DELETE",
            token: currentUser.token,
          });
        }
      }
      for (const [index, item] of syncedItems.entries()) {
        await backendRequest("/api/carrinho/itens", {
          method: "PUT",
          token: currentUser.token,
          body: {
            idProduto: item.backendProduct.id,
            quantidade: items[index].quantity,
          },
        });
      }

      const orderPayload = await backendRequest("/api/pedidos", {
        method: "POST",
        token: currentUser.token,
        body: {
          idEndereco: currentUser.addressId,
          codigoCupom: "",
        },
      });
      if (!isRecord(orderPayload) || typeof orderPayload.idPedido !== "string" ||
        typeof orderPayload.dataPedido !== "string" ||
        typeof orderPayload.subtotal !== "number" ||
        typeof orderPayload.valorFrete !== "number" ||
        typeof orderPayload.valorTotal !== "number" ||
        !Array.isArray(orderPayload.itens)) {
        throw new Error("A API retornou o pedido em formato inválido.");
      }
      const orderItems = orderPayload.itens.map((item) => {
        if (!isRecord(item) || typeof item.idProduto !== "string" ||
          typeof item.nomeProduto !== "string" ||
          typeof item.quantidade !== "number" ||
          typeof item.precoUnitario !== "number") {
          throw new Error("A API retornou um item do pedido em formato inválido.");
        }
        const matchedProduct = syncedItems.find((synced) => synced.backendProduct.id === item.idProduto);
        if (!matchedProduct) throw new Error("A API retornou um produto inesperado no pedido.");
        return {
          productId: matchedProduct.localProduct.id,
          name: item.nomeProduto,
          image: matchedProduct.localProduct.image,
          price: item.precoUnitario,
          quantity: item.quantidade,
        };
      });
      const order: Order = {
        id: orderPayload.idPedido,
        userId: currentUser.id,
        items: orderItems,
        address: currentUser.address,
        payment,
        changeFor,
        subtotal: orderPayload.subtotal,
        delivery: orderPayload.valorFrete,
        total: orderPayload.valorTotal,
        createdAt: orderPayload.dataPedido,
      };
      commit({
        ...data,
        orders: [order, ...data.orders],
        carts: { ...data.carts, [currentUser.id]: [] },
      });
      return order;
    },
    [cart, commit, currentUser, data, products],
  );

  const getOrder = useCallback((id: string) => {
    return currentUser ? data.orders.find((order) => order.id === id && order.userId === currentUser.id) : undefined;
  }, [currentUser, data.orders]);

  const value = useMemo<ShopContextValue>(() => ({
    ready,
    currentUser,
    products,
    catalogLoading,
    catalogError,
    refreshCatalog,
    cart,
    orders,
    storageError,
    register,
    login,
    resetPassword,
    logout,
    addToCart,
    setCartQuantity,
    removeFromCart,
    saveProfile,
    saveAddress,
    createOrder,
    getOrder,
  }), [
    ready, currentUser, products, catalogLoading, catalogError, refreshCatalog, cart, orders, storageError, register, login, resetPassword, logout,
    addToCart, setCartQuantity, removeFromCart, saveProfile, saveAddress, createOrder, getOrder,
  ]);

  return <ShopContext.Provider value={value}>{children}</ShopContext.Provider>;
}

export function useShop() {
  const value = useContext(ShopContext);
  if (!value) throw new Error("useShop deve ser usado dentro de ShopProvider.");
  return value;
}
