import React from 'react';
import { Activity, ShieldCheck, Zap } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer style={{
      borderTop: '1px solid var(--color-border)',
      background: 'rgba(11, 15, 25, 0.95)',
      padding: '3rem 0 2rem 0',
      marginTop: 'auto'
    }}>
      <div className="container">
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '2.5rem',
          marginBottom: '2.5rem'
        }}>
          <div>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '0.75rem', color: '#fff' }}>Go N Buy</h3>
            <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              Production-grade e-commerce microservices platform demonstrating modern cloud architecture,
              high-concurrency inventory reservation, and distributed event-driven order workflows.
            </p>
          </div>

          <div>
            <h4 style={{ fontSize: '0.9rem', color: 'var(--color-text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '1rem' }}>
              Architecture
            </h4>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.875rem', color: 'var(--color-text)' }}>
              <li>Spring Boot 3.x & Java 21</li>
              <li>RabbitMQ Event-Driven Messaging</li>
              <li>Redis Distributed Cart Cache</li>
              <li>PostgreSQL Microservice Databases</li>
            </ul>
          </div>

          <div>
            <h4 style={{ fontSize: '0.9rem', color: 'var(--color-text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '1rem' }}>
              Infrastructure
            </h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', color: 'var(--color-text)' }}>
                <Activity size={16} color="var(--color-secondary)" />
                <span>Kubernetes & Helm v3</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', color: 'var(--color-text)' }}>
                <ShieldCheck size={16} color="var(--color-success)" />
                <span>Zero-Trust IAM & Security Audits</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', color: 'var(--color-text)' }}>
                <Zap size={16} color="var(--color-warning)" />
                <span>Prometheus & Grafana Observability</span>
              </div>
            </div>
          </div>
        </div>

        <div style={{
          borderTop: '1px solid var(--color-border)',
          paddingTop: '1.5rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          fontSize: '0.8rem',
          color: 'var(--color-text-dim)'
        }}>
          <div>© {new Date().getFullYear()} ShopSphere / Go N Buy Platform. Licensed under MIT.</div>
          <div>Cloud-Native DevOps Capstone Project</div>
        </div>
      </div>
    </footer>
  );
};
