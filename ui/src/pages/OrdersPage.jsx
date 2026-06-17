import React, { useState } from "react";
import toast from "react-hot-toast";
import {
  getActiveOrdersApi, updateOrderStatusApi, createOrderApi, getMenuApi
} from "../api/api";
import { useApi } from "../hooks/useApi";

function timeAgo(iso) {
  const diff = Math.floor((Date.now() - new Date(iso)) / 60000);
  if (diff < 1) return "just now";
  return diff === 1 ? "1 min ago" : `${diff} min ago`;
}

const NEXT_STATUS = { PENDING: "PREPARING", PREPARING: "READY", READY: "SERVED" };
const BTN_LABEL   = { PENDING: "→ Preparing", PREPARING: "→ Ready", READY: "→ Served" };

function OrderCard({ order, onAdvance, onCancel }) {
  return (
    <div className="order-card">
      <div className="order-card-header">
        <div>
          <div className="order-table">Table {order.tableNumber}</div>
          <div style={{ fontSize: 12, color: "var(--muted)", marginTop: 1 }}>{order.customerName}</div>
        </div>
        <div className="order-time">{timeAgo(order.createdAt)}</div>
      </div>
      <div className="order-items">
        {order.items?.map((item, i) => (
          <div key={i}>• {item.menuItemName} × {item.quantity}</div>
        ))}
        {order.notes && (
          <div style={{ color: "var(--gold)", marginTop: 4, fontSize: 11 }}>Note: {order.notes}</div>
        )}
      </div>
      <div className="order-footer">
        <div className="order-total">₹{order.totalAmount}</div>
        <div className="order-actions">
          {order.status !== "READY" && (
            <button className="btn btn-ghost btn-sm" onClick={() => onCancel(order.id)}>Cancel</button>
          )}
          <button className="btn btn-primary btn-sm" onClick={() => onAdvance(order.id, order.status)}>
            {BTN_LABEL[order.status]}
          </button>
        </div>
      </div>
    </div>
  );
}

function CreateOrderModal({ menu, onClose, onCreated }) {
  const [table, setTable]     = useState("");
  const [customer, setCustomer] = useState("");
  const [notes, setNotes]     = useState("");
  const [items, setItems]     = useState([{ menuItemId: "", quantity: 1 }]);
  const [loading, setLoading] = useState(false);

  const addItem    = () => setItems(p => [...p, { menuItemId: "", quantity: 1 }]);
  const removeItem = (i) => setItems(p => p.filter((_, idx) => idx !== i));
  const updateItem = (i, k, v) => setItems(p => p.map((it, idx) => idx === i ? { ...it, [k]: v } : it));

  const submit = async () => {
    const filled = items.filter(i => i.menuItemId);
    if (!table || !filled.length) { toast.error("Table and at least one item required"); return; }
    setLoading(true);
    try {
      await createOrderApi({ tableNumber: table, customerName: customer, notes, items: filled });
      toast.success("Order created!");
      onCreated();
      onClose();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to create order");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-title">New Order</div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Table</label>
            <input className="form-input" placeholder="T4" value={table} onChange={e => setTable(e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Customer</label>
            <input className="form-input" placeholder="Name (optional)" value={customer} onChange={e => setCustomer(e.target.value)} />
          </div>
        </div>
        {items.map((item, i) => (
          <div key={i} style={{ display: "flex", gap: 8, alignItems: "flex-end", marginBottom: 8 }}>
            <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
              <label className="form-label">Item</label>
              <select className="form-select" value={item.menuItemId} onChange={e => updateItem(i, "menuItemId", e.target.value)}>
                <option value="">Select item</option>
                {(menu || []).filter(m => m.available).map(m => (
                  <option key={m.id} value={m.id}>{m.name} — ₹{m.price}</option>
                ))}
              </select>
            </div>
            <div className="form-group" style={{ width: 64, marginBottom: 0 }}>
              <label className="form-label">Qty</label>
              <input className="form-input" type="number" min="1" value={item.quantity}
                onChange={e => updateItem(i, "quantity", parseInt(e.target.value) || 1)} />
            </div>
            {items.length > 1 && (
              <button className="btn-icon" onClick={() => removeItem(i)} style={{ marginBottom: 0 }}>✕</button>
            )}
          </div>
        ))}
        <button className="btn btn-ghost btn-sm" style={{ marginBottom: 12 }} onClick={addItem}>+ Add item</button>
        <div className="form-group">
          <label className="form-label">Notes</label>
          <input className="form-input" placeholder="e.g. No sugar, oat milk" value={notes} onChange={e => setNotes(e.target.value)} />
        </div>
        <div className="modal-footer">
          <button className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={submit} disabled={loading}>
            {loading ? <span className="spinner" /> : "Place Order"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function OrdersPage() {
  const { data: orders, loading, refetch } = useApi(getActiveOrdersApi);
  const { data: menu }                     = useApi(getMenuApi);
  const [showCreate, setShowCreate]        = useState(false);

  const advance = async (id, currentStatus) => {
    const next = NEXT_STATUS[currentStatus];
    if (!next) return;
    try {
      await updateOrderStatusApi(id, next);
      toast.success(`Order moved to ${next}`);
      refetch();
    } catch (e) {
      toast.error(e.response?.data?.message || "Update failed");
    }
  };

  const cancel = async (id) => {
    try {
      await updateOrderStatusApi(id, "CANCELLED");
      toast.success("Order cancelled");
      refetch();
    } catch (e) {
      toast.error("Cancel failed");
    }
  };

  const cols = [
    { status: "PENDING",   label: "Pending",   color: "var(--orange)" },
    { status: "PREPARING", label: "Preparing", color: "var(--blue)" },
    { status: "READY",     label: "Ready",     color: "var(--green)" },
  ];

  if (loading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 48 }}><div className="spinner" /></div>;
  }

  return (
    <div>
      <div className="section-header">
        <div />
        <button className="btn btn-primary btn-sm" onClick={() => setShowCreate(true)}>+ New Order</button>
      </div>
      <div className="order-board">
        {cols.map(col => {
          const colOrders = (orders || []).filter(o => o.status === col.status);
          return (
            <div key={col.status}>
              <div className="order-col-header">
                <span className="order-col-title" style={{ color: col.color }}>{col.label}</span>
                <span className="order-count">{colOrders.length}</span>
              </div>
              <div className="order-col-scroll scrollbar-hide">
                {colOrders.length === 0 ? (
                  <div style={{ textAlign: "center", padding: "20px 0", color: "var(--muted)", fontSize: 12 }}>No orders</div>
                ) : (
                  colOrders.map(o => (
                    <OrderCard key={o.id} order={o} onAdvance={advance} onCancel={cancel} />
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
      {showCreate && (
        <CreateOrderModal menu={menu} onClose={() => setShowCreate(false)} onCreated={refetch} />
      )}
    </div>
  );
}
