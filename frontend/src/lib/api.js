/**
 * Small fetch wrapper that automatically attaches the JWT (when present) as
 * an Authorization: Bearer <token> header, so callers never repeat that
 * boilerplate.
 *
 * In dev, VITE_API_BASE_URL is unset, so paths stay relative ("/api/...")
 * and vite.config.js's proxy forwards them to the local backend.
 *
 * In production (e.g. deployed on Render as a separate static site + web
 * service), set VITE_API_BASE_URL to the backend's full URL at build time
 * and every call is prefixed with it automatically.
 */

const TOKEN_STORAGE_KEY = "schemagenie_token";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

// Debug line: open the browser console (F12) on the deployed site to see
// exactly what backend URL this build is configured to call. If this prints
// an empty string in production, VITE_API_BASE_URL was not set at build time.
console.log("[SchemaGenie] API_BASE_URL =", JSON.stringify(API_BASE_URL) || "(empty — using relative paths)");

export function getToken() {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
}

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

export async function apiFetch(path, options = {}) {
  const token = getToken();
  const headers = new Headers(options.headers);
  headers.set("Content-Type", "application/json");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const url = `${API_BASE_URL}${path}`;
  const res = await fetch(url, { ...options, headers });

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.error) message = body.error;
    } catch {
      // ignore -- no JSON body
    }
    throw new ApiError(message, res.status);
  }

  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return res.json();
  }
  // e.g. ZIP downloads
  return res;
}

export function apiUrl(path) {
  return `${API_BASE_URL}${path}`;
}
