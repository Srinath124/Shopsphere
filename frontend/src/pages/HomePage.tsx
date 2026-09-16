import React, { useState, useEffect } from 'react';
import { Search, Filter, Sparkles, Layers, Cpu, ShieldAlert } from 'lucide-react';
import { ProductCard } from '../components/ProductCard';
import { Product, Category } from '../types';
import { productApi } from '../services/api';

export const HomePage: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    loadProducts();
  }, [selectedCategory]);

  const loadCategories = async () => {
    try {
      const data = await productApi.getCategories();
      setCategories(data);
    } catch (err) {
      console.error('Failed to load categories', err);
    }
  };

  const loadProducts = async () => {
    try {
      setLoading(true);
      const res = await productApi.getAll({
        category: selectedCategory || undefined,
        search: searchQuery || undefined,
      });
      setProducts(res.content || []);
    } catch (err) {
      console.error('Failed to load products', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadProducts();
  };

  return (
    <div className="page-container">
      {/* Hero Section */}
      <div className="container" style={{ marginBottom: '3.5rem' }}>
        <div className="glass-panel" style={{
          padding: '4rem 2.5rem',
          position: 'relative',
          overflow: 'hidden',
          background: 'linear-gradient(135deg, rgba(30, 27, 75, 0.7), rgba(15, 23, 42, 0.8))',
          border: '1px solid rgba(99, 102, 241, 0.2)'
        }}>
          <div style={{ maxWidth: '680px', position: 'relative', zIndex: 2 }}>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.5rem',
              padding: '0.35rem 0.85rem',
              borderRadius: 'var(--radius-full)',
              background: 'rgba(99, 102, 241, 0.15)',
              border: '1px solid rgba(99, 102, 241, 0.3)',
              color: '#a5b4fc',
              fontSize: '0.825rem',
              fontWeight: 600,
              marginBottom: '1.25rem'
            }}>
              <Sparkles size={16} /> Cloud-Native Microservices Architecture
            </div>

            <h1 style={{
              fontSize: '2.75rem',
              lineHeight: 1.15,
              marginBottom: '1.25rem',
              letterSpacing: '-0.03em'
            }}>
              High-Velocity E-Commerce Engineered for Scale.
            </h1>

            <p style={{
              fontSize: '1.05rem',
              color: 'var(--color-text-muted)',
              marginBottom: '2rem',
              lineHeight: 1.6
            }}>
              Built on 6 autonomous Spring Boot 3 microservices with Redis distributed cache,
              RabbitMQ event-driven order orchestration, and zero-loss pessimistic inventory locking.
            </p>

            {/* Architecture Highlights Pill Row */}
            <div style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                <Layers size={18} color="var(--color-secondary)" />
                <span>6 Microservices</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                <Cpu size={18} color="var(--color-primary)" />
                <span>RabbitMQ Event Mesh</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                <ShieldAlert size={18} color="var(--color-success)" />
                <span>Pessimistic Concurrency</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Product Catalog Section */}
      <div className="container">
        {/* Search & Filter Bar */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '1.25rem',
          flexWrap: 'wrap',
          marginBottom: '2rem'
        }}>
          {/* Category Tabs */}
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            <button
              onClick={() => setSelectedCategory('')}
              className={`btn ${selectedCategory === '' ? 'btn-primary' : 'btn-secondary'}`}
              style={{ fontSize: '0.85rem', padding: '0.45rem 0.95rem' }}
            >
              All Categories
            </button>
            {categories.map((cat) => (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat.id)}
                className={`btn ${selectedCategory === cat.id ? 'btn-primary' : 'btn-secondary'}`}
                style={{ fontSize: '0.85rem', padding: '0.45rem 0.95rem' }}
              >
                {cat.name}
              </button>
            ))}
          </div>

          {/* Search Input */}
          <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.5rem', minWidth: '280px' }}>
            <div style={{ position: 'relative', width: '100%' }}>
              <input
                type="text"
                placeholder="Search products..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="form-input"
                style={{ paddingLeft: '2.5rem' }}
              />
              <Search
                size={18}
                color="var(--color-text-dim)"
                style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)' }}
              />
            </div>
            <button type="submit" className="btn btn-secondary">
              <Filter size={18} />
            </button>
          </form>
        </div>

        {/* Product Grid */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '5rem 0', color: 'var(--color-text-muted)' }}>
            Loading catalog...
          </div>
        ) : products.length === 0 ? (
          <div className="glass-panel" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
            <h3 style={{ marginBottom: '0.5rem' }}>No products found</h3>
            <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
              Try adjusting your search or category filter.
            </p>
          </div>
        ) : (
          <div className="grid-cols-4">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
