import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingBag, ShoppingCart, Bell, User, LogOut, Package } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export const Navbar: React.FC = () => {
  const { isAuthenticated, username, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const totalItems = cart?.itemCount || 0;

  return (
    <header style={{
      background: 'rgba(11, 15, 25, 0.85)',
      backdropFilter: 'blur(16px)',
      borderBottom: '1px solid var(--color-border)',
      position: 'sticky',
      top: 0,
      zIndex: 50
    }}>
      <div className="container" style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '4.5rem'
      }}>
        {/* Brand Logo */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            background: 'linear-gradient(135deg, var(--color-primary), var(--color-accent))',
            padding: '0.5rem',
            borderRadius: 'var(--radius-md)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 15px var(--color-primary-glow)'
          }}>
            <ShoppingBag size={24} color="#fff" />
          </div>
          <div>
            <span style={{ fontSize: '1.25rem', fontWeight: 800, color: '#fff', letterSpacing: '-0.03em' }}>Go N Buy</span>
            <span style={{ fontSize: '0.75rem', display: 'block', color: 'var(--color-secondary)', fontWeight: 600, marginTop: '-3px' }}>ShopSphere Cloud</span>
          </div>
        </Link>

        {/* Navigation Links */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          <Link to="/" style={{ color: 'var(--color-text)', fontWeight: 500, fontSize: '0.95rem' }}>
            Catalog
          </Link>
          {isAuthenticated && (
            <>
              <Link to="/orders" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--color-text)', fontWeight: 500, fontSize: '0.95rem' }}>
                <Package size={18} color="var(--color-text-muted)" />
                Orders
              </Link>
              <Link to="/notifications" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--color-text)', fontWeight: 500, fontSize: '0.95rem' }}>
                <Bell size={18} color="var(--color-text-muted)" />
                Alerts
              </Link>
            </>
          )}
        </nav>

        {/* Actions (Cart & Auth) */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Link to="/cart" className="btn btn-secondary" style={{ position: 'relative', padding: '0.5rem 0.85rem' }}>
            <ShoppingCart size={20} />
            {totalItems > 0 && (
              <span style={{
                position: 'absolute',
                top: '-5px',
                right: '-5px',
                background: 'var(--color-primary)',
                color: '#fff',
                borderRadius: 'var(--radius-full)',
                padding: '2px 7px',
                fontSize: '0.75rem',
                fontWeight: 700
              }}>
                {totalItems}
              </span>
            )}
          </Link>

          {isAuthenticated ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                padding: '0.4rem 0.8rem',
                background: 'rgba(255, 255, 255, 0.05)',
                borderRadius: 'var(--radius-full)',
                border: '1px solid var(--color-border)'
              }}>
                <User size={16} color="var(--color-secondary)" />
                <span style={{ fontSize: '0.875rem', fontWeight: 600 }}>{username}</span>
              </div>
              <button onClick={handleLogout} className="btn btn-secondary" title="Sign Out" style={{ padding: '0.5rem' }}>
                <LogOut size={18} />
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Link to="/login" className="btn btn-secondary">Sign In</Link>
              <Link to="/register" className="btn btn-primary">Sign Up</Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
