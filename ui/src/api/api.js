import axios from "axios";

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || "",
});

// Attach JWT + X-Tenant-ID on every request
api.interceptors.request.use((config) => {
  const stored = localStorage.getItem("cafe_auth");
  if (stored) {
    const { token, tenantId } = JSON.parse(stored);
    config.headers["Authorization"] = `Bearer ${token}`;
    config.headers["X-Tenant-ID"] = tenantId;
  }
  return config;
});

// Auth
export const loginApi = (tenantId, email, password) =>
  api.post("/api/auth/login", { email, password }, {
    headers: { "X-Tenant-ID": tenantId },
  });

export const registerTenantApi = (data) =>
  api.post("/api/auth/register-tenant", data);

// Menu
export const getMenuApi = () => api.get("/api/menu");
export const createMenuItemApi = (data) => api.post("/api/menu", data);
export const updateMenuItemApi = (id, data) => api.put(`/api/menu/${id}`, data);
export const toggleAvailabilityApi = (id) => api.patch(`/api/menu/${id}/toggle-availability`);
export const deleteMenuItemApi = (id) => api.delete(`/api/menu/${id}`);

// Orders
export const getActiveOrdersApi = () => api.get("/api/orders/active");
export const createOrderApi = (data) => api.post("/api/orders", data);
export const updateOrderStatusApi = (id, status) =>
  api.patch(`/api/orders/${id}/status`, { status });
export const getOrderHistoryApi = (start, end, page = 0) =>
  api.get("/api/orders/history", { params: { start, end, page, size: 20 } });

// Staff
export const getStaffApi = () => api.get("/api/staff");
export const createStaffApi = (data) => api.post("/api/staff", data);
export const deactivateStaffApi = (id) => api.delete(`/api/staff/${id}`);

// Analytics
export const getDashboardApi = () => api.get("/api/analytics/dashboard");

export default api;
