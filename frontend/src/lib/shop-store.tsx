"use client";

import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  useSyncExternalStore,
  type ReactNode,
} from "react";
import {
  calculateDelivery,
  products,
  type Address,
  type CartItem,
  type DemoUser,
  type Order,
  type PaymentMethod,
} from "@/lib/shop-data";

type PersistedData = {
  users: DemoUser[];
  sessionId: string | null;
  carts: Record<string, CartItem[]>;
  orders: Order[];
};

type Registration = Pick<DemoUser, "name" | "email" | "phone"> & { password: string };
type ShopContextValue = {
  ready: boolean;
  currentUser: DemoUser | null;
  cart: CartItem[];
  orders: Order[];
  storageError: string;
  register: (input: Registration) => Promise<void>;
  login: (email: string, password: string) => Promise<DemoUser>;
  resetPassword: (email: string, password: string) => Promise<void>;
  logout: () => void;
  addToCart: (productId: string, quantity: number) => void;
  setCartQuantity: (productId: string, quantity: number) => void;
  removeFromCart: (productId: string) => void;
  saveProfile: (name: string, phone: string) => void;
  saveAddress: (address: Address) => void;
  createOrder: (payment: PaymentMethod, changeFor: number | null) => Order;
  getOrder: (id: string) => Order | undefined;
};

const STORAGE_KEY = "doce-miolo-store-v1";
export const DEMO_ADMIN_CREDENTIALS = {
  email: "admin@docemiolo.com.br",
  password: "DoceMioloAdmin2026!",
} as const;
export const DEMO_CUSTOMER_CREDENTIALS = {
  email: "cliente@docemiolo.com.br",
  password: "DoceMioloCliente2026!",
} as const;
const ShopContext = createContext<ShopContextValue | null>(null);
const STORE_EVENT = "doce-miolo-store-change";

const emptyData: PersistedData = { users: [], sessionId: null, carts: {}, orders: [] };
const paymentMethods: PaymentMethod[] = ["Dinheiro", "Cartão de débito", "Cartão de crédito"];

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

function isDemoUser(value: unknown): value is DemoUser {
  return isRecord(value) &&
    typeof value.id === "string" &&
    typeof value.name === "string" &&
    typeof value.email === "string" &&
    typeof value.phone === "string" &&
    (value.role === undefined || value.role === "CUSTOMER" || value.role === "ADMIN") &&
    typeof value.passwordSalt === "string" &&
    typeof value.passwordHash === "string" &&
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

function encodeBytes(bytes: Uint8Array) {
  return btoa(Array.from(bytes, (byte) => String.fromCharCode(byte)).join(""));
}

async function hashPassword(password: string, salt?: string) {
  if (!globalThis.crypto?.subtle) {
    throw new Error("Este navegador não oferece armazenamento seguro para a demonstração.");
  }
  const saltBytes = salt
    ? Uint8Array.from(atob(salt), (character) => character.charCodeAt(0))
    : crypto.getRandomValues(new Uint8Array(16));
  const material = await crypto.subtle.importKey("raw", new TextEncoder().encode(password), "PBKDF2", false, ["deriveBits"]);
  const digest = await crypto.subtle.deriveBits(
    { name: "PBKDF2", salt: saltBytes, iterations: 120_000, hash: "SHA-256" },
    material,
    256,
  );
  return { salt: salt ?? encodeBytes(saltBytes), hash: encodeBytes(new Uint8Array(digest)) };
}

function parsePersistedData(value: string): PersistedData {
  const parsed: unknown = JSON.parse(value);
  if (!isRecord(parsed) || !Array.isArray(parsed.users) || !parsed.users.every(isDemoUser) ||
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
    users: parsed.users.map((user) => ({ ...user, role: user.role ?? "CUSTOMER" })),
    sessionId: parsed.sessionId,
    carts,
    orders: parsed.orders,
  };
}

export function ShopProvider({ children }: { children: ReactNode }) {
  const snapshot = useSyncExternalStore(subscribeToStore, getStoreSnapshot, getServerSnapshot);
  const ready = useSyncExternalStore(subscribeToStore, getClientReadySnapshot, getServerReadySnapshot);
  const [writeError, setWriteError] = useState("");
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

  const storageError = writeError || parseError;
  const currentUser = data.users.find((user) => user.id === data.sessionId) ?? null;
  const cart = useMemo(() => currentUser ? data.carts[currentUser.id] ?? [] : [], [currentUser, data.carts]);
  const orders = useMemo(() => currentUser ? data.orders.filter((order) => order.userId === currentUser.id) : [], [currentUser, data.orders]);

  const register = useCallback(
    async ({ name, email, phone, password }: Registration) => {
      const normalizedEmail = email.trim().toLowerCase();
      if (normalizedEmail === DEMO_ADMIN_CREDENTIALS.email || normalizedEmail === DEMO_CUSTOMER_CREDENTIALS.email) {
        throw new Error("Este e-mail é reservado para uma conta demonstrativa.");
      }
      if (data.users.some((user) => user.email.toLowerCase() === normalizedEmail)) {
        throw new Error("Já existe uma conta cadastrada com este e-mail.");
      }
      const { salt, hash } = await hashPassword(password);
      const user: DemoUser = {
        id: crypto.randomUUID(),
        name: name.trim(),
        email: normalizedEmail,
        phone: phone.trim(),
        role: "CUSTOMER",
        passwordSalt: salt,
        passwordHash: hash,
        address: null,
      };
      commit({
        ...data,
        users: [...data.users, user],
        sessionId: user.id,
        carts: { ...data.carts, [user.id]: [] },
      });
    },
    [commit, data],
  );

  const login = useCallback(
    async (email: string, password: string) => {
      const normalizedEmail = email.trim().toLowerCase();
      let user = data.users.find((candidate) => candidate.email === normalizedEmail);
      if (!user && normalizedEmail === DEMO_ADMIN_CREDENTIALS.email && password === DEMO_ADMIN_CREDENTIALS.password) {
        const { salt, hash } = await hashPassword(password);
        user = {
          id: "demo-admin",
          name: "Administração",
          email: DEMO_ADMIN_CREDENTIALS.email,
          phone: "",
          role: "ADMIN",
          passwordSalt: salt,
          passwordHash: hash,
          address: null,
        };
        commit({ ...data, users: [...data.users, user], sessionId: user.id });
        return user;
      }
      if (!user && normalizedEmail === DEMO_CUSTOMER_CREDENTIALS.email && password === DEMO_CUSTOMER_CREDENTIALS.password) {
        const { salt, hash } = await hashPassword(password);
        user = {
          id: "demo-customer",
          name: "Cliente Demonstração",
          email: DEMO_CUSTOMER_CREDENTIALS.email,
          phone: "",
          role: "CUSTOMER",
          passwordSalt: salt,
          passwordHash: hash,
          address: null,
        };
        commit({
          ...data,
          users: [...data.users, user],
          sessionId: user.id,
          carts: { ...data.carts, [user.id]: data.carts[user.id] ?? [] },
        });
        return user;
      }
      if (!user) throw new Error("E-mail ou senha incorretos.");
      const { hash } = await hashPassword(password, user.passwordSalt);
      if (hash !== user.passwordHash) throw new Error("E-mail ou senha incorretos.");
      commit({ ...data, sessionId: user.id });
      return user;
    },
    [commit, data],
  );

  const resetPassword = useCallback(
    async (email: string, password: string) => {
      const user = data.users.find((candidate) => candidate.email === email.trim().toLowerCase());
      if (!user) throw new Error("Não encontramos uma conta com esse e-mail.");
      const { salt, hash } = await hashPassword(password);
      commit({
        ...data,
        users: data.users.map((candidate) =>
          candidate.id === user.id ? { ...candidate, passwordSalt: salt, passwordHash: hash } : candidate,
        ),
      });
    },
    [commit, data],
  );

  const logout = useCallback(() => commit({ ...data, sessionId: null }), [commit, data]);

  const addToCart = useCallback(
    (productId: string, quantity: number) => {
      if (!currentUser) throw new Error("Entre na sua conta para adicionar produtos ao carrinho.");
      if (!products.some((product) => product.id === productId) || !Number.isInteger(quantity) || quantity < 1) {
        throw new Error("Selecione uma quantidade válida.");
      }
      const previous = data.carts[currentUser.id] ?? [];
      const found = previous.find((item) => item.productId === productId);
      const next = found
        ? previous.map((item) => item.productId === productId ? { ...item, quantity: item.quantity + quantity } : item)
        : [...previous, { productId, quantity }];
      commit({ ...data, carts: { ...data.carts, [currentUser.id]: next } });
    },
    [commit, currentUser, data],
  );

  const setCartQuantity = useCallback(
    (productId: string, quantity: number) => {
      if (!currentUser) throw new Error("Entre na sua conta para alterar o carrinho.");
      const previous = data.carts[currentUser.id] ?? [];
      const next = quantity <= 0
        ? previous.filter((item) => item.productId !== productId)
        : previous.map((item) => item.productId === productId ? { ...item, quantity } : item);
      commit({ ...data, carts: { ...data.carts, [currentUser.id]: next } });
    },
    [commit, currentUser, data],
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
    (address: Address) => {
      if (!currentUser) throw new Error("Entre na sua conta para salvar o endereço.");
      commit({
        ...data,
        users: data.users.map((user) => user.id === currentUser.id ? { ...user, address } : user),
      });
    },
    [commit, currentUser, data],
  );

  const createOrder = useCallback(
    (payment: PaymentMethod, changeFor: number | null) => {
      if (!currentUser) throw new Error("Entre na sua conta antes de confirmar o pedido.");
      if (!currentUser.address) throw new Error("Cadastre um endereço de entrega antes de continuar.");
      const items = data.carts[currentUser.id] ?? [];
      if (items.length === 0) throw new Error("Seu carrinho está vazio.");
      const orderItems = items.map((item) => {
        const product = products.find((candidate) => candidate.id === item.productId);
        if (!product) throw new Error("Um produto do carrinho não está mais disponível.");
        return {
          productId: product.id,
          name: product.name,
          image: product.image,
          price: product.price,
          quantity: item.quantity,
        };
      });
      const subtotal = orderItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
      const delivery = calculateDelivery(subtotal);
      const order: Order = {
        id: `DM-${Date.now().toString(36).toUpperCase()}-${crypto.randomUUID().slice(0, 5).toUpperCase()}`,
        userId: currentUser.id,
        items: orderItems,
        address: currentUser.address,
        payment,
        changeFor,
        subtotal,
        delivery,
        total: subtotal + delivery,
        createdAt: new Date().toISOString(),
      };
      commit({
        ...data,
        orders: [order, ...data.orders],
        carts: { ...data.carts, [currentUser.id]: [] },
      });
      return order;
    },
    [commit, currentUser, data],
  );

  const getOrder = useCallback((id: string) => {
    return currentUser ? data.orders.find((order) => order.id === id && order.userId === currentUser.id) : undefined;
  }, [currentUser, data.orders]);

  const value = useMemo<ShopContextValue>(() => ({
    ready,
    currentUser,
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
    ready, currentUser, cart, orders, storageError, register, login, resetPassword, logout,
    addToCart, setCartQuantity, removeFromCart, saveProfile, saveAddress, createOrder, getOrder,
  ]);

  return <ShopContext.Provider value={value}>{children}</ShopContext.Provider>;
}

export function useShop() {
  const value = useContext(ShopContext);
  if (!value) throw new Error("useShop deve ser usado dentro de ShopProvider.");
  return value;
}
