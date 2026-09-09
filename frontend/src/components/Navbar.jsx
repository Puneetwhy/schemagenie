import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function Navbar() {
  const { user, loading, logout } = useAuth();

  return (
    <header className="navbar-shell">
      <div className="navbar-inner">
        {/* Brand */}
        <Link to="/" className="navbar-brand">
          <span className="navbar-brand-mark">
            <span />
            <span />
            <span />
          </span>

          <span>
            Schema<span className="navbar-brand-accent">Genie</span>
          </span>
        </Link>

        {/* Navigation */}
        <nav className="navbar-nav">
          {loading ? null : user ? (
            <>
              <span className="navbar-greeting">
                Hi, {user.displayName}
              </span>

              <Link to="/history" className="navbar-link">
                <HistoryIcon />
                <span>My History</span>
              </Link>

              <button
                onClick={logout}
                className="navbar-logout"
                type="button"
              >
                <LogoutIcon />
                <span>Log Out</span>
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="navbar-link">
                Log In
              </Link>

              <Link to="/signup" className="navbar-signup">
                <span>Sign Up</span>
                <ArrowIcon />
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

function HistoryIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-4 w-4"
      aria-hidden="true"
    >
      <path
        d="M4 4.5h12M4 8.5h12M4 12.5h7"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
      <path
        d="M14.5 12.5v4m0 0-1.7-1.7m1.7 1.7 1.7-1.7"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function LogoutIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-4 w-4"
      aria-hidden="true"
    >
      <path
        d="M8 4H5.5A1.5 1.5 0 0 0 4 5.5v9A1.5 1.5 0 0 0 5.5 16H8"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
      <path
        d="M11 6.5 14.5 10 11 13.5M14 10H7"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg
      viewBox="0 0 16 16"
      fill="none"
      className="h-3.5 w-3.5"
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