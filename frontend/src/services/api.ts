import axios from 'axios';
import { AuthResponse, Cart, NotificationItem, Order, Product, Category } from '../types';

const API = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token if available
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth API
export const authApi = {
  login: async (credentials: { username: string; password: string }): Promise<AuthResponse> => {
    const { data } = await API.post<AuthResponse>('/api/auth/login', credentials);
    return data;
  },
  register: async (userData: { username: string; email: string; password: string; firstName?: string; lastName?: string }): Promise<AuthResponse> => {
    const { data } = await API.post<AuthResponse>('/api/auth/register', userData);
    return data;
  },
  me: async () => {
    const { data } = await API.get('/api/auth/me');
    return data;
  },
};

// Products API
export const productApi = {
  getAll: async (params?: { category?: string; search?: string; page?: number; size?: number }) => {
    const { data } = await API.get<{ content: Product[]; totalElements: number; totalPages: number }>('/api/products', { params });
    return data;
  },
  getById: async (id: string): Promise<Product> => {
    const { data } = await API.get<Product>(`/api/products/${id}`);
    return data;
  },
  getCategories: async (): Promise<Category[]> => {
    const { data } = await API.get<Category[]>('/api/categories');
    return data;
  },
};

// Cart API
export const cartApi = {
  getCart: async (): Promise<Cart> => {
    const { data } = await API.get<Cart>('/api/cart');
    return data;
  },
  addItem: async (item: { productId: string; productName: string; unitPrice: number; quantity: number }): Promise<Cart> => {
    const { data } = await API.post<Cart>('/api/cart/items', item);
    return data;
  },
  updateQuantity: async (productId: string, quantity: number): Promise<Cart> => {
    const { data } = await API.put<Cart>(`/api/cart/items/${productId}`, { quantity });
    return data;
  },
  removeItem: async (productId: string): Promise<Cart> => {
    const { data } = await API.delete<Cart>(`/api/cart/items/${productId}`);
    return data;
  },
  clearCart: async (): Promise<void> => {
    await API.delete('/api/cart');
  },
};

// Order API
export const orderApi = {
  createOrder: async (orderData: { shippingAddress: string; items: { productId: string; quantity: number }[] }, idempotencyKey?: string): Promise<Order> => {
    const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
    const { data } = await API.post<Order>('/api/orders', orderData, { headers });
    return data;
  },
  getUserOrders: async (): Promise<Order[]> => {
    const { data } = await API.get<Order[]>('/api/orders');
    return data;
  },
  getOrderById: async (id: string): Promise<Order> => {
    const { data } = await API.get<Order>(`/api/orders/${id}`);
    return data;
  },
  cancelOrder: async (id: string): Promise<Order> => {
    const { data } = await API.post<Order>(`/api/orders/${id}/cancel`);
    return data;
  },
};

// Notification API
export const notificationApi = {
  getNotifications: async (): Promise<NotificationItem[]> => {
    const { data } = await API.get<NotificationItem[]>('/api/notifications');
    return data;
  },
};

// Inventory API
export const inventoryApi = {
  getStock: async (productId: string) => {
    const { data } = await API.get(`/api/inventory/${productId}`);
    return data;
  },
};

export default API;
