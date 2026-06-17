import React from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const NAV = [
  { to: "/dashboard", label: "Dashboard",    icon: "▦" },
  { to: "/orders",    label: "Live Orders",  icon: "📋" },
  { to: "/menu",      label: "Menu",         icon: "☰" },
  { to: "/staff",     label: "Staff",        icon: "👥" },
];

const PAGE_TITLES = {
  "/dashboard": "Dashboard",
  "/orders":    "Live Orders",
  "/menu":      "Menu Management",
  "/staff":     "Staff Management",
};

export default function Layout() {
  const { auth, logout } = useAuth();
  const navigate = useNavigate();
  const path = window.location.pathname;

  const handleLogout = () => { logout(); navigate("/login"); };

  return (
    <div className="app">
      <div className="sidebar">
        <div className="sidebar-logo">
          <div className="logo-mark">☕ Cafe SaaS</div>
          <div className="tenant-pill">{auth?.tenantId}</div>
        </div>
        <nav className="sidebar-nav">
          {NAV.map(n => (
            <NavLink key={n.to} to={n.to}
              className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}>
              <span style={{ fontSize: 14 }}>{n.icon}</span>
              {n.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="user-row">
            <div className="user-avatar">{(auth?.name || "A")[0]}</div>
            <div className="user-info">
              <div className="user-name">{auth?.name || "Admin"}</div>
              <div className="user-role">{auth?.role}</div>
            </div>
            <button className="logout-btn" onClick={handleLogout} title="Logout">⇥</button>
          </div>
        </div>
      </div>

      <div className="main">
        <div className="topbar">
          <div className="page-title">{PAGE_TITLES[path] || "Cafe SaaS"}</div>
          <div className="topbar-actions">
            <span style={{ fontSize: 12, color: "var(--muted)" }}>{auth?.tenantId}</span>
            <div style={{ width: 8, height: 8, borderRadius: "50%", background: "var(--green)" }} />
          </div>
        </div>
        <div className="content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
