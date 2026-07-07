import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);
const STORAGE_KEY = 'minerva_user';
const LEGACY_KEY = 'odc_user';

function loadStoredUser() {
  const stored = localStorage.getItem(STORAGE_KEY) || localStorage.getItem(LEGACY_KEY);
  if (!stored) return null;
  try {
    const user = JSON.parse(stored);
    if (localStorage.getItem(LEGACY_KEY)) {
      localStorage.setItem(STORAGE_KEY, stored);
      localStorage.removeItem(LEGACY_KEY);
    }
    return user;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem(LEGACY_KEY);
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setUser(loadStoredUser());
    setLoading(false);
  }, []);

  const login = useCallback((userData) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(userData));
    localStorage.removeItem(LEGACY_KEY);
    setUser(userData);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem(LEGACY_KEY);
    setUser(null);
  }, []);

  const hasPermission = useCallback((module, action) => {
    if (!user?.permissions) return false;
    if (user.roles?.includes('ADMINISTRATEUR')) return true;
    const modulePerms = user.permissions[module];
    return modulePerms?.includes(action) ?? false;
  }, [user]);

  const refreshProfil = useCallback(async () => {
    try {
      const res = await api.get('/auth/moi');
      const stored = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
      const updated = { ...stored, ...res.data };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
      setUser(updated);
    } catch {
      logout();
    }
  }, [logout]);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasPermission, refreshProfil }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth doit être utilisé dans AuthProvider');
  return ctx;
}
