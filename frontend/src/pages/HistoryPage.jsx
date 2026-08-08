import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { apiFetch, ApiError } from "../lib/api.js";

export default function HistoryPage() {
  const navigate = useNavigate();
  const { user, loading: authLoading } = useAuth();
  const [items, setItems] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!authLoading && !user) {
      navigate("/login");
    }
  }, [authLoading, user, navigate]);

  useEffect(() => {
    if (!user) return;
    apiFetch("/api/schema/history")
      .then(setItems)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Couldn't load history."));
  }, [user]);

  if (authLoading || !user) {
    return <div className="max-w-3xl mx-auto px-4 py-16 text-center text-gray-500">Loading...</div>;
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-12">
      <h1 className="text-2xl font-bold text-gray-900">My History</h1>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {items && items.length === 0 && (
        <p className="mt-6 text-gray-500">
          No saved schemas yet.{" "}
          <Link to="/" className="text-accent hover:underline">
            Generate your first one
          </Link>
          .
        </p>
      )}

      <div className="mt-6 space-y-3">
        {items?.map((item) => (
          <Link
            key={item.sessionId}
            to={`/result/${item.sessionId}`}
            className="block bg-white rounded-xl border border-gray-200 p-4 hover:border-accent transition"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-medium uppercase tracking-wide text-accent">
                {item.databaseType}
              </span>
              <span className="text-xs text-gray-400">
                {new Date(item.createdAt).toLocaleDateString()}
              </span>
            </div>
            <p className="mt-2 text-sm text-gray-700">{item.descriptionSnippet}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
