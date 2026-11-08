export type Category =
  | "Chocolate"
  | "Baunilha"
  | "Morango"
  | "Red velvet"
  | "Especiais";

export type Product = {
  id: string;
  name: string;
  description: string;
  price: number;
  category: Category;
  image: string;
  available: boolean;
  tag?: string;
};

export type Address = {
  recipient: string;
  cep: string;
  street: string;
  number: string;
  complement: string;
  district: string;
  city: string;
  state: string;
  notes?: string;
};

export type ShopUser = {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: "CUSTOMER" | "ADMIN";
  address: Address | null;
  addressId?: string | null;
  token?: string;
};

export type CartItem = { productId: string; quantity: number };
export type PaymentMethod = "Dinheiro" | "Cartão de débito" | "Cartão de crédito";

export type Order = {
  id: string;
  userId: string;
  items: { productId: string; name: string; image: string; price: number; quantity: number }[];
  address: Address;
  payment: PaymentMethod;
  changeFor: number | null;
  subtotal: number;
  delivery: number;
  total: number;
  createdAt: string;
};

export const DELIVERY_FEE = 8.9;
export const FREE_DELIVERY_FROM = 120;

export const productPresentation: Omit<Product, "id" | "price" | "available">[] = [
  {
    name: "Brigadeiro Supremo",
    description: "Chocolate intenso, brigadeiro cremoso e granulado belga.",
    category: "Chocolate",
    image: "photo-1576618148400-f54bed99fcfd",
    tag: "Mais amado",
  },
  {
    name: "Nuvem de Baunilha",
    description: "Massa fofinha de baunilha com um delicado buttercream.",
    category: "Baunilha",
    image: "photo-1486427944299-d1955d23e34d",
  },
  {
    name: "Morango Encantado",
    description: "Recheio de morangos fresquinhos e cobertura rosada.",
    category: "Morango",
    image: "photo-1614707267537-b85aaf00c4b7",
    tag: "Feito com fruta",
  },
  {
    name: "Red Velvet",
    description: "Massa aveludada com frosting de cream cheese.",
    category: "Red velvet",
    image: "photo-1587668178277-295251f900ce",
  },
  {
    name: "Doce de Leite",
    description: "Doce de leite artesanal, flor de sal e massa amanteigada.",
    category: "Especiais",
    image: "photo-1603532648955-039310d9ed75",
  },
  {
    name: "Choco Miolo",
    description: "Surpresa de ganache no coração de um bolinho de cacau.",
    category: "Chocolate",
    image: "photo-1606313564200-e75d5e30476c",
    tag: "Coração cremoso",
  },
  {
    name: "Oreo Cream",
    description: "Biscoito crocante, creme de baunilha e muita diversão.",
    category: "Especiais",
    image: "photo-1602351447937-745cb720612f",
  },
  {
    name: "Confete Mágico",
    description: "Baunilha, confeitos coloridos e memórias felizes.",
    category: "Baunilha",
    image: "photo-1519869325930-281384150729",
    tag: "Alegria em cada mordida",
  },
];

export const categories = ["Todos", "Chocolate", "Baunilha", "Morango", "Red velvet", "Especiais"];

export function productImage(image: string) {
  return image.startsWith("http") || image.startsWith("/")
    ? image
    : `https://images.unsplash.com/${image}?auto=format&fit=crop&w=900&q=85`;
}

export function formatCurrency(value: number) {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);
}

export function calculateDelivery(subtotal: number) {
  return subtotal >= FREE_DELIVERY_FROM ? 0 : DELIVERY_FEE;
}

export function isValidEmail(email: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email.trim());
}

export function formatCep(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 8);
  return digits.length > 5 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : digits;
}

export function formatPhone(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 11);
  if (digits.length <= 2) return digits;
  if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
  if (digits.length <= 10) return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
}
