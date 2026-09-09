import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { ApiError } from "../lib/api.js";

export default function SignupPage() {
  const navigate = useNavigate();
  const { signup } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await signup(email, password, displayName);
      navigate("/");
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "Couldn't sign up. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page relative min-h-screen overflow-hidden">
      {/* Animated background */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="auth-grid absolute inset-0" />

        <div className="auth-orb auth-orb-one" />
        <div className="auth-orb auth-orb-two" />

        <div className="absolute inset-x-0 top-0 h-48 bg-gradient-to-b from-white/80 to-transparent" />
      </div>

      {/* Main content */}
      <div className="relative mx-auto flex min-h-screen max-w-md items-center px-4 py-10 sm:px-6">
        <div className="auth-content w-full">

          {/* Brand */}
          <div className="auth-enter auth-delay-1 mb-7 flex justify-center">
            <Link
              to="/"
              className="group inline-flex items-center gap-2.5 rounded-full border border-gray-200/80 bg-white/80 px-3.5 py-1.5 text-xs font-semibold text-gray-600 shadow-sm backdrop-blur-xl transition-all hover:border-accent/20 hover:shadow-md"
            >
              <span className="auth-logo-mark">
                <span />
              </span>

              <span>SchemaGenie</span>
            </Link>
          </div>

          {/* Heading */}
          <div className="auth-enter auth-delay-2 text-center">
            <h1 className="text-3xl font-bold tracking-[-0.035em] text-gray-950 sm:text-4xl">
              Build something great.
            </h1>

            <p className="mx-auto mt-2.5 max-w-sm text-sm leading-6 text-gray-500">
              Create your account and start generating database schemas.
            </p>
          </div>

          {/* Signup form */}
          <form
            onSubmit={handleSubmit}
            className="auth-form auth-enter auth-delay-3 mt-8 rounded-[1.5rem] border border-gray-200/80 bg-white/95 p-5 shadow-[0_25px_70px_rgba(15,23,42,0.07)] backdrop-blur-xl sm:p-7"
          >
            {/* Animated top line */}
            <div className="auth-card-shimmer pointer-events-none absolute" />

            {/* Display name */}
            <div className="auth-field">
              <label
                htmlFor="signup-name"
                className="mb-2 block text-xs font-semibold text-gray-700"
              >
                Display name
              </label>

              <div className="auth-input-wrapper group relative">
                <div className="auth-input-icon">
                  <UserIcon />
                </div>

                <input
                  id="signup-name"
                  type="text"
                  required
                  value={displayName}
                  onChange={(e) => setDisplayName(e.target.value)}
                  placeholder="Your name"
                  autoComplete="name"
                  className="auth-input w-full rounded-xl border border-gray-200 bg-gray-50/60 py-3 pl-10 pr-3.5 text-sm text-gray-900 outline-none transition-all placeholder:text-gray-400"
                />
              </div>
            </div>

            {/* Email */}
            <div className="auth-field mt-5">
              <label
                htmlFor="signup-email"
                className="mb-2 block text-xs font-semibold text-gray-700"
              >
                Email address
              </label>

              <div className="auth-input-wrapper group relative">
                <div className="auth-input-icon">
                  <MailIcon />
                </div>

                <input
                  id="signup-email"
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  autoComplete="email"
                  className="auth-input w-full rounded-xl border border-gray-200 bg-gray-50/60 py-3 pl-10 pr-3.5 text-sm text-gray-900 outline-none transition-all placeholder:text-gray-400"
                />
              </div>
            </div>

            {/* Password */}
            <div className="auth-field mt-5">
              <label
                htmlFor="signup-password"
                className="mb-2 block text-xs font-semibold text-gray-700"
              >
                Password
              </label>

              <div className="auth-input-wrapper group relative">
                <div className="auth-input-icon">
                  <LockIcon />
                </div>

                <input
                  id="signup-password"
                  type="password"
                  required
                  minLength={8}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Create a password"
                  autoComplete="new-password"
                  className="auth-input w-full rounded-xl border border-gray-200 bg-gray-50/60 py-3 pl-10 pr-3.5 text-sm text-gray-900 outline-none transition-all placeholder:text-gray-400"
                />
              </div>

              <div className="mt-2 flex items-center gap-1.5">
                <span className="h-1 w-1 rounded-full bg-accent/60" />
                <p className="text-[11px] text-gray-400">
                  At least 8 characters.
                </p>
              </div>
            </div>

            {/* Error */}
            {error && (
              <div className="auth-error mt-5 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-3.5 py-3 text-sm text-red-700">
                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-100 text-xs font-bold">
                  !
                </span>

                <p className="leading-5">{error}</p>
              </div>
            )}

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="auth-submit group relative mt-6 inline-flex w-full items-center justify-center gap-2.5 overflow-hidden rounded-xl bg-gray-950 px-5 py-3.5 text-sm font-semibold text-white shadow-lg shadow-gray-950/10 transition-all duration-300 hover:-translate-y-0.5 hover:bg-gray-800 hover:shadow-xl disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:translate-y-0"
            >
              <span className="auth-button-shine pointer-events-none absolute inset-y-0 -left-1/2 w-1/3 skew-x-[-20deg] bg-white/10" />

              {loading ? (
                <>
                  <span className="auth-spinner h-4 w-4 rounded-full border-2 border-white/25 border-t-white" />
                  <span>Creating account...</span>
                </>
              ) : (
                <>
                  <span>Create Account</span>

                  <span className="auth-arrow">
                    <ArrowIcon />
                  </span>
                </>
              )}
            </button>

            {/* Divider */}
            <div className="my-6 flex items-center gap-3">
              <div className="h-px flex-1 bg-gray-100" />

              <span className="text-[10px] font-medium uppercase tracking-wider text-gray-400">
                Already a member?
              </span>

              <div className="h-px flex-1 bg-gray-100" />
            </div>

            {/* Login */}
            <p className="text-center text-sm text-gray-500">
              Already have an account?{" "}
              <Link
                to="/login"
                className="font-semibold text-accent transition-colors hover:text-accent-dark"
              >
                Log in
              </Link>
            </p>
          </form>

          {/* Footer */}
          <div className="auth-enter auth-delay-4 mt-6 flex items-center justify-center gap-2 text-[10px] font-medium uppercase tracking-[0.12em] text-gray-400">
            <span className="h-1 w-1 rounded-full bg-accent/60" />
            Your schemas. Your workflow.
            <span className="h-1 w-1 rounded-full bg-accent/60" />
          </div>
        </div>
      </div>
    </main>
  );
}

/* ==============================================================
   ICONS
============================================================== */

function UserIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-4 w-4"
      aria-hidden="true"
    >
      <circle
        cx="10"
        cy="6.5"
        r="3"
        stroke="currentColor"
        strokeWidth="1.4"
      />

      <path
        d="M4.5 16.5c.8-2.6 2.7-4 5.5-4s4.7 1.4 5.5 4"
        stroke="currentColor"
        strokeWidth="1.4"
        strokeLinecap="round"
      />
    </svg>
  );
}

function MailIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-4 w-4"
      aria-hidden="true"
    >
      <rect
        x="2.5"
        y="4"
        width="15"
        height="12"
        rx="2"
        stroke="currentColor"
        strokeWidth="1.4"
      />

      <path
        d="m3.5 6 5.1 4.05a2.2 2.2 0 0 0 2.8 0L16.5 6"
        stroke="currentColor"
        strokeWidth="1.4"
        strokeLinecap="round"
      />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-4 w-4"
      aria-hidden="true"
    >
      <rect
        x="3.5"
        y="8"
        width="13"
        height="9"
        rx="2"
        stroke="currentColor"
        strokeWidth="1.4"
      />

      <path
        d="M6.5 8V6a3.5 3.5 0 0 1 7 0v2"
        stroke="currentColor"
        strokeWidth="1.4"
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