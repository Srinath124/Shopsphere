import React, { useState, useEffect } from 'react';
import { Bell, Mail } from 'lucide-react';
import { NotificationItem } from '../types';
import { notificationApi } from '../services/api';

export const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const data = await notificationApi.getNotifications();
      setNotifications(data);
    } catch (err) {
      console.error('Failed to load notifications', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container page-container" style={{ textAlign: 'center', padding: '5rem 0' }}>
        Loading notifications...
      </div>
    );
  }

  return (
    <div className="container page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>Notification Feed</h1>
          <p style={{ color: 'var(--color-text-muted)' }}>
            Real-time messages dispatched across RabbitMQ topic exchange to notification-service.
          </p>
        </div>
        <button onClick={loadNotifications} className="btn btn-secondary" style={{ fontSize: '0.85rem' }}>
          Refresh
        </button>
      </div>

      {notifications.length === 0 ? (
        <div className="glass-panel" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <div style={{
            display: 'inline-flex',
            padding: '1.25rem',
            borderRadius: 'var(--radius-full)',
            background: 'rgba(99, 102, 241, 0.1)',
            marginBottom: '1.5rem'
          }}>
            <Bell size={48} color="var(--color-primary)" />
          </div>
          <h2>No Notifications Yet</h2>
          <p style={{ color: 'var(--color-text-muted)', marginTop: '0.5rem' }}>
            When you place orders or payments process, event notifications will appear here.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {notifications.map((n) => (
            <div
              key={n.id}
              className="glass-panel"
              style={{
                padding: '1.25rem 1.5rem',
                display: 'flex',
                alignItems: 'flex-start',
                gap: '1rem'
              }}
            >
              <div style={{
                padding: '0.6rem',
                borderRadius: 'var(--radius-md)',
                background: 'rgba(99, 102, 241, 0.15)',
                color: 'var(--color-primary)',
                marginTop: '0.2rem'
              }}>
                <Mail size={20} />
              </div>

              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                  <h3 style={{ fontSize: '1.05rem', color: '#fff' }}>{n.subject}</h3>
                  <span style={{ fontSize: '0.8rem', color: 'var(--color-text-dim)' }}>
                    {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>

                <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem', marginBottom: '0.5rem' }}>
                  {n.body}
                </p>

                <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', fontSize: '0.75rem' }}>
                  <span className="badge badge-info">{n.eventType}</span>
                  <span style={{ color: 'var(--color-text-dim)' }}>Sent to: {n.recipient}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
