import React from "react";
import { getDashboardApi, getActiveOrdersApi } from "../api/api";
import { useApi } from "../hooks/useApi";

function StatCard({ label, value, sub, color, icon }) {
  return (
    <div className="stat-card">
      <div className="stat-icon-wrap" style={{ background: `${color}18` }}>
        <span style={{ fontSize: 18 }}>{icon}</span>
      </div>
      <div className="stat-label">{label}</div>
      <div className="stat-value" style={{ color }}>{value}</div>
      <div className="stat-sub">{sub}</div>
    </div>
  );
}

function StatusBadge({ status }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{status}</span>;
}

function timeAgo(iso) {
  const diff = Math.floor((Date.now() - new Date(iso)) / 60000);
  if (diff < 1) return "just now";
  return diff === 1 ? "1 min ago" : `${diff} min ago`;
}

export default function Dashboard() {
  const { data: stats, loading: statsLoading }   = useApi(getDashboardApi);
  const { data: orders, loading: ordersLoading } = useApi(getActiveOrdersApi);

  if (statsLoading || ordersLoading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 48 }}><div className="spinner" /></div>;
  }

  const s = stats || {};
  const o = orders || [];

  return (
    <div>
      <div className="grid-4" style={{ marginBottom: 24 }}>
        <StatCard label="Pending Orders"   value={s.pendingOrders}   sub="Awaiting kitchen"         color="var(--orange)" icon="⏳" />
        <StatCard label="In Preparation"   value={s.preparingOrders} sub="Being made now"           color="var(--blue)"   icon="👨‍🍳" />
        <StatCard label="Today's Orders"   value={s.todayOrderCount} sub="Completed today"          color="var(--green)"  icon="✓" />
        <StatCard label="Today's Revenue"  value={`₹${(s.todayRevenue || 0).toLocaleString()}`}
                  sub={`₹${(s.weekRevenue || 0).toLocaleString()} this week`}                       color="var(--gold)"   icon="₹" />
      </div>

      <div className="card">
        <div className="card-title">Active Orders</div>
        {o.length === 0 ? (
          <div className="empty-state">No active orders right now</div>
        ) : (
          <table>
            <thead><tr>
              <th>Table</th><th>Customer</th><th>Items</th><th>Total</th><th>Status</th><th>Time</th>
            </tr></thead>
            <tbody>
              {o.map(order => (
                <tr key={order.id}>
                  <td style={{ fontWeight: 600 }}>T{order.tableNumber}</td>
                  <td>{order.customerName}</td>
                  <td style={{ color: "var(--muted)", fontSize: 12 }}>
                    {order.items?.map(i => `${i.menuItemName} ×${i.quantity}`).join(", ")}
                  </td>
                  <td style={{ fontWeight: 600, color: "var(--gold)" }}>₹{order.totalAmount}</td>
                  <td><StatusBadge status={order.status} /></td>
                  <td style={{ color: "var(--muted)", fontSize: 12 }}>{timeAgo(order.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
