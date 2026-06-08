import type { Role } from "./types";

function base64UrlDecode(str: string): string {
  str = str.replace(/-/g, "+").replace(/_/g, "/");
  const padded = str.padEnd(str.length + ((4 - (str.length % 4)) % 4), "=");
  return atob(padded);
}

function base64UrlEncode(obj: unknown): string {
  return btoa(JSON.stringify(obj))
    .replace(/=/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");
}

export function decodeToken(token: string): { sub: string; role: Role; exp: number } | null {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    const payload = JSON.parse(base64UrlDecode(parts[1]));
    if (payload.role !== "ADMIN" && payload.role !== "USER") return null;
    if (typeof payload.exp !== "number") return null;
    return { sub: payload.sub, role: payload.role as Role, exp: payload.exp };
  } catch {
    return null;
  }
}

const EXP_BUFFER_SEC = 30;

export function isTokenExpired(token: string): boolean {
  const decoded = decodeToken(token);
  if (!decoded) return true;
  return Date.now() / 1000 >= decoded.exp - EXP_BUFFER_SEC;
}

export function storeAuth(token: string): void {
  localStorage.setItem("token", token);
  const decoded = decodeToken(token);
  if (decoded) {
    localStorage.setItem("role", decoded.role);
    localStorage.setItem("username", decoded.sub);
  }
}

export function getRole(): Role | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("role") as Role | null;
}

export function clearAuth(): void {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("username");
}

export function isLoggedIn(): boolean {
  if (typeof window === "undefined") return false;
  return !!localStorage.getItem("token");
}

const TEN_DAYS_SEC = 864_000;

export function buildMockJwt(username: string, role: string): string {
  const now = Math.floor(Date.now() / 1000);
  const header = base64UrlEncode({ alg: "HS256", typ: "JWT" });
  const payload = base64UrlEncode({ sub: username, role, iat: now, exp: now + TEN_DAYS_SEC });
  return `${header}.${payload}.mock-sig`;
}

export const ADMIN_ROUTES = ["/users", "/nodes"] as const;
