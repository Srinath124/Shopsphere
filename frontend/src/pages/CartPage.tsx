import React from 'react';
import { Link } from 'react-router-dom';
import { Trash2, ArrowRight, ShoppingBag } from 'lucide-react';
import { useCart } from '../context/CartContext';

export const CartPage: React.FC = () => {
  const { cart, loading, updateQuantity, removeFromCart, clearCart } = useCart();

  if (loading && !cart) {
    return (
      <div className="container page-container" style={{ textAlign: 'center', padding: '5rem 0' }}>
        Loading your cart...
      </div>
    );
  }

  const items = cart?.items || [];

  if (items.length === 0) {
    return (
      <div className="container page-container">
        <div className="glass-panel" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <div style={{
            display: 'inline-flex',
            padding: '1.25rem',
            borderRadius: 'var(--radius-full)',
            background: 'rgba(99, 102, 241, 0.1)',
            marginBottom: '1.5rem'
          }}>
            <ShoppingBag size={48} color="var(--color-primary)" />
          </div>
          <h2 style={{ marginBottom: '0.5rem' }}>Your Cart is Empty</h2>
          <p style={{ color: 'var(--color-text-muted)', marginBottom: '1.75rem' }}>
            Explore our catalog to find items you want to purchase.
          </p>
          <Link to="/" className="btn btn-primary">
            Start Shopping
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem' }}>Shopping Cart</h1>
        <button onClick={clearCart} className="btn btn-secondary" style={{ fontSize: '0.85rem' }}>
          Clear Cart
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: '2rem', alignItems: 'start' }}>
        {/* Cart Items List */}
        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            {items.map((item) => (
              <div
                key={item.productId}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  paddingBottom: '1.25rem',
                  borderBottom: '1px solid var(--color-border)',
                  gap: '1rem'
                }}
              >
                <div style={{ flex: 1 }}>
                  <Link
                    to={`/products/${item.productId}`}
                    style={{ fontSize: '1.05rem', fontWeight: 600, color: '#fff', display: 'block', marginBottom: '0.25rem' }}
                  >
                    {item.productName}
                  </Link>
                  <span style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>
                    ${Number(item.unitPrice).toFixed(2)} each
                  </span>
                </div>

                {/* Quantity Controls */}
                <div style={{ display: 'flex', alignItems: 'center', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-md)' }}>
                  <button
                    onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                    style={{ padding: '0.4rem 0.8rem', color: '#fff' }}
                    disabled={item.quantity <= 1}
                  >
                    -
                  </button>
                  <span style={{ padding: '0.4rem 0.8rem', fontSize: '0.9rem', fontWeight: 600 }}>
                    {item.quantity}
                  </span>
                  <button
                    onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                    style={{ padding: '0.4rem 0.8rem', color: '#fff' }}
                  >
                    +
                  </button>
                </div>

                {/* Subtotal */}
                <div style={{ width: '100px', textAlign: 'right', fontWeight: 700, fontSize: '1.1rem' }}>
                  ${Number(item.subtotal).toFixed(2)}
                </div>

                {/* Remove */}
                <button
                  onClick={() => removeFromCart(item.productId)}
                  className="btn btn-secondary"
                  style={{ padding: '0.5rem', color: 'var(--color-danger)' }}
                  title="Remove item"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Order Summary Sidebar */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '1.25rem' }}>Order Summary</h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.5rem', fontSize: '0.95rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--color-text-muted)' }}>
              <span>Subtotal ({cart?.itemCount} items)</span>
              <span style={{ color: '#fff' }}>${Number(cart?.totalAmount).toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--color-text-muted)' }}>
              <span>Shipping</span>
              <span style={{ color: 'var(--color-success)' }}>Free</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--color-text-muted)' }}>
              <span>Tax (calculated)</span>
              <span style={{ color: '#fff' }}>$0.00</span>
            </div>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              fontWeight: 800,
              fontSize: '1.3rem',
              color: '#fff',
              borderTop: '1px solid var(--color-border)',
              paddingTop: '1rem',
              marginTop: '0.5rem'
            }}>
              <span>Total</span>
              <span>${Number(cart?.totalAmount).toFixed(2)}</span>
            </div>
          </div>

          <Link to="/checkout" className="btn btn-primary" style={{ width: '100%', padding: '0.85rem' }}>
            <span>Proceed to Checkout</span>
            <ArrowRight size={18} />
          </Link>
        </div>
      </div>
    </div>
  );
};
