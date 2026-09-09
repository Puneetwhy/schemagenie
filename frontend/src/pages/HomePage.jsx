
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch, ApiError } from "../lib/api.js";

const DATABASES = [
  { value: "MONGODB", title: "MongoDB", description: "Flexible, document-based — good for evolving schemas" },
  { value: "POSTGRESQL", title: "PostgreSQL", description: "Structured, relational — good for strict data integrity" },
  { value: "MYSQL", title: "MySQL", description: "Widely used relational database — great ecosystem support" },
  { value: "SQLITE", title: "SQLite", description: "Lightweight, file-based — good for small apps and prototypes" },
];

const LANGUAGES = [
  { value: "JAVA", label: "Java" },
  { value: "JAVASCRIPT", label: "JavaScript" },
  { value: "PYTHON", label: "Python" },
  { value: "PHP", label: "PHP" },
  { value: "GO", label: "Go" },
  { value: "C", label: "C" },
  { value: "CPP", label: "C++" },
  { value: "CSHARP", label: "C#" },
];

const EXAMPLES = [
  {
    title: "Food delivery app",
    snippet: "Restaurants, riders, and orders with menus and ratings.",
    description:
      "A food delivery app with restaurants, riders, and orders. Restaurants have menus with items and prices. Riders pick up orders and deliver them to customers, who can rate the delivery afterward.",
  },
  {
    title: "Blog platform",
    snippet: "Authors, posts, comments, and tags.",
    description:
      "A blogging platform where authors write posts. Each post can have multiple comments from readers and multiple tags. Authors have a profile with a bio and a list of their published posts.",
  },
  {
    title: "Online learning platform",
    snippet: "Courses, students, instructors, and enrollments.",
    description:
      "An online learning platform with courses created by instructors. Students enroll in courses and track their progress through lessons. Each course has multiple lessons and students can leave reviews.",
  },
  {
    title: "Inventory management",
    snippet: "Products, suppliers, warehouses, and stock levels.",
    description:
      "An inventory management system tracking products supplied by multiple suppliers, stored across multiple warehouses. Each warehouse tracks stock levels per product, and orders reduce stock when fulfilled.",
  },
];

export default function HomePage() {
  const navigate = useNavigate();
  const [description, setDescription] = useState("");
  const [databaseType, setDatabaseType] = useState("MONGODB");
  const [language, setLanguage] = useState("JAVA");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  async function handleGenerate() {
    setError(null);
    setLoading(true);
    try {
      const res = await apiFetch("/api/schema/generate", {
        method: "POST",
        body: JSON.stringify({ description, databaseType, language }),
      });
      navigate(`/result/${res.sessionId}`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="relative min-h-[calc(100vh-64px)] overflow-hidden bg-slate-50">

      {/* Background */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -top-52 left-1/2 h-[650px] w-[900px] -translate-x-1/2 rounded-full bg-accent/[0.08] blur-3xl" />
        <div className="absolute -right-40 top-[35%] h-[420px] w-[420px] rounded-full bg-indigo-100/50 blur-3xl" />
        <div className="absolute -left-40 bottom-[-100px] h-[400px] w-[400px] rounded-full bg-purple-100/40 blur-3xl" />
      </div>

      <div className="relative mx-auto max-w-5xl px-4 py-12 sm:px-6 sm:py-20 lg:px-8">

        {/* Hero */}
        <div className="mx-auto max-w-3xl text-center">

          <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white/80 px-4 py-2 text-xs font-semibold text-slate-600 shadow-sm backdrop-blur">
            <span className="h-2 w-2 rounded-full bg-emerald-500 shadow-[0_0_0_4px_rgba(16,185,129,0.12)]" />
            AI-powered database schema generator
          </div>

          <h1 className="text-4xl font-bold tracking-tight text-slate-950 sm:text-6xl">
            Describe your app.
            <span className="mt-1 block bg-gradient-to-r from-accent via-indigo-500 to-purple-600 bg-clip-text text-transparent">
              Get your database.
            </span>
          </h1>

          <p className="mx-auto mt-6 max-w-2xl text-base leading-7 text-slate-500 sm:text-lg">
            Plain English in. A complete schema, model classes, migration
            script, and ER diagram out.
          </p>

          <div className="mt-6 flex flex-wrap justify-center gap-x-6 gap-y-2 text-xs font-medium text-slate-400">
            <span>✦ Smart schema generation</span>
            <span>✦ Multiple databases</span>
            <span>✦ Multiple languages</span>
            <span>✦ ER diagram ready</span>
          </div>
        </div>

        {/* Examples */}
        <div className="mx-auto mt-12 max-w-4xl">

          <div className="mb-4">
            <p className="text-sm font-semibold text-slate-900">
              Start with an example
            </p>
            <p className="mt-1 text-xs text-slate-400">
              Choose a template to instantly populate your project description
            </p>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {EXAMPLES.map((ex) => (
              <button
                key={ex.title}
                type="button"
                onClick={() => setDescription(ex.description)}
                className="group relative overflow-hidden rounded-2xl border border-slate-200/80 bg-white p-5 text-left shadow-sm transition-all duration-300 hover:-translate-y-1 hover:border-accent/40 hover:shadow-xl hover:shadow-slate-200/60"
              >
                <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-accent to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100" />

                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-sm font-semibold text-slate-900">
                      {ex.title}
                    </div>

                    <div className="mt-1.5 text-xs leading-5 text-slate-500">
                      {ex.snippet}
                    </div>
                  </div>

                  <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-slate-50 text-slate-400 transition-all duration-300 group-hover:bg-accent/10 group-hover:text-accent">
                    →
                  </div>
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Generator */}
        <div className="mx-auto mt-8 max-w-4xl">

          <div className="overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-2xl shadow-slate-200/50">

            {/* Card Header */}
            <div className="border-b border-slate-100 bg-gradient-to-r from-slate-50/80 via-white to-slate-50/50 px-6 py-5 sm:px-8">

              <div className="flex items-center gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-accent/20 to-indigo-100 text-lg text-accent shadow-sm">
                  ✦
                </div>

                <div>
                  <h2 className="text-sm font-semibold text-slate-900">
                    Generate your schema
                  </h2>

                  <p className="mt-0.5 text-xs text-slate-400">
                    Configure your database and model language
                  </p>
                </div>
              </div>

            </div>

            <div className="p-6 sm:p-8">

              {/* Description */}
              <div>
                <div className="mb-3 flex items-center justify-between gap-3">
                  <label className="text-sm font-semibold text-slate-800">
                    What are you building?
                  </label>

                  <span className="hidden text-xs text-slate-400 sm:block">
                    Describe it naturally
                  </span>
                </div>

                <div className="relative">
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="e.g. A food delivery app with restaurants, riders, and orders..."
                    className="min-h-[175px] w-full resize-y rounded-2xl border border-slate-200 bg-slate-50/50 p-5 text-sm leading-6 text-slate-800 placeholder:text-slate-400 shadow-inner transition-all duration-200 focus:border-accent focus:bg-white focus:outline-none focus:ring-4 focus:ring-accent/10"
                  />

                  <div className="pointer-events-none absolute bottom-4 right-4 hidden rounded-lg border border-slate-100 bg-white/90 px-2.5 py-1.5 text-[10px] font-medium text-slate-400 shadow-sm sm:block">
                    Natural language supported
                  </div>
                </div>
              </div>

              {/* Database */}
              <div className="mt-8">

                <div className="mb-4">
                  <label className="text-sm font-semibold text-slate-800">
                    Target database
                  </label>

                  <p className="mt-1 text-xs text-slate-400">
                    Select where your generated schema will be used
                  </p>
                </div>

                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  {DATABASES.map((db) => (
                    <SelectCard
                      key={db.value}
                      value={db.value}
                      title={db.title}
                      description={db.description}
                      selected={databaseType === db.value}
                      onSelect={setDatabaseType}
                    />
                  ))}
                </div>
              </div>

              {/* Language */}
              <div className="mt-8">

                <div className="mb-4">
                  <label className="text-sm font-semibold text-slate-800">
                    Model code language
                  </label>

                  <p className="mt-1 text-xs text-slate-400">
                    Choose the language for generated model classes
                  </p>
                </div>

                <div className="relative">
                  <select
                    value={language}
                    onChange={(e) => setLanguage(e.target.value)}
                    className="w-full appearance-none rounded-xl border border-slate-200 bg-slate-50 px-4 py-3.5 pr-11 text-sm font-medium text-slate-700 shadow-sm outline-none transition-all duration-200 hover:border-slate-300 focus:border-accent focus:bg-white focus:ring-4 focus:ring-accent/10 sm:w-72"
                  >
                    {LANGUAGES.map((lang) => (
                      <option key={lang.value} value={lang.value}>
                        {lang.label}
                      </option>
                    ))}
                  </select>

                  <div className="pointer-events-none absolute left-[calc(100%-2.75rem)] top-1/2 -translate-y-1/2 text-slate-400 sm:left-[calc(18rem-2.75rem)]">
                    <svg
                      className="h-4 w-4"
                      viewBox="0 0 20 20"
                      fill="currentColor"
                    >
                      <path
                        fillRule="evenodd"
                        d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z"
                        clipRule="evenodd"
                      />
                    </svg>
                  </div>
                </div>

                <p className="mt-2 text-xs leading-5 text-slate-400">
                  Java generates full JPA/Mongo-annotated classes; other
                  languages generate idiomatic model classes/structs.
                </p>
              </div>

              {/* Error */}
              {error && (
                <div className="mt-6 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3.5 text-sm text-red-700">
                  <div className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-100 font-bold">
                    !
                  </div>

                  <p className="leading-5">
                    {error}
                  </p>
                </div>
              )}

              {/* Bottom action */}
              <div className="mt-8 flex flex-col-reverse gap-4 border-t border-slate-100 pt-6 sm:flex-row sm:items-center sm:justify-between">

                <div>
                  <p className="text-xs font-medium text-slate-500">
                    Ready to generate?
                  </p>

                  <p className="mt-0.5 text-[11px] text-slate-400">
                    Your schema will be generated automatically
                  </p>
                </div>

                <button
                  onClick={handleGenerate}
                  disabled={loading || description.trim().length === 0}
                  className="group inline-flex w-full items-center justify-center gap-2.5 rounded-xl bg-slate-950 px-7 py-3.5 text-sm font-semibold text-white shadow-lg shadow-slate-900/10 transition-all duration-300 hover:-translate-y-0.5 hover:bg-accent hover:shadow-xl hover:shadow-accent/20 disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:translate-y-0 disabled:hover:bg-slate-950 sm:w-auto"
                >
                  {loading && (
                    <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                  )}

                  {!loading && (
                    <span className="text-base">
                      ✦
                    </span>
                  )}

                  {loading ? "Generating..." : "Generate Schema"}

                  {!loading && (
                    <span className="transition-transform duration-300 group-hover:translate-x-1">
                      →
                    </span>
                  )}
                </button>

              </div>
            </div>
          </div>

          <div className="mt-5 flex justify-center">
            <p className="text-xs text-slate-400">
              Supports MongoDB, PostgreSQL, MySQL & SQLite
            </p>
          </div>

        </div>
      </div>
    </div>
  );
}

function SelectCard({ value, title, description, selected, onSelect }) {
  return (
    <button
      type="button"
      onClick={() => onSelect(value)}
      className={`group relative overflow-hidden rounded-2xl border p-5 text-left transition-all duration-300 ${
        selected
          ? "border-accent bg-accent/[0.04] shadow-md shadow-accent/10 ring-1 ring-accent"
          : "border-slate-200 bg-white hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-lg hover:shadow-slate-200/60"
      }`}
    >
      {selected && (
        <div className="absolute left-0 top-0 h-full w-1 bg-accent" />
      )}

      <div className="flex items-start justify-between gap-4">

        <div>
          <div
            className={`text-sm font-semibold ${
              selected ? "text-accent" : "text-slate-900"
            }`}
          >
            {title}
          </div>

          <div className="mt-1.5 text-xs leading-5 text-slate-500">
            {description}
          </div>
        </div>

        <div
          className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full border transition-all duration-200 ${
            selected
              ? "border-accent bg-accent text-white shadow-sm"
              : "border-slate-300 bg-white text-transparent group-hover:border-slate-400"
          }`}
        >
          {selected && (
            <span className="text-[11px] font-bold">
              ✓
            </span>
          )}
        </div>

      </div>
    </button>
  );
}
