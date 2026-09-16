import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UserPlus, AlertCircle } from 'lucide-react';
import { authApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

export const RegisterPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);
      const res = await authApi.register({ username, email, password, firstName, lastName });
      login(res.username, res.token, res.role);
      navigate('/');
    } catch (err: any) {
      console.error('Registration error', err);
      setError(err.response?.data?.message || 'Registration failed. Try a different username or email.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container page-container" style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '70vh'
    }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '480px', padding: '2.5rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{
            display: 'inline-flex',
            padding: '1rem',
            borderRadius: 'var(--radius-full)',
            background: 'rgba(99, 102, 241, 0.15)',
            marginBottom: '1rem'
          }}>
            <UserPlus size={32} color="var(--color-primary)" />
          </div>
          <h1 style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>Create an Account</h1>
          <p style={{ color: 'var(--color-text-muted)', fontSize: '0.9rem' }}>
            Join Go N Buy for seamless shopping, order tracking, and lightning fast checkout.
          </p>
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

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">First Name</label>
              <input
                type="text"
                placeholder="John"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Last Name</label>
              <input
                type="text"
                placeholder="Doe"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                className="form-input"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Username</label>
            <input
              type="text"
              required
              placeholder="johndoe"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input
              type="email"
              required
              placeholder="john@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              type="password"
              required
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="form-input"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '1rem', padding: '0.85rem' }}
          >
            {loading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--color-secondary)', fontWeight: 600 }}>
            Sign In
          </Link>
        </div>
      </div>
    </div>
  );
};
