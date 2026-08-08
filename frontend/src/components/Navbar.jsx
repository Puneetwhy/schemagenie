import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function Navbar() {
  const { user, loading, logout } = useAuth();

  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">
        <Link to="/" className="text-lg font-semibold text-gray-900">
          Schema<span className="text-accent">Genie</span>
        </Link>

        <nav className="flex items-center gap-4 text-sm">
          {loading ? null : user ? (
            <>
              <span className="text-gray-600 hidden sm:inline">Hi, {user.displayName}</span>
              <Link to="/history" className="text-gray-700 hover:text-accent">
                My History
              </Link>
              <button
                onClick={logout}
                className="rounded-full px-4 py-1.5 border border-gray-300 text-gray-700 hover:bg-gray-100"
              >
                Log Out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-gray-700 hover:text-accent">
                Log In
              </Link>
              <Link
                to="/signup"
                className="rounded-full px-4 py-1.5 bg-accent text-white hover:bg-accent-dark"
              >
                Sign Up
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
