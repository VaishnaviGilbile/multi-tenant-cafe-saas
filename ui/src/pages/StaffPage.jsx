import React, { useState } from "react";
import toast from "react-hot-toast";
import { getStaffApi, createStaffApi, deactivateStaffApi } from "../api/api";
import { useApi } from "../hooks/useApi";

function AddStaffModal({ onClose, onAdded }) {
  const [form, setForm] = useState({ name: "", email: "", password: "", role: "STAFF", phoneNumber: "" });
  const [loading, setLoading] = useState(false);
  const set = (k, v) => setForm(p => ({ ...p, [k]: v }));

  const submit = async () => {
    if (!form.name || !form.email || !form.password) {
      toast.error("Name, email, and password are required");
      return;
    }
    setLoading(true);
    try {
      await createStaffApi(form);
      toast.success("Staff member added!");
      onAdded();
      onClose();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to add staff");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-title">Add Staff Member</div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Full Name</label>
            <input className="form-input" value={form.name} onChange={e => set("name", e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Role</label>
            <select className="form-select" value={form.role} onChange={e => set("role", e.target.value)}>
              {["TENANT_ADMIN", "MANAGER", "STAFF", "CASHIER"].map(r => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">Email</label>
          <input className="form-input" type="email" value={form.email} onChange={e => set("email", e.target.value)} />
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Password</label>
            <input className="form-input" type="password" value={form.password} onChange={e => set("password", e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Phone (optional)</label>
            <input className="form-input" value={form.phoneNumber} onChange={e => set("phoneNumber", e.target.value)} />
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={submit} disabled={loading}>
            {loading ? <span className="spinner" /> : "Add Member"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function StaffPage() {
  const { data: staff, loading, refetch } = useApi(getStaffApi);
  const [showModal, setShowModal] = useState(false);

  const deactivate = async (id) => {
    if (!window.confirm("Deactivate this staff member?")) return;
    try {
      await deactivateStaffApi(id);
      toast.success("Staff member deactivated");
      refetch();
    } catch (e) {
      toast.error("Failed to deactivate");
    }
  };

  if (loading) {
    return <div style={{ display: "flex", justifyContent: "center", padding: 48 }}><div className="spinner" /></div>;
  }

  return (
    <div>
      <div className="section-header">
        <div />
        <button className="btn btn-primary btn-sm" onClick={() => setShowModal(true)}>+ Add Staff</button>
      </div>
      <div className="card" style={{ padding: 0, overflow: "hidden" }}>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th style={{ textAlign: "right" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {(staff || []).map(member => (
              <tr key={member.id}>
                <td>
                  <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                    <div className="user-avatar" style={{ width: 30, height: 30, fontSize: 12 }}>
                      {member.name[0]}
                    </div>
                    <span style={{ fontWeight: 500 }}>{member.name}</span>
                  </div>
                </td>
                <td style={{ color: "var(--muted)" }}>{member.email}</td>
                <td><span className={`badge badge-${member.role.toLowerCase()}`}>{member.role}</span></td>
                <td><span className={`badge badge-${member.active ? "active" : "inactive"}`}>{member.active ? "Active" : "Inactive"}</span></td>
                <td style={{ textAlign: "right" }}>
                  {member.active && (
                    <button className="btn btn-danger btn-sm" onClick={() => deactivate(member.id)}>
                      Deactivate
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {(!staff || staff.length === 0) && (
          <div className="empty-state">No staff members yet</div>
        )}
      </div>
      {showModal && (
        <AddStaffModal onClose={() => setShowModal(false)} onAdded={refetch} />
      )}
    </div>
  );
}
