import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Package, Clock, CheckCircle2, XCircle, Truck } from 'lucide-react';
import { Order } from '../types';
import { orderApi } from '../services/api';

export const OrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [cancellingId, setCancellingId] = useState<string | null>(null);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const data = await orderApi.getUserOrders();
      setOrders(data);
    } catch (err) {
      console.error('Failed to load orders', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelOrder = async (orderId: string) => {
    if (!window.confirm('Are you sure you want to cancel this order? Stock will be released back to inventory.')) {
      return;
    }

    try {
      setCancellingId(orderId);
      await orderApi.cancelOrder(orderId);
      await loadOrders();
    } catch (err) {
      console.error('Failed to cancel order', err);
      alert('Unable to cancel order.');
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
        return (
          <span className="badge badge-success">
            <CheckCircle2 size={14} /> Confirmed
          </span>
        );
      case 'PENDING':
        return (
          <span className="badge badge-warning">
            <Clock size={14} /> Processing
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="badge badge-danger">
            <XCircle size={14} /> Cancelled
          </span>
        );
      case 'SHIPPED':
      case 'DELIVERED':
        return (
          <span className="badge badge-info">
            <Truck size={14} /> {status}
          </span>
        );
      default:
        return <span className="badge badge-info">{status}</span>;
    }
  };

  if (loading) {
    return (
      <div className="container page-container" style={{ textAlign: 'center', padding: '5rem 0' }}>
        Loading your order history...
      </div>
    );
  }

  return (
    <div className="container page-container">
      <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Your Orders</h1>
      <p style={{ color: 'var(--color-text-muted)', marginBottom: '2.5rem' }}>
        Track distributed order lifecycle, inventory reservation statuses, and event logs.
      </p>

      {orders.length === 0 ? (
        <div className="glass-panel" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <div style={{
            display: 'inline-flex',
            padding: '1.25rem',
            borderRadius: 'var(--radius-full)',
            background: 'rgba(99, 102, 241, 0.1)',
            marginBottom: '1.5rem'
          }}>
            <Package size={48} color="var(--color-primary)" />
          </div>
          <h2 style={{ marginBottom: '0.5rem' }}>No Orders Found</h2>
          <p style={{ color: 'var(--color-text-muted)', marginBottom: '1.5rem' }}>
            You haven't placed any orders yet.
          </p>
          <Link to="/" className="btn btn-primary">
            Start Shopping
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {orders.map((order) => (
            <div key={order.id} className="glass-panel" style={{ padding: '1.75rem' }}>
              {/* Order Header */}
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start',
                flexWrap: 'wrap',
                gap: '1rem',
                paddingBottom: '1.25rem',
                borderBottom: '1px solid var(--color-border)'
              }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.25rem' }}>
                    <span style={{ fontSize: '1.15rem', fontWeight: 700, color: '#fff' }}>
                      Order #{order.id.slice(0, 8)}
                    </span>
                    {getStatusBadge(order.status)}
                  </div>
                  <span style={{ fontSize: '0.825rem', color: 'var(--color-text-dim)' }}>
                    Placed on {new Date(order.createdAt).toLocaleString()}
                  </span>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)', display: 'block' }}>
                    Total Amount
                  </span>
                  <span style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff' }}>
                    ${Number(order.totalAmount).toFixed(2)}
                  </span>
                </div>
              </div>

              {/* Order Items */}
              <div style={{ padding: '1.25rem 0', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {order.items.map((item) => (
                  <div key={item.id || item.productId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
                    <div>
                      <span style={{ color: '#fff', fontWeight: 500 }}>{item.productName}</span>
                      <span style={{ color: 'var(--color-text-dim)', marginLeft: '0.5rem' }}>x{item.quantity}</span>
                    </div>
                    <span style={{ color: 'var(--color-text-muted)' }}>
                      ${Number(item.subtotal).toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>

              {/* Shipping & Actions */}
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '1rem',
                paddingTop: '1rem',
                borderTop: '1px solid var(--color-border)',
                fontSize: '0.85rem'
              }}>
                <div style={{ color: 'var(--color-text-muted)' }}>
                  <strong>Shipping to:</strong> {order.shippingAddress}
                </div>

                {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
                  <button
                    onClick={() => handleCancelOrder(order.id)}
                    disabled={cancellingId === order.id}
                    className="btn btn-danger"
                    style={{ fontSize: '0.825rem', padding: '0.4rem 0.85rem' }}
                  >
                    {cancellingId === order.id ? 'Cancelling...' : 'Cancel Order'}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
