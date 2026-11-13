export type DashboardPeriod = "today" | "7days" | "30days" | "custom";
export type OrderStatus = "PENDENTE" | "PAGO" | "EM_PROCESSAMENTO" | "ENVIADO" | "CONCLUIDO" | "CANCELADO";

export type DashboardOrder = {
  id: string;
  number: string;
  createdAt: string;
  status: OrderStatus;
  total: number;
};

export type DailyRevenue = {
  date: string;
  label: string;
  revenue: number;
};

export type DashboardSummary = {
  activeUsers: number;
  activeProducts: number;
  activeCoupons: number;
  pendingOrders: number;
  allTimeOrders: number;
  confirmedRevenue: number;
  period: string;
  startDate: string;
  endDate: string;
  totalOrders: number;
  revenue: number;
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
  PENDENTE: "Aguardando pagamento",
  PAGO: "Pago",
  EM_PROCESSAMENTO: "Em preparação",
  ENVIADO: "Em entrega",
  CONCLUIDO: "Entregue",
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

export const ORDER_STATUS_TRANSITIONS: Partial<Record<OrderStatus, OrderStatus[]>> = {
  PENDENTE: ["PAGO", "ENVIADO", "CONCLUIDO", "CANCELADO"],
  PAGO: ["EM_PROCESSAMENTO"],
  EM_PROCESSAMENTO: ["ENVIADO"],
  ENVIADO: ["CONCLUIDO"],
};

const backendStatusNames: Record<OrderStatus, string> = {
  PENDENTE: "PENDENTE_PAGAMENTO",
  PAGO: "PAGO",
  EM_PROCESSAMENTO: "EM_PREPARACAO",
  ENVIADO: "ENVIADO",
  CONCLUIDO: "CONCLUIDO",
  CANCELADO: "CANCELADO",
};

const statuses: OrderStatus[] = [
  "PENDENTE",
  "PAGO",
  "EM_PROCESSAMENTO",
  "ENVIADO",
  "CONCLUIDO",
  "CANCELADO",
];

type BackendDashboard = {
  usuariosAtivos: number;
  produtosAtivos: number;
  cuponsAtivos: number;
  pedidosPendentes: number;
  totalPedidos: number;
  faturamentoConfirmado: number;
  periodo: string;
  dataInicio: string;
  dataFim: string;
  faturamentoRecebido: number;
  pedidosNoPeriodo: number;
  ticketMedioRecebido: number;
  faturamentoPorDia: { data: string; faturamento: number }[];
  pedidosPorStatus: { status: string; quantidade: number }[];
  ultimosPedidos: {
    idPedido: string;
    numeroPedido: string;
    status: string;
    dataPedido: string;
    valorTotal: number;
  }[];
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function readNumber(value: unknown, field: string): number {
  const parsed = typeof value === "number" ? value : typeof value === "string" ? Number(value) : Number.NaN;
  if (!Number.isFinite(parsed)) throw new Error(`O campo ${field} retornado pela API é inválido.`);
  return parsed;
}

function readString(value: unknown, field: string): string {
  if (typeof value !== "string" || !value) throw new Error(`O campo ${field} retornado pela API é inválido.`);
  return value;
}

function readDashboard(payload: unknown): BackendDashboard {
  if (!isRecord(payload) || !Array.isArray(payload.faturamentoPorDia) ||
    !Array.isArray(payload.pedidosPorStatus) || !Array.isArray(payload.ultimosPedidos)) {
    throw new Error("A API retornou o dashboard em formato inválido.");
  }

  const statusesData = payload.pedidosPorStatus.map((entry, index) => {
    if (!isRecord(entry)) throw new Error(`O status ${index + 1} retornado pela API é inválido.`);
    return {
      status: readString(entry.status, `pedidosPorStatus[${index}].status`),
      quantidade: readNumber(entry.quantidade, `pedidosPorStatus[${index}].quantidade`),
    };
  });
  const dailyData = payload.faturamentoPorDia.map((entry, index) => {
    if (!isRecord(entry)) throw new Error(`O faturamento diário ${index + 1} retornado pela API é inválido.`);
    return {
      data: readString(entry.data, `faturamentoPorDia[${index}].data`),
      faturamento: readNumber(entry.faturamento, `faturamentoPorDia[${index}].faturamento`),
    };
  });
  const ordersData = payload.ultimosPedidos.map((entry, index) => {
    if (!isRecord(entry)) throw new Error(`O pedido recente ${index + 1} retornado pela API é inválido.`);
    return {
      idPedido: readString(entry.idPedido, `ultimosPedidos[${index}].idPedido`),
      numeroPedido: readString(entry.numeroPedido, `ultimosPedidos[${index}].numeroPedido`),
      status: readString(entry.status, `ultimosPedidos[${index}].status`),
      dataPedido: readString(entry.dataPedido, `ultimosPedidos[${index}].dataPedido`),
      valorTotal: readNumber(entry.valorTotal, `ultimosPedidos[${index}].valorTotal`),
    };
  });

  return {
    usuariosAtivos: readNumber(payload.usuariosAtivos, "usuariosAtivos"),
    produtosAtivos: readNumber(payload.produtosAtivos, "produtosAtivos"),
    cuponsAtivos: readNumber(payload.cuponsAtivos, "cuponsAtivos"),
    pedidosPendentes: readNumber(payload.pedidosPendentes, "pedidosPendentes"),
    totalPedidos: readNumber(payload.totalPedidos, "totalPedidos"),
    faturamentoConfirmado: readNumber(payload.faturamentoConfirmado, "faturamentoConfirmado"),
    periodo: readString(payload.periodo, "periodo"),
    dataInicio: readString(payload.dataInicio, "dataInicio"),
    dataFim: readString(payload.dataFim, "dataFim"),
    faturamentoRecebido: readNumber(payload.faturamentoRecebido, "faturamentoRecebido"),
    pedidosNoPeriodo: readNumber(payload.pedidosNoPeriodo, "pedidosNoPeriodo"),
    ticketMedioRecebido: readNumber(payload.ticketMedioRecebido, "ticketMedioRecebido"),
    faturamentoPorDia: dailyData,
    pedidosPorStatus: statusesData,
    ultimosPedidos: ordersData,
  };
}

function mapStatus(status: string): OrderStatus {
  switch (status) {
    case "PENDENTE_PAGAMENTO": return "PENDENTE";
    case "EM_PREPARACAO": return "EM_PROCESSAMENTO";
    case "PAGO":
    case "ENVIADO":
    case "CONCLUIDO":
    case "CANCELADO":
      return status;
    default:
      throw new Error(`A API retornou um status de pedido desconhecido: ${status}.`);
  }
}

function formatDayLabel(date: string) {
  return new Date(`${date}T12:00:00`).toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" });
}

function mapDashboard(data: BackendDashboard): DashboardSummary {
  const statusCounts = Object.fromEntries(statuses.map((status) => [status, 0])) as Record<OrderStatus, number>;
  for (const entry of data.pedidosPorStatus) {
    const status = mapStatus(entry.status);
    statusCounts[status] += entry.quantidade;
  }

  return {
    activeUsers: data.usuariosAtivos,
    activeProducts: data.produtosAtivos,
    activeCoupons: data.cuponsAtivos,
    pendingOrders: data.pedidosPendentes,
    allTimeOrders: data.totalPedidos,
    confirmedRevenue: data.faturamentoConfirmado,
    period: data.periodo,
    startDate: data.dataInicio,
    endDate: data.dataFim,
    totalOrders: data.pedidosNoPeriodo,
    revenue: data.faturamentoRecebido,
    averageOrder: data.ticketMedioRecebido,
    statuses: statusCounts,
    dailyRevenue: data.faturamentoPorDia.map((entry) => ({
      date: entry.data,
      label: formatDayLabel(entry.data),
      revenue: entry.faturamento,
    })),
    recentOrders: data.ultimosPedidos.map((order) => ({
      id: order.idPedido,
      number: order.numeroPedido,
      createdAt: order.dataPedido,
      status: mapStatus(order.status),
      total: order.valorTotal,
    })),
  };
}

async function fetchDashboard(query: DashboardQuery, token: string): Promise<DashboardSummary> {
  const parameters = new URLSearchParams({
    periodo: query.period === "today"
      ? "HOJE"
      : query.period === "7days"
        ? "ULTIMOS_7_DIAS"
        : query.period === "30days"
          ? "ULTIMOS_30_DIAS"
          : "PERSONALIZADO",
  });
  if (query.period === "custom") {
    if (!query.startDate || !query.endDate) {
      throw new Error("Selecione a data inicial e a data final do período.");
    }
    parameters.set("dataInicio", query.startDate);
    parameters.set("dataFim", query.endDate);
  }

  const response = await fetch(`/api/admin/dashboard?${parameters.toString()}`, {
    headers: { Authorization: `Bearer ${token}` },
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
    const message = isRecord(payload) && typeof payload.detail === "string"
      ? payload.detail
      : isRecord(payload) && typeof payload.message === "string"
        ? payload.message
        : typeof payload === "string" && payload
          ? payload
          : `A API recusou a solicitação (HTTP ${response.status}).`;
    throw new Error(message);
  }

  return mapDashboard(readDashboard(payload));
}

async function updateOrderStatus(orderId: string, status: OrderStatus, token: string): Promise<void> {
  const response = await fetch(`/api/pedidos/admin/${encodeURIComponent(orderId)}/status`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ status: backendStatusNames[status] }),
  });
  if (response.ok) return;

  const responseText = await response.text();
  let payload: unknown = null;
  if (responseText) {
    try {
      payload = JSON.parse(responseText);
    } catch {
      payload = responseText;
    }
  }
  const message = isRecord(payload) && typeof payload.detail === "string"
    ? payload.detail
    : isRecord(payload) && typeof payload.message === "string"
      ? payload.message
      : typeof payload === "string" && payload
        ? payload
        : `A API recusou a alteração (HTTP ${response.status}).`;
  throw new Error(message);
}

export const adminDashboardService = {
  getSummary: fetchDashboard,
  updateOrderStatus,
};
