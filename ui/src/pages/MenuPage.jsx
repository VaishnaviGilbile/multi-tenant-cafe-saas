import React, { useState } from "react";
import toast from "react-hot-toast";
import {
  getMenuApi, createMenuItemApi, updateMenuItemApi,
  toggleAvailabilityApi, deleteMenuItemApi
} from "../api/api";
import { useApi } from "../hooks/useApi";

const CATEGORIES = ["ALL", "HOT_DRINK", "COLD_DRINK", "FOOD", "DESSERT", "SNACK"];

function MenuItemModal({ item, onClose, onSaved }) {
  const [form, setForm] = useState(
    item || { name: "", description: "", price: "", category: "HOT_DRINK", prepTimeMinutes: "" }
  );
  const [loading, setLoading] = useState(false);
  const set = (k, v) => setForm(p => ({ ...p, [k]: v }));

  const submit = async () => {
    if (!form.name || !form.price) { toast.error("Name and price are required"); return; }
    setLoading(true);
    try {
      const payload = { ...form, price: parseFloat(form.price), prepTimeMinutes: parseInt(form.prepTimeMinutes) || null };
      item ? await updateMenuItemApi(item.id, payload) : await createMenuItemApi(payload);
      toast.success(item ? "Item updated" : "Item added!");
      onSaved();
      onClose();
    } catch (e) {
      toast.error(e.response?.data?.message || "Save failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-title">{item ? "Edit Item" : "Add Menu Item"}</div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Name</label>
            <input className="form-input" value={form.name} onChange={e => set("name", e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Price (₹)</label>
            <input className="form-input" type="number" value={form.price} onChange={e => set("price", e.target.value)} />
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Category</label>
            <select className="form-select" value={form.category} onChange={e => set("category", e.target.value)}>
              {CATEGORIES.slice(1).map(c => <option key={c}>{c}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Prep Time (min)</label>
            <input className="form-input" type="number" value={form.prepTimeMinutes || ""} onChange={e => set("prepTimeMinutes", e.target.value)} />
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <input className="form-input" value={form.description || ""} onChange={e => set("description", e.target.value)} />
        </div>
        <div className="modal-footer">
          <button className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={submit} disabled={loading}>
            {loading ? <span className="spinner" /> : "Save"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function MenuPage() {
  const { data: items, loading, refetch } = useApi(getMenuApi);
  const [filter, setFilter]   = useState("ALL");
  const [editing, setEditing] = useState(null);
  const [showModal, setShowModal] = useState(false);

  const filtered = filter === "ALL" ? (items || []) : (items || []).filter(i => i.category === filter);

  const toggle = async (id) => {
    try {
      await toggleAvailabilityApi(id);
      toast.success("Availability updated");
      refetch();
    } catch (e) { toast.error("Update failed"); }
  };

  const remove = async (id) => {
    if (!window.confirm("Remove this item?")) return;
    try {
      await deleteMenuItemApi(id);
      toast.success("Item removed");
      refetch();
    } catch (e) { toast.error("Delete failed"); }
  };

  if (loading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 48 }}><div className="spinner" /></div>;
  }

  return (
    <div>
      <div className="section-header">
        <div className="tag-row" style={{ marginBottom: 0 }}>
          {CATEGORIES.map(c => (
            <span key={c} className={`tag ${filter === c ? "active" : ""}`} onClick={() => setFilter(c)}>
              {c.replace("_", " ")}
            </span>
          ))}
        </div>
        <button className="btn btn-primary btn-sm" onClick={() => { setEditing(null); setShowModal(true); }}>
          + Add Item
        </button>
      </div>
      <div style={{ height: 16 }} />
      <div className="menu-grid">
        {filtered.map(item => (
          <div key={item.id} className="menu-item-card" style={{ opacity: item.available ? 1 : 0.55 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
              <div className="menu-item-name">{item.name}</div>
              <span className={`badge badge-${item.available ? "active" : "inactive"}`}>
                {item.available ? "On" : "Off"}
              </span>
            </div>
            <div className="menu-item-price">₹{item.price}</div>
            {item.description && <div className="menu-item-desc">{item.description}</div>}
            <div className="menu-item-footer">
              <span className="category-pill">{item.category.replace("_", " ")}</span>
              <div style={{ display: "flex", gap: 4 }}>
                <button className="btn-icon" onClick={() => toggle(item.id)} title="Toggle availability">⟳</button>
                <button className="btn-icon" onClick={() => { setEditing(item); setShowModal(true); }}>✎</button>
                <button className="btn-icon" style={{ color: "var(--red)" }} onClick={() => remove(item.id)}>✕</button>
              </div>
            </div>
          </div>
        ))}
        {filtered.length === 0 && (
          <div className="empty-state" style={{ gridColumn: "1/-1" }}>
            <div className="empty-icon">☕</div>
            <div>No items in this category</div>
          </div>
        )}
      </div>
      {showModal && (
        <MenuItemModal
          item={editing}
          onClose={() => { setShowModal(false); setEditing(null); }}
          onSaved={refetch}
        />
      )}
    </div>
  );
}
