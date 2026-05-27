import { createContext, useCallback, useMemo, useState, type ReactNode } from 'react';
import { getToken, setToken, clearToken, decodeToken, isTokenExpired } from './token';

export interface AuthUser {
  userId: string;
  tenantId: string;
  roles: string[];
}

export interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);

function resolveUser(): AuthUser | null {
  const token = getToken();
  if (!token || isTokenExpired(token)) {
    clearToken();
    return null;
  }
  const claims = decodeToken(token);
  if (!claims) return null;
  return { userId: claims.sub, tenantId: claims.tenant_id, roles: claims.roles };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => resolveUser());

  const login = useCallback((token: string) => {
    setToken(token);
    const claims = decodeToken(token);
    if (!claims || isTokenExpired(token)) {
      clearToken();
      setUser(null);
      return;
    }
    setUser({ userId: claims.sub, tenantId: claims.tenant_id, roles: claims.roles });
  }, []);

  const logout = useCallback(() => {
    clearToken();
    setUser(null);
    window.location.href = '/login';
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isAuthenticated: user !== null,
    login,
    logout,
  }), [user, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
