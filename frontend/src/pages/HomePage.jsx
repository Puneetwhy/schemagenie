import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch, ApiError } from "../lib/api.js";

export default function HomePage() {
  const navigate = useNavigate();
  const [description, setDescription] = useState("");
  const [databaseType, setDatabaseType] = useState("MONGODB");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  async function handleGenerate() {
    setError(null);
    setLoading(true);
    try {
      const res = await apiFetch("/api/schema/generate", {
        method: "POST",
        body: JSON.stringify({ description, databaseType }),
      });
      navigate(`/result/${res.sessionId}`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-16 sm:py-24">
      <h1 className="text-3xl sm:text-5xl font-bold tracking-tight text-gray-900 text-center">
        Describe your app. Get your database.
      </h1>
      <p className="mt-4 text-center text-gray-500 text-lg">
        Plain English in. A full schema, model classes, migration script, and ER diagram out.
      </p>

      <div className="mt-10 bg-white rounded-2xl shadow-sm border border-gray-200 p-6 sm:p-8">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          What are you building?
        </label>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="e.g. A food delivery app with restaurants, riders, and orders. Restaurants have menus with items and prices. Riders pick up orders and deliver them to customers, who can rate the delivery afterward."
          className="w-full min-h-[150px] rounded-xl border border-gray-300 p-4 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-accent focus:border-transparent"
        />

        <div className="mt-6">
          <label className="block text-sm font-medium text-gray-700 mb-3">Target database</label>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <DbOption
              value="MONGODB"
              title="MongoDB"
              description="Flexible, document-based — good for evolving schemas"
              selected={databaseType === "MONGODB"}
              onSelect={setDatabaseType}
            />
            <DbOption
              value="POSTGRESQL"
              title="PostgreSQL"
              description="Structured, relational — good for strict data integrity"
              selected={databaseType === "POSTGRESQL"}
              onSelect={setDatabaseType}
            />
          </div>
        </div>

        {error && (
          <p className="mt-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-4 py-2">
            {error}
          </p>
        )}

        <button
          onClick={handleGenerate}
          disabled={loading || description.trim().length === 0}
          className="mt-6 w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-full bg-accent px-6 py-3 text-white font-medium hover:bg-accent-dark disabled:opacity-50 disabled:cursor-not-allowed transition"
        >
          {loading && (
            <span className="h-4 w-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
          )}
          {loading ? "Generating..." : "Generate Schema"}
        </button>
      </div>
    </div>
  );
}

function DbOption({ value, title, description, selected, onSelect }) {
  return (
    <button
      type="button"
      onClick={() => onSelect(value)}
      className={`text-left rounded-xl border p-4 transition ${
        selected
          ? "border-accent bg-accent-light ring-1 ring-accent"
          : "border-gray-200 hover:border-gray-300"
      }`}
    >
      <div className="font-semibold text-gray-900">{title}</div>
      <div className="text-sm text-gray-500 mt-1">{description}</div>
    </button>
  );
}
