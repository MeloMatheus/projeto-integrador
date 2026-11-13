"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  ArrowUpRight,
  CalendarDays,
  CakeSlice,
  CheckCircle2,
  ChevronDown,
  CircleDollarSign,
  ClipboardList,
  Clock3,
  CreditCard,
  LayoutDashboard,
  LoaderCircle,
  LogOut,
  PackageCheck,
  Package,
  RefreshCw,
  ShoppingBag,
  TicketPercent,
  Truck,
  UsersRound,
  Wallet,
  XCircle,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  adminDashboardService,
  ORDER_STATUS_COLORS,
  ORDER_STATUS_LABELS,
  ORDER_STATUS_TRANSITIONS,
  type DashboardPeriod,
  type DashboardSummary,
  type OrderStatus,
} from "@/lib/admin-dashboard";
import { formatCurrency } from "@/lib/shop-data";

const periods: { id: DashboardPeriod; label: string }[] = [
  { id: "today", label: "Hoje" },
  { id: "7days", label: "7 dias" },
  { id: "30days", label: "30 dias" },
  { id: "custom", label: "Personalizado" },
];

const statusOrder: OrderStatus[] = [
  "PENDENTE",
  "PAGO",
  "EM_PROCESSAMENTO",
  "ENVIADO",
  "CONCLUIDO",
  "CANCELADO",
];

const statusIcons: Record<OrderStatus, typeof Clock3> = {
  PENDENTE: Clock3,
  PAGO: CreditCard,
  EM_PROCESSAMENTO: PackageCheck,
  ENVIADO: Truck,
  CONCLUIDO: CheckCircle2,
  CANCELADO: XCircle,
};

function dateKey(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

function getDefaultStart() {
  const date = new Date();
  date.setDate(date.getDate() - 6);
  return dateKey(date);
}

function formatLongDate(date: Date) {
  return date.toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long" });
}

export function AdminDashboard({ userName, token, onLogout }: { userName: string; token: string; onLogout: () => void }) {
  const router = useRouter();
  const [period, setPeriod] = useState<DashboardPeriod>("7days");
  const [startDate, setStartDate] = useState(getDefaultStart);
  const [endDate, setEndDate] = useState(() => dateKey(new Date()));
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [selectedStatuses, setSelectedStatuses] = useState<Record<string, OrderStatus>>({});
  const [updatingOrderId, setUpdatingOrderId] = useState<string | null>(null);
  const [orderStatusError, setOrderStatusError] = useState("");
  const [orderStatusNotice, setOrderStatusNotice] = useState("");

  useEffect(() => {
    let active = true;
    adminDashboardService.getSummary({ period, startDate, endDate }, token)
      .then((result) => {
        if (active) setSummary(result);
      })
      .catch((caught: unknown) => {
        if (active) setError(caught instanceof Error ? caught.message : "Não foi possível carregar os dados do painel.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, [period, startDate, endDate, refreshKey, token]);

  const displayRange = useMemo(() => {
    if (!summary) return "";
    const start = new Date(`${summary.startDate}T12:00:00`);
    const end = new Date(`${summary.endDate}T12:00:00`);
    if (summary.startDate === summary.endDate) return formatLongDate(end);
    return `${start.toLocaleDateString("pt-BR", { day: "2-digit", month: "short" })} – ${end.toLocaleDateString("pt-BR", { day: "2-digit", month: "short", year: "numeric" })}`;
  }, [summary]);

  const logout = () => {
    onLogout();
    router.push("/login");
  };

  const changePeriod = (next: DashboardPeriod) => {
    setLoading(true);
    setError("");
    setPeriod(next);
  };

  const refresh = () => {
    setLoading(true);
    setError("");
    setRefreshKey((key) => key + 1);
  };

  const saveOrderStatus = async (orderId: string, orderNumber: string) => {
    const nextStatus = selectedStatuses[orderId];
    if (!nextStatus) return;

    setUpdatingOrderId(orderId);
    setOrderStatusError("");
    setOrderStatusNotice("");
    try {
      await adminDashboardService.updateOrderStatus(orderId, nextStatus, token);
      setSelectedStatuses((current) => {
        const updated = { ...current };
        delete updated[orderId];
        return updated;
      });
      setOrderStatusNotice(`Status do pedido ${orderNumber} atualizado.`);
      refresh();
    } catch (caught: unknown) {
      setOrderStatusError(caught instanceof Error ? caught.message : "Não foi possível alterar o status do pedido.");
    } finally {
      setUpdatingOrderId(null);
    }
  };

  const changeStartDate = (value: string) => {
    setLoading(true);
    setError("");
    setStartDate(value);
  };

  const changeEndDate = (value: string) => {
    setLoading(true);
    setError("");
    setEndDate(value);
  };

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <Link href="/admin" className="admin-brand">
          <span className="admin-brand-mark"><CakeSlice size={20} /></span>
          <span>doce <b>miolo</b><small>PAINEL ADMINISTRATIVO</small></span>
        </Link>
        <div className="admin-sidebar-label">MENU PRINCIPAL</div>
        <a href="#visao-geral" className="admin-nav-item active"><LayoutDashboard size={17} /> Visão geral</a>
        <a href="#pedidos" className="admin-nav-item"><ClipboardList size={17} /> Pedidos <ChevronDown size={13} /></a>
        <div className="admin-sidebar-spacer" />
        <div className="admin-profile">
          <span className="admin-profile-avatar">{userName.charAt(0).toUpperCase()}</span>
          <span><b>{userName}</b><small>Administrador</small></span>
          <button type="button" onClick={logout} aria-label="Sair do painel"><LogOut size={16} /></button>
        </div>
      </aside>

      <main className="admin-main" id="visao-geral">
        <header className="admin-topbar">
          <div className="admin-breadcrumb"><span>Painel</span><b>/</b><strong>Visão geral</strong></div>
          <div className="admin-top-actions">
            <Badge className="admin-demo-badge"><span /> DASHBOARD ADMINISTRATIVO</Badge>
            <Button variant="outline" className="admin-logout-button" onClick={logout}><LogOut size={15} /> Sair</Button>
          </div>
        </header>

        <div className="admin-content">
          <section className="admin-welcome">
            <div><span className="eyebrow">DOCE MIOLO · PAINEL DA LOJA</span><h1>Bom dia, {userName.split(" ")[0]} <span>☀</span></h1><p>Acompanhe o ritmo da sua loja e cada pedido de carinho.</p></div>
            <Button variant="outline" className="admin-refresh" onClick={refresh} disabled={loading}><RefreshCw size={15} className={loading ? "is-spinning" : ""} /> Atualizar dados</Button>
          </section>

          <section className="admin-filters" aria-label="Filtros do dashboard">
            <div className="admin-period-tabs" role="group" aria-label="Selecionar período">{periods.map((option) => <button key={option.id} type="button" className={period === option.id ? "selected" : ""} onClick={() => changePeriod(option.id)} aria-pressed={period === option.id}>{option.label}</button>)}</div>
            <div className="admin-period-date"><CalendarDays size={15} /><span>{displayRange || "Carregando período"}</span><ChevronDown size={13} /></div>
          </section>
          {period === "custom" && <div className="admin-custom-range"><label>De <input type="date" value={startDate} max={endDate} onChange={(event) => changeStartDate(event.target.value)} /></label><span>até</span><label>Até <input type="date" value={endDate} min={startDate} max={dateKey(new Date())} onChange={(event) => changeEndDate(event.target.value)} /></label></div>}
          {error && <div className="admin-error" role="alert"><XCircle size={17} />{error}<button type="button" onClick={refresh}>Tentar novamente</button></div>}

          <section className="admin-metrics" aria-label="Resumo financeiro do período">
            <MetricCard icon={CircleDollarSign} label="Faturamento recebido" value={summary ? formatCurrency(summary.revenue) : "—"} meta={summary ? `${summary.totalOrders} pedidos no período` : "No período selecionado"} tone="rose" loading={loading || Boolean(error)} />
            <MetricCard icon={ShoppingBag} label="Pedidos no período" value={summary ? String(summary.totalOrders) : "—"} meta={summary ? `${summary.statuses.CANCELADO} cancelado(s)` : "Compras registradas"} tone="lavender" loading={loading || Boolean(error)} />
            <MetricCard icon={Wallet} label="Ticket médio recebido" value={summary ? formatCurrency(summary.averageOrder) : "—"} meta="Somente pagamentos recebidos" tone="sage" loading={loading || Boolean(error)} />
          </section>

          <section className="admin-metrics admin-overview-metrics" aria-label="Resumo geral da loja">
            <MetricCard icon={UsersRound} label="Usuários ativos" value={summary ? String(summary.activeUsers) : "—"} meta="Contas ativas" tone="sage" loading={loading || Boolean(error)} />
            <MetricCard icon={Package} label="Produtos ativos" value={summary ? String(summary.activeProducts) : "—"} meta="Disponíveis para venda" tone="lavender" loading={loading || Boolean(error)} />
            <MetricCard icon={TicketPercent} label="Cupons ativos" value={summary ? String(summary.activeCoupons) : "—"} meta="Cupons disponíveis" tone="rose" loading={loading || Boolean(error)} />
            <MetricCard icon={Clock3} label="Pedidos pendentes" value={summary ? String(summary.pendingOrders) : "—"} meta="Aguardando pagamento" tone="lavender" loading={loading || Boolean(error)} />
            <MetricCard icon={ClipboardList} label="Total de pedidos" value={summary ? String(summary.allTimeOrders) : "—"} meta="Desde o início da loja" tone="sage" loading={loading || Boolean(error)} />
            <MetricCard icon={CircleDollarSign} label="Faturamento confirmado total" value={summary ? formatCurrency(summary.confirmedRevenue) : "—"} meta="Pagamentos confirmados" tone="rose" loading={loading || Boolean(error)} />
          </section>

          <section className="admin-dashboard-grid admin-status-overview">
            <Card className="admin-panel-card admin-status-card">
              <CardContent>
                <div className="admin-panel-heading"><div><span className="admin-section-kicker">OPERAÇÃO</span><h2>Status dos pedidos</h2><p>Pedidos no período selecionado.</p></div><ShoppingBag size={18} className="admin-heading-icon" /></div>
                {loading ? <StatusSkeleton /> : error ? <DashboardError /> : summary?.totalOrders ? <div className="admin-status-list">{statusOrder.map((status) => {
                  const Icon = statusIcons[status];
                  const amount = summary.statuses[status];
                  const percent = summary.totalOrders ? amount / summary.totalOrders * 100 : 0;
                  return <div className="admin-status-row" key={status}><span className="admin-status-icon" style={{ color: ORDER_STATUS_COLORS[status], backgroundColor: `${ORDER_STATUS_COLORS[status]}16` }}><Icon size={15} /></span><span className="admin-status-name">{ORDER_STATUS_LABELS[status]}</span><div className="admin-status-track"><i style={{ width: `${percent}%`, backgroundColor: ORDER_STATUS_COLORS[status] }} /></div><b>{amount}</b></div>;
                })}</div> : <StatusEmpty />}
              </CardContent>
            </Card>
          </section>

          <section className="admin-orders-section" id="pedidos">
            <Card className="admin-panel-card">
              <CardContent>
                <div className="admin-panel-heading admin-orders-heading"><div><span className="admin-section-kicker">ATIVIDADE RECENTE</span><h2>Últimos pedidos</h2><p>Pedidos recebidos no período selecionado.</p></div><span className="admin-order-count"><ShoppingBag size={14} />{loading || error ? "—" : summary?.recentOrders.length ?? 0} pedidos</span></div>
                {orderStatusError && <div className="admin-error" role="alert"><XCircle size={16} />{orderStatusError}</div>}
                {orderStatusNotice && <div className="admin-status-notice" role="status"><CheckCircle2 size={16} />{orderStatusNotice}</div>}
                {loading ? <OrdersSkeleton /> : error ? <DashboardError /> : summary?.recentOrders.length ? <div className="admin-table-wrap"><table className="admin-orders-table"><thead><tr><th>Número do pedido</th><th>Data</th><th>Status atual</th><th>Alterar status</th><th className="align-right">Total</th></tr></thead><tbody>{summary.recentOrders.map((order) => {
                  const transitions = ORDER_STATUS_TRANSITIONS[order.status] ?? [];
                  const selectedStatus = selectedStatuses[order.id] ?? "";
                  return <tr key={order.id}>
                    <td><b>{order.number}</b></td>
                    <td>{new Date(order.createdAt).toLocaleString("pt-BR", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" })}</td>
                    <td><OrderStatusBadge status={order.status} /></td>
                    <td><div className="admin-status-editor">
                      <select
                        aria-label={`Novo status do pedido ${order.number}`}
                        value={selectedStatus}
                        disabled={!transitions.length || updatingOrderId !== null}
                        onChange={(event) => {
                          setOrderStatusError("");
                          setOrderStatusNotice("");
                          const nextStatus = transitions.find((status) => status === event.target.value);
                          setSelectedStatuses((current) => {
                            if (nextStatus) return { ...current, [order.id]: nextStatus };
                            const updated = { ...current };
                            delete updated[order.id];
                            return updated;
                          });
                        }}
                      >
                        <option value="">{transitions.length ? "Selecionar" : "Sem alteração"}</option>
                        {transitions.map((status) => <option key={status} value={status}>{ORDER_STATUS_LABELS[status]}</option>)}
                      </select>
                      <button
                        type="button"
                        className="admin-status-save"
                        disabled={!selectedStatus || updatingOrderId !== null}
                        onClick={() => void saveOrderStatus(order.id, order.number)}
                      >
                        {updatingOrderId === order.id ? <LoaderCircle size={13} className="is-spinning" /> : "Salvar"}
                      </button>
                    </div></td>
                    <td className="align-right order-amount">{formatCurrency(order.total)}</td>
                  </tr>;
                })}</tbody></table></div> : <OrdersEmpty />}
              </CardContent>
            </Card>
          </section>

          <footer className="admin-footer"><span>Doce Miolo · Administração</span><span><span className="admin-live-dot" /> Indicadores fornecidos pelo serviço administrativo</span></footer>
        </div>
      </main>
    </div>
  );
}

function MetricCard({ icon: Icon, label, value, meta, tone, loading }: {
  icon: typeof CircleDollarSign;
  label: string;
  value: string;
  meta: string;
  tone: "rose" | "lavender" | "sage";
  loading: boolean;
}) {
  return <Card className={`admin-metric-card tone-${tone}`}><CardContent><span className="metric-icon"><Icon size={19} /></span><span className="metric-label">{label}</span>{loading ? <span className="metric-skeleton" /> : <strong className="metric-value">{value}</strong>}<span className="metric-meta">{meta}</span><span className="metric-corner"><ArrowUpRight size={16} /></span></CardContent></Card>;
}

function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return <span className={`order-status-badge status-${status.toLowerCase()}`}><i />{ORDER_STATUS_LABELS[status]}</span>;
}

function StatusSkeleton() {
  return <div className="admin-status-skeleton">{statusOrder.slice(0, 5).map((status) => <i key={status} />)}</div>;
}

function OrdersSkeleton() {
  return <div className="admin-orders-skeleton">{Array.from({ length: 4 }, (_, index) => <i key={index} />)}</div>;
}

function DashboardError() {
  return <div className="admin-empty-state"><XCircle size={22} /><b>Não foi possível carregar esta seção.</b><span>Tente atualizar os dados.</span></div>;
}

function StatusEmpty() {
  return <div className="admin-empty-state compact"><ShoppingBag size={22} /><b>Nenhum pedido neste período</b><span>Quando chegar uma compra, ela aparecerá aqui.</span></div>;
}

function OrdersEmpty() {
  return <div className="admin-empty-state"><ShoppingBag size={22} /><b>Nenhum pedido para exibir</b><span>Altere o período para consultar outras datas.</span></div>;
}
