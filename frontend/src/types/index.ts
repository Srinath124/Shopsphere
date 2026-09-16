export interface User {
  id?: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'USER' | 'ADMIN';
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  tokenType: string;
  username: string;
  role: string;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
}

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  sku: string;
  category?: Category;
  imageUrl?: string;
  availableStock?: number;
}

export interface CartItem {
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Cart {
  userId: string;
  items: CartItem[];
  totalAmount: number;
  itemCount: number;
}

export interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: string;
  userId: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'SHIPPED' | 'DELIVERED';
  totalAmount: number;
  shippingAddress: string;
  idempotencyKey?: string;
  items: OrderItem[];
  createdAt: string;
}

export interface NotificationItem {
  id: string;
  userId: string;
  orderId?: string;
  eventType: string;
  channel: string;
  recipient: string;
  subject: string;
  body: string;
  status: 'SENT' | 'FAILED' | 'SIMULATED';
  createdAt: string;
}

export interface InventoryStock {
  productId: string;
  sku: string;
  availableStock: number;
  reservedStock: number;
}
