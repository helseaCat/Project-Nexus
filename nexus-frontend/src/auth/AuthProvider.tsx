import { createContext, useCallback, useMemo, type ReactNode } from 'react';
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

export function AuthProvider({ children }: { children: ReactNode }) {
  const getUser = useCallback((): AuthUser | null => {
    const token = getToken();
    if (!token || isTokenExpired(token)) {
      clearToken();
      return null;
    }
    const claims = decodeToken(token);
    if (!claims) return null;
    return { userId: claims.sub, tenantId: claims.tenant_id, roles: claims.roles };
  }, []);

  const login = useCallback((token: string) => {
    setToken(token);
  }, []);

  const logout = useCallback(() => {
    clearToken();
    window.location.href = '/login';
  }, []);

  const user = getUser();

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isAuthenticated: user !== null,
    login,
    logout,
  }), [user, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
