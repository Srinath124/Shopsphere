import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, ShoppingCart, ShieldCheck, Truck, Check } from 'lucide-react';
import { Product, InventoryStock } from '../types';
import { productApi, inventoryApi } from '../services/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();

  const [product, setProduct] = useState<Product | null>(null);
  const [stock, setStock] = useState<InventoryStock | null>(null);
  const [quantity, setQuantity] = useState<number>(1);
  const [loading, setLoading] = useState<boolean>(true);
  const [adding, setAdding] = useState<boolean>(false);
  const [added, setAdded] = useState<boolean>(false);

  useEffect(() => {
    if (id) {
      loadProductAndStock(id);
    }
  }, [id]);

  const loadProductAndStock = async (productId: string) => {
    try {
      setLoading(true);
      const prodData = await productApi.getById(productId);
      setProduct(prodData);

      try {
        const stockData = await inventoryApi.getStock(productId);
        setStock(stockData);
      } catch (stockErr) {
        console.warn('Could not fetch real-time inventory for product', stockErr);
      }
    } catch (err) {
      console.error('Failed to load product', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!product) return;
    if (!isAuthenticated) {
      window.location.href = '/login';
      return;
    }

    try {
      setAdding(true);
      await addToCart(product.id, product.name, product.price, quantity);
      setAdded(true);
      setTimeout(() => setAdded(false), 2500);
    } catch (err) {
      console.error(err);
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="container page-container" style={{ textAlign: 'center', padding: '5rem 0' }}>
        Loading product details...
      </div>
    );
  }

  if (!product) {
    return (
      <div className="container page-container">
        <div className="glass-panel" style={{ textAlign: 'center', padding: '3rem' }}>
          <h2>Product Not Found</h2>
          <Link to="/" className="btn btn-primary" style={{ marginTop: '1rem' }}>
            Back to Catalog
          </Link>
        </div>
      </div>
    );
  }

  const available = stock ? stock.availableStock : (product.availableStock ?? 10);
  const isOutOfStock = available <= 0;

  return (
    <div className="container page-container">
      <Link to="/" style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.5rem',
        color: 'var(--color-text-muted)',
        marginBottom: '1.5rem',
        fontSize: '0.9rem'
      }}>
        <ArrowLeft size={16} /> Back to Catalog
      </Link>

      <div className="glass-panel" style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
        gap: '3rem',
        padding: '2.5rem',
        overflow: 'hidden'
      }}>
        {/* Product Visual */}
        <div style={{
          height: '420px',
          background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(6, 182, 212, 0.1))',
          borderRadius: 'var(--radius-md)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          border: '1px solid var(--color-border)'
        }}>
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          ) : (
            <span style={{ fontSize: '5rem', fontWeight: 800, color: 'rgba(255, 255, 255, 0.15)' }}>
              {product.name.slice(0, 2).toUpperCase()}
            </span>
          )}
        </div>

        {/* Product Details & Actions */}
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <div style={{
            fontSize: '0.85rem',
            color: 'var(--color-secondary)',
            fontWeight: 600,
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
            marginBottom: '0.5rem'
          }}>
            {product.category?.name || 'Category'}
          </div>

          <h1 style={{ fontSize: '2rem', marginBottom: '0.75rem', lineHeight: 1.25 }}>
            {product.name}
          </h1>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
            <span style={{ fontSize: '2rem', fontWeight: 800, color: '#fff' }}>
              ${Number(product.price).toFixed(2)}
            </span>

            {/* Live Stock Badge */}
            {isOutOfStock ? (
              <span className="badge badge-danger">Out of Stock</span>
            ) : available < 5 ? (
              <span className="badge badge-warning">Low Stock ({available} available)</span>
            ) : (
              <span className="badge badge-success">In Stock ({available} units)</span>
            )}
          </div>

          <div style={{
            color: 'var(--color-text-muted)',
            fontSize: '0.95rem',
            lineHeight: 1.7,
            marginBottom: '2rem',
            flex: 1
          }}>
            {product.description}
          </div>

          {/* SKU & Metadata */}
          <div style={{
            padding: '1rem',
            background: 'rgba(0,0,0,0.2)',
            borderRadius: 'var(--radius-sm)',
            fontSize: '0.85rem',
            color: 'var(--color-text-dim)',
            marginBottom: '2rem'
          }}>
            <div><strong>SKU:</strong> {product.sku}</div>
            {stock && <div><strong>Reserved in Active Carts:</strong> {stock.reservedStock}</div>}
          </div>

          {/* Quantity and Add to Cart Button */}
          <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', marginBottom: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-md)' }}>
              <button
                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                style={{ padding: '0.75rem 1rem', color: '#fff', fontSize: '1.1rem' }}
                disabled={quantity <= 1 || isOutOfStock}
              >
                -
              </button>
              <span style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>{quantity}</span>
              <button
                onClick={() => setQuantity(Math.min(available, quantity + 1))}
                style={{ padding: '0.75rem 1rem', color: '#fff', fontSize: '1.1rem' }}
                disabled={quantity >= available || isOutOfStock}
              >
                +
              </button>
            </div>

            <button
              onClick={handleAddToCart}
              disabled={isOutOfStock || adding}
              className={`btn ${added ? 'btn-secondary' : 'btn-primary'}`}
              style={{ flex: 1, padding: '0.85rem 1.5rem', fontSize: '1rem' }}
            >
              {added ? (
                <>
                  <Check size={20} color="var(--color-success)" />
                  <span>Added to Cart</span>
                </>
              ) : (
                <>
                  <ShoppingCart size={20} />
                  <span>{adding ? 'Adding...' : 'Add to Cart'}</span>
                </>
              )}
            </button>
          </div>

          {/* Value Props */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '1rem',
            borderTop: '1px solid var(--color-border)',
            paddingTop: '1.5rem'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
              <Truck size={18} color="var(--color-secondary)" />
              <span>Instant Fulfillment</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
              <ShieldCheck size={18} color="var(--color-success)" />
              <span>Idempotent Checkout</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
