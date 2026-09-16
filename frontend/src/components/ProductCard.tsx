import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Check } from 'lucide-react';
import { Product } from '../types';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

interface ProductCardProps {
  product: Product;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();
  const [added, setAdded] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) {
      window.location.href = '/login';
      return;
    }

    try {
      setLoading(true);
      await addToCart(product.id, product.name, product.price, 1);
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const isOutOfStock = product.availableStock !== undefined && product.availableStock <= 0;

  return (
    <Link to={`/products/${product.id}`} className="glass-card" style={{
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
      textDecoration: 'none'
    }}>
      {/* Product Image / Placeholder */}
      <div style={{
        height: '200px',
        background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(6, 182, 212, 0.1))',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        borderBottom: '1px solid var(--color-border)'
      }}>
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
        ) : (
          <div style={{
            fontSize: '3rem',
            fontWeight: 800,
            color: 'rgba(255, 255, 255, 0.15)',
            userSelect: 'none'
          }}>
            {product.name.slice(0, 2).toUpperCase()}
          </div>
        )}

        {/* Stock status badge */}
        <div style={{ position: 'absolute', top: '12px', right: '12px' }}>
          {isOutOfStock ? (
            <span className="badge badge-danger">Out of Stock</span>
          ) : product.availableStock !== undefined && product.availableStock < 5 ? (
            <span className="badge badge-warning">Only {product.availableStock} Left</span>
          ) : (
            <span className="badge badge-success">In Stock</span>
          )}
        </div>
      </div>

      {/* Content */}
      <div style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', flex: 1 }}>
        <div style={{
          fontSize: '0.75rem',
          color: 'var(--color-secondary)',
          fontWeight: 600,
          textTransform: 'uppercase',
          letterSpacing: '0.05em',
          marginBottom: '0.4rem'
        }}>
          {product.category?.name || 'General'}
        </div>

        <h3 style={{
          fontSize: '1.1rem',
          color: '#fff',
          marginBottom: '0.5rem',
          lineHeight: 1.3
        }}>
          {product.name}
        </h3>

        <p style={{
          color: 'var(--color-text-muted)',
          fontSize: '0.85rem',
          marginBottom: '1.25rem',
          flex: 1,
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden'
        }}>
          {product.description}
        </p>

        {/* Price & Action */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginTop: 'auto',
          paddingTop: '0.75rem',
          borderTop: '1px solid var(--color-border)'
        }}>
          <div>
            <span style={{ fontSize: '0.8rem', color: 'var(--color-text-dim)', display: 'block' }}>Price</span>
            <span style={{ fontSize: '1.35rem', fontWeight: 800, color: '#fff' }}>
              ${Number(product.price).toFixed(2)}
            </span>
          </div>

          <button
            onClick={handleAddToCart}
            disabled={isOutOfStock || loading}
            className={`btn ${added ? 'btn-secondary' : 'btn-primary'}`}
            style={{
              padding: '0.5rem 1rem',
              fontSize: '0.85rem',
              opacity: isOutOfStock ? 0.5 : 1,
              cursor: isOutOfStock ? 'not-allowed' : 'pointer'
            }}
          >
            {added ? (
              <>
                <Check size={16} color="var(--color-success)" />
                <span>Added</span>
              </>
            ) : (
              <>
                <ShoppingCart size={16} />
                <span>{loading ? 'Adding...' : 'Add'}</span>
              </>
            )}
          </button>
        </div>
      </div>
    </Link>
  );
};
