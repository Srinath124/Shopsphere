import React, { createContext, useContext, useState, useEffect } from 'react';
import { Cart } from '../types';
import { cartApi } from '../services/api';
import { useAuth } from './AuthContext';

interface CartContextType {
  cart: Cart | null;
  loading: boolean;
  refreshCart: () => Promise<void>;
  addToCart: (productId: string, productName: string, unitPrice: number, quantity?: number) => Promise<void>;
  updateQuantity: (productId: string, quantity: number) => Promise<void>;
  removeFromCart: (productId: string) => Promise<void>;
  clearCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  const refreshCart = async () => {
    if (!isAuthenticated) {
      setCart(null);
      return;
    }
    try {
      setLoading(true);
      const data = await cartApi.getCart();
      setCart(data);
    } catch (err) {
      console.error('Failed to fetch cart', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshCart();
  }, [isAuthenticated]);

  const addToCart = async (productId: string, productName: string, unitPrice: number, quantity = 1) => {
    if (!isAuthenticated) return;
    try {
      const updated = await cartApi.addItem({ productId, productName, unitPrice, quantity });
      setCart(updated);
    } catch (err) {
      console.error('Failed to add item to cart', err);
      throw err;
    }
  };

  const updateQuantity = async (productId: string, quantity: number) => {
    if (!isAuthenticated) return;
    try {
      const updated = await cartApi.updateQuantity(productId, quantity);
      setCart(updated);
    } catch (err) {
      console.error('Failed to update cart quantity', err);
      throw err;
    }
  };

  const removeFromCart = async (productId: string) => {
    if (!isAuthenticated) return;
    try {
      const updated = await cartApi.removeItem(productId);
      setCart(updated);
    } catch (err) {
      console.error('Failed to remove item from cart', err);
      throw err;
    }
  };

  const clearCart = async () => {
    if (!isAuthenticated) return;
    try {
      await cartApi.clearCart();
      setCart({ userId: '', items: [], totalAmount: 0, itemCount: 0 });
    } catch (err) {
      console.error('Failed to clear cart', err);
      throw err;
    }
  };

  return (
    <CartContext.Provider
      value={{
        cart,
        loading,
        refreshCart,
        addToCart,
        updateQuantity,
        removeFromCart,
        clearCart,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};
