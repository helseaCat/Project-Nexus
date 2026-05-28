const TOKEN_KEY = 'nexus_jwt';

export interface JwtClaims {
  sub: string;       // user ID
  tenant_id: string;
  roles: string[];
  exp: number;
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function decodeToken(token: string): JwtClaims | null {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;
    // Convert base64url to standard base64
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
    const parsed: unknown = JSON.parse(atob(padded));
    // Validate claims shape
    if (
      typeof parsed === 'object' &&
      parsed !== null &&
      typeof (parsed as Record<string, unknown>).sub === 'string' &&
      typeof (parsed as Record<string, unknown>).tenant_id === 'string' &&
      Array.isArray((parsed as Record<string, unknown>).roles) &&
      Number.isFinite((parsed as Record<string, unknown>).exp)
    ) {
      return parsed as JwtClaims;
    }
    return null;
  } catch {
    return null;
  }
}

export function isTokenExpired(token: string): boolean {
  const claims = decodeToken(token);
  if (!claims || !Number.isFinite(claims.exp)) return true;
  return Date.now() >= claims.exp * 1000;
}
