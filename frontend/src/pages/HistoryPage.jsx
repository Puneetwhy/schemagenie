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
      .catch((err) =>
        setError(
          err instanceof ApiError
            ? err.message
            : "Couldn't load history."
        )
      );
  }, [user]);

  if (authLoading || !user) {
    return (
      <main className="history-page">
        <div className="history-loading">
          <div className="history-loading-spinner" />
          <p>Loading your history...</p>
        </div>
      </main>
    );
  }

  return (
    <main className="history-page">
      <div className="history-background-grid" />
      <div className="history-orb history-orb-one" />
      <div className="history-orb history-orb-two" />

      <div className="history-container">
        {/* Header */}
        <div className="history-header">
          <div>
            <div className="history-eyebrow">
              <span className="history-eyebrow-dot" />
              Your workspace
            </div>

            <h1 className="history-title">
              My <span>History</span>
            </h1>

            <p className="history-subtitle">
              Revisit your previously generated database schemas.
            </p>
          </div>

          <Link to="/" className="history-new-button">
            <PlusIcon />
            <span>New Schema</span>
          </Link>
        </div>

        {/* Error */}
        {error && (
          <div className="history-error">
            <div className="history-error-icon">
              <AlertIcon />
            </div>

            <div>
              <p className="history-error-title">
                Couldn't load your history
              </p>
              <p className="history-error-text">
                {error}
              </p>
            </div>
          </div>
        )}

        {/* Empty State */}
        {items && items.length === 0 && !error && (
          <div className="history-empty">
            <div className="history-empty-icon">
              <DatabaseIcon />
            </div>

            <h2>No schemas yet</h2>

            <p>
              Your generated schemas will appear here once you create one.
            </p>

            <Link to="/" className="history-empty-button">
              Generate your first schema
              <ArrowIcon />
            </Link>
          </div>
        )}

        {/* History List */}
        {items && items.length > 0 && (
          <div className="history-list">
            <div className="history-list-header">
              <span>
                {items.length} {items.length === 1 ? "schema" : "schemas"}
              </span>

              <span>Recently generated</span>
            </div>

            <div className="history-items">
              {items.map((item, index) => (
                <Link
                  key={item.sessionId}
                  to={`/result/${item.sessionId}`}
                  className="history-card"
                  style={{
                    animationDelay: `${Math.min(index * 55, 400)}ms`,
                  }}
                >
                  <div className="history-card-main">
                    <div className="history-card-icon">
                      <DatabaseIcon />
                    </div>

                    <div className="history-card-content">
                      <div className="history-card-meta">
                        <span className="history-database-badge">
                          <span className="history-database-dot" />
                          {item.databaseType}
                        </span>

                        <span className="history-date">
                          {new Date(item.createdAt).toLocaleDateString()}
                        </span>
                      </div>

                      <p className="history-description">
                        {item.descriptionSnippet}
                      </p>
                    </div>
                  </div>

                  <div className="history-card-arrow">
                    <ArrowIcon />
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}
      </div>
    </main>
  );
}

/* -----------------------------------------
   Icons
----------------------------------------- */

function DatabaseIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-5 w-5"
      aria-hidden="true"
    >
      <ellipse
        cx="10"
        cy="4.5"
        rx="5.5"
        ry="2.5"
        stroke="currentColor"
        strokeWidth="1.4"
      />
      <path
        d="M4.5 4.5v5.3c0 1.4 2.46 2.5 5.5 2.5s5.5-1.1 5.5-2.5V4.5"
        stroke="currentColor"
        strokeWidth="1.4"
      />
      <path
        d="M4.5 9.8v5.2c0 1.4 2.46 2.5 5.5 2.5s5.5-1.1 5.5-2.5V9.8"
        stroke="currentColor"
        strokeWidth="1.4"
      />
    </svg>
  );
}

function PlusIcon() {
  return (
    <svg
      viewBox="0 0 16 16"
      fill="none"
      className="h-4 w-4"
      aria-hidden="true"
    >
      <path
        d="M8 3v10M3 8h10"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg
      viewBox="0 0 16 16"
      fill="none"
      className="h-4 w-4"
      aria-hidden="true"
    >
      <path
        d="M3 8h9M8.5 4.5 12 8l-3.5 3.5"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function AlertIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-4 w-4"
      aria-hidden="true"
    >
      <path
        d="M10 3.5 17 16H3L10 3.5Z"
        stroke="currentColor"
        strokeWidth="1.4"
        strokeLinejoin="round"
      />
      <path
        d="M10 8v3.5M10 14h.01"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
}