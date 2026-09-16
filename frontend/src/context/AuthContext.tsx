import React, { createContext, useContext, useState, useEffect } from 'react';

interface AuthContextType {
  token: string | null;
  username: string | null;
  role: string | null;
  isAuthenticated: boolean;
  login: (username: string, token: string, role: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [username, setUsername] = useState<string | null>(localStorage.getItem('username'));
  const [role, setRole] = useState<string | null>(localStorage.getItem('role'));

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }, [token]);

  const login = (newUsername: string, newToken: string, newRole: string) => {
    localStorage.setItem('token', newToken);
    localStorage.setItem('username', newUsername);
    localStorage.setItem('role', newRole);
    setToken(newToken);
    setUsername(newUsername);
    setRole(newRole);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    setToken(null);
    setUsername(null);
    setRole(null);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        username,
        role,
        isAuthenticated: !!token,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
