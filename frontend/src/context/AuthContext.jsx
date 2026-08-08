import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { apiFetch, getToken, setToken as storeToken, clearToken, ApiError } from "../lib/api.js";

const AuthContext = createContext(undefined);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setTokenState] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadMe = useCallback(async () => {
    const existingToken = getToken();
    if (!existingToken) {
      setLoading(false);
      return;
    }
    setTokenState(existingToken);
    try {
      const me = await apiFetch("/api/auth/me");
      setUser(me);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        clearToken();
        setTokenState(null);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadMe();
  }, [loadMe]);

  const login = useCallback(async (email, password) => {
    const res = await apiFetch("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    storeToken(res.token);
    setTokenState(res.token);
    setUser({ id: res.userId, email: res.email, displayName: res.displayName });
  }, []);

  const signup = useCallback(async (email, password, displayName) => {
    const res = await apiFetch("/api/auth/signup", {
      method: "POST",
      body: JSON.stringify({ email, password, displayName }),
    });
    storeToken(res.token);
    setTokenState(res.token);
    setUser({ id: res.userId, email: res.email, displayName: res.displayName });
  }, []);

  const logout = useCallback(() => {
    clearToken();
    setTokenState(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, loading, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
