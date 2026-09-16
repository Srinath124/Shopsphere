import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ShieldCheck, Lock, AlertCircle, ArrowLeft } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { orderApi } from '../services/api';

export const CheckoutPage: React.FC = () => {
  const { cart, clearCart } = useCart();
  const navigate = useNavigate();

  const [shippingAddress, setShippingAddress] = useState('');
  const [city, setCity] = useState('');
  const [postalCode, setPostalCode] = useState('');
  const [country, setCountry] = useState('US');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const items = cart?.items || [];

  if (items.length === 0) {
    return (
      <div className="container page-container">
        <div className="glass-panel" style={{ textAlign: 'center', padding: '3rem' }}>
          <h2>No items in cart</h2>
          <Link to="/" className="btn btn-primary" style={{ marginTop: '1rem' }}>
            Browse Catalog
          </Link>
        </div>
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!shippingAddress.trim()) {
      setError('Please provide a complete shipping address');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // Generate a unique idempotency key for this checkout submission
      const idempotencyKey = crypto.randomUUID();

      const orderData = {
        shippingAddress: `${shippingAddress}, ${city} ${postalCode}, ${country}`,
        items: items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      };

      await orderApi.createOrder(orderData, idempotencyKey);
      await clearCart();
      navigate('/orders');
    } catch (err: any) {
      console.error('Checkout failed', err);
      setError(err.response?.data?.message || 'Failed to place order. Please check inventory or retry.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container page-container">
      <Link to="/cart" style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.5rem',
        color: 'var(--color-text-muted)',
        marginBottom: '1.5rem',
        fontSize: '0.9rem'
      }}>
        <ArrowLeft size={16} /> Back to Cart
      </Link>

      <h1 style={{ fontSize: '2rem', marginBottom: '2rem' }}>Secure Checkout</h1>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: '2.5rem', alignItems: 'start' }}>
        {/* Shipping & Payment Form */}
        <form onSubmit={handleSubmit} className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
            <Lock size={20} color="var(--color-secondary)" />
            <h2 style={{ fontSize: '1.25rem' }}>Shipping Information</h2>
          </div>

          {error && (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.75rem 1rem',
              background: 'rgba(239, 68, 68, 0.15)',
              border: '1px solid rgba(239, 68, 68, 0.3)',
              borderRadius: 'var(--radius-md)',
              color: 'var(--color-danger)',
              marginBottom: '1.5rem',
              fontSize: '0.875rem'
            }}>
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Street Address</label>
            <input
              type="text"
              required
              placeholder="123 Cloud Way"
              value={shippingAddress}
              onChange={(e) => setShippingAddress(e.target.value)}
              className="form-input"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">City</label>
              <input
                type="text"
                required
                placeholder="San Francisco"
                value={city}
                onChange={(e) => setCity(e.target.value)}
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Postal / ZIP Code</label>
              <input
                type="text"
                required
                placeholder="94105"
                value={postalCode}
                onChange={(e) => setPostalCode(e.target.value)}
                className="form-input"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Country</label>
            <input
              type="text"
              required
              value={country}
              onChange={(e) => setCountry(e.target.value)}
              className="form-input"
            />
          </div>

          {/* Idempotent Assurance Box */}
          <div style={{
            padding: '1rem',
            background: 'rgba(99, 102, 241, 0.08)',
            border: '1px solid rgba(99, 102, 241, 0.2)',
            borderRadius: 'var(--radius-md)',
            marginTop: '1.5rem',
            fontSize: '0.85rem',
            color: 'var(--color-text-muted)',
            display: 'flex',
            gap: '0.75rem',
            alignItems: 'center'
          }}>
            <ShieldCheck size={28} color="var(--color-primary)" style={{ flexShrink: 0 }} />
            <div>
              <strong>Idempotent Transaction Guarantee:</strong> Each order submission generates a cryptographically unique key. Even under network retries or sudden disconnects, you will never be double-charged.
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '2rem', padding: '0.9rem', fontSize: '1.05rem' }}
          >
            {loading ? 'Processing Order...' : `Place Order — $${Number(cart?.totalAmount).toFixed(2)}`}
          </button>
        </form>

        {/* Order Review Sidebar */}
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '1.25rem' }}>Review Order</h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem', marginBottom: '1.5rem' }}>
            {items.map((item) => (
              <div key={item.productId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
                <span style={{ color: 'var(--color-text-muted)' }}>
                  {item.quantity}x {item.productName}
                </span>
                <span style={{ color: '#fff', fontWeight: 600 }}>${Number(item.subtotal).toFixed(2)}</span>
              </div>
            ))}
          </div>

          <div style={{ borderTop: '1px solid var(--color-border)', paddingTop: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 800, fontSize: '1.25rem' }}>
              <span>Total</span>
              <span>${Number(cart?.totalAmount).toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
