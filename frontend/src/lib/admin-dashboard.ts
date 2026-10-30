export type DashboardPeriod = "today" | "7days" | "30days" | "custom";
export type OrderStatus = "PENDENTE" | "PAGO" | "EM_PROCESSAMENTO" | "ENVIADO" | "CONCLUIDO" | "CANCELADO";
export type PaymentStatus = "PENDENTE" | "RECEBIDO" | "ESTORNADO";

export type DashboardOrder = {
  id: string;
  customerName: string;
  createdAt: string;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  total: number;
};

export type DailyRevenue = {
  date: string;
  label: string;
  revenue: number;
  orders: number;
};

export type DashboardSummary = {
  startDate: string;
  endDate: string;
  totalOrders: number;
  revenue: number;
  receivedOrders: number;
  averageOrder: number;
  statuses: Record<OrderStatus, number>;
  dailyRevenue: DailyRevenue[];
  recentOrders: DashboardOrder[];
};

export type DashboardQuery = {
  period: DashboardPeriod;
  startDate?: string;
  endDate?: string;
};

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PENDENTE: "Pendentes",
  PAGO: "Pagos",
  EM_PROCESSAMENTO: "Em processamento",
  ENVIADO: "Enviados",
  CONCLUIDO: "Concluídos",
  CANCELADO: "Cancelados",
};

export const ORDER_STATUS_COLORS: Record<OrderStatus, string> = {
  PENDENTE: "#d4a453",
  PAGO: "#6c9c76",
  EM_PROCESSAMENTO: "#8d78b8",
  ENVIADO: "#6994bf",
  CONCLUIDO: "#679183",
  CANCELADO: "#c47676",
};

const statuses: OrderStatus[] = [
  "CONCLUIDO",
  "EM_PROCESSAMENTO",
  "ENVIADO",
  "PENDENTE",
  "PAGO",
  "CANCELADO",
];

function toDateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function fromDateKey(value: string) {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function getMockOrders(): DashboardOrder[] {
  const now = new Date();

  return Array.from({ length: 54 }, (_, index) => {
    const createdAt = new Date(now);
    createdAt.setDate(now.getDate() - (index % 30));
    createdAt.setHours(8 + ((index * 7) % 11), (index * 13) % 60, 0, 0);
    const status = statuses[(index * 5 + Math.floor(index / 4)) % statuses.length];
    const names = ["Mariana Costa", "Beatriz Almeida", "Lucas Ferreira", "Ana Oliveira", "Camila Santos", "Rafael Lima"];

    return {
      id: `DM-${createdAt.getTime().toString(36).toUpperCase()}-${String(index + 1).padStart(3, "0")}`,
      customerName: names[index % names.length],
      createdAt: createdAt.toISOString(),
      status,
      paymentStatus: status === "CANCELADO"
        ? "ESTORNADO"
        : status === "PENDENTE"
          ? "PENDENTE"
          : "RECEBIDO",
      total: [38.7, 54.8, 29.8, 72.4, 45.6, 96.3, 34.5, 61.2][index % 8],
    };
  });
}

function getPeriodRange(query: DashboardQuery, now: Date) {
  const end = new Date(now);
  end.setHours(23, 59, 59, 999);
  let start = new Date(now);
  start.setHours(0, 0, 0, 0);

  if (query.period === "7days") start.setDate(start.getDate() - 6);
  if (query.period === "30days") start.setDate(start.getDate() - 29);

  if (query.period === "custom") {
    if (!query.startDate || !query.endDate) {
      throw new Error("Selecione a data inicial e a data final do período.");
    }
    start = fromDateKey(query.startDate);
    end.setTime(fromDateKey(query.endDate).getTime());
    end.setHours(23, 59, 59, 999);
    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || start > end) {
      throw new Error("A data inicial deve ser anterior ou igual à data final.");
    }
    const days = Math.floor((end.getTime() - start.getTime()) / 86_400_000);
    if (days > 89) throw new Error("Escolha um intervalo de até 90 dias.");
  }

  return { start, end };
}

async function fetchMockDashboard(query: DashboardQuery): Promise<DashboardSummary> {
  await new Promise((resolve) => window.setTimeout(resolve, 220));

  const now = new Date();
  const { start, end } = getPeriodRange(query, now);
  const orders = getMockOrders()
    .filter((order) => {
      const date = new Date(order.createdAt);
      return date >= start && date <= end;
    })
    .sort((left, right) => right.createdAt.localeCompare(left.createdAt));
  const statusesSummary = Object.fromEntries(statuses.map((status) => [status, 0])) as Record<OrderStatus, number>;
  for (const order of orders) statusesSummary[order.status] += 1;

  const received = orders.filter((order) => order.paymentStatus === "RECEBIDO" && order.status !== "CANCELADO");
  const dailyRevenue: DailyRevenue[] = [];
  const cursor = new Date(start);
  while (cursor <= end) {
    const date = toDateKey(cursor);
    const dayOrders = received.filter((order) => toDateKey(new Date(order.createdAt)) === date);
    dailyRevenue.push({
      date,
      label: cursor.toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" }),
      revenue: dayOrders.reduce((sum, order) => sum + order.total, 0),
      orders: dayOrders.length,
    });
    cursor.setDate(cursor.getDate() + 1);
  }

  return {
    startDate: toDateKey(start),
    endDate: toDateKey(end),
    totalOrders: orders.length,
    revenue: received.reduce((sum, order) => sum + order.total, 0),
    receivedOrders: received.length,
    averageOrder: received.length ? received.reduce((sum, order) => sum + order.total, 0) / received.length : 0,
    statuses: statusesSummary,
    dailyRevenue,
    recentOrders: orders.slice(0, 6),
  };
}

export const adminDashboardService = {
  getSummary: fetchMockDashboard,
};
