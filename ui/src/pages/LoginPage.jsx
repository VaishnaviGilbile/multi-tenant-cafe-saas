import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { loginApi, registerTenantApi } from "../api/api";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate  = useNavigate();
  const [tab, setTab]           = useState("login");
  const [loading, setLoading]   = useState(false);

  // Login state
  const [tenantId,  setTenantId]  = useState("");
  const [email,     setEmail]     = useState("");
  const [password,  setPassword]  = useState("");

  // Register state
  const [reg, setReg] = useState({
    tenantId: "", tenantName: "", subdomain: "",
    adminName: "", adminEmail: "", adminPassword: ""
  });
  const setRegField = (k, v) => setReg(p => ({ ...p, [k]: v }));

  const handleLogin = async () => {
    if (!tenantId || !email || !password) { toast.error("All fields required"); return; }
    setLoading(true);
    try {
      const { data } = await loginApi(tenantId, email, password);
      login({ ...data, tenantId });
      navigate("/dashboard");
    } catch (e) {
      toast.error(e.response?.data?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    setLoading(true);
    try {
      const { data } = await registerTenantApi(reg);
      login({ ...data, tenantId: reg.tenantId });
      toast.success("Cafe registered!");
      navigate("/dashboard");
    } catch (e) {
      toast.error(e.response?.data?.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">☕ Cafe SaaS</div>
        <div className="login-subtitle">Multi-tenant cafe management platform</div>

        <div className="login-tabs">
          <button className={`login-tab ${tab === "login" ? "active" : ""}`} onClick={() => setTab("login")}>Sign In</button>
          <button className={`login-tab ${tab === "register" ? "active" : ""}`} onClick={() => setTab("register")}>New Cafe</button>
        </div>

        {tab === "login" ? (
          <>
            <div className="form-group">
              <label className="form-label">Tenant ID</label>
              <input className="form-input" placeholder="your-cafe-id" value={tenantId} onChange={e => setTenantId(e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input className="form-input" type="email" placeholder="you@cafe.com" value={email} onChange={e => setEmail(e.target.value)} />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="form-input" type="password" value={password} onChange={e => setPassword(e.target.value)}
                onKeyDown={e => e.key === "Enter" && handleLogin()} />
            </div>
            <button className="btn btn-primary" style={{ width: "100%", justifyContent: "center", padding: "11px" }}
              onClick={handleLogin} disabled={loading}>
              {loading ? <span className="spinner" /> : "Sign In"}
            </button>
          </>
        ) : (
          <>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Tenant ID</label>
                <input className="form-input" placeholder="blue-tokai-mumbai" value={reg.tenantId} onChange={e => setRegField("tenantId", e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Cafe Name</label>
                <input className="form-input" placeholder="Blue Tokai Mumbai" value={reg.tenantName} onChange={e => setRegField("tenantName", e.target.value)} />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Subdomain</label>
              <input className="form-input" placeholder="blue-tokai-mumbai" value={reg.subdomain} onChange={e => setRegField("subdomain", e.target.value)} />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Admin Name</label>
                <input className="form-input" value={reg.adminName} onChange={e => setRegField("adminName", e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Admin Email</label>
                <input className="form-input" type="email" value={reg.adminEmail} onChange={e => setRegField("adminEmail", e.target.value)} />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="form-input" type="password" placeholder="Min 8 characters" value={reg.adminPassword} onChange={e => setRegField("adminPassword", e.target.value)} />
            </div>
            <button className="btn btn-primary" style={{ width: "100%", justifyContent: "center", padding: "11px" }}
              onClick={handleRegister} disabled={loading}>
              {loading ? <span className="spinner" /> : "Register Cafe"}
            </button>
          </>
        )}
      </div>
    </div>
  );
}
