
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { apiFetch, apiUrl, ApiError } from "../lib/api.js";
import CodeTab from "../components/CodeTab.jsx";
import MermaidDiagram from "../components/MermaidDiagram.jsx";

const TABS = ["Model Classes", "Migration Script", "ER Diagram", "Explanation", "Raw JSON"];

const HIGHLIGHT_LANGUAGE = {
  JAVA: "java",
  JAVASCRIPT: "javascript",
  PYTHON: "python",
  PHP: "php",
  GO: "go",
  C: "c",
  CPP: "cpp",
  CSHARP: "csharp",
};

export default function ResultPage() {
  const { sessionId } = useParams();
  const { user } = useAuth();

  const [session, setSession] = useState(null);
  const [activeTab, setActiveTab] = useState("Model Classes");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [refineNote, setRefineNote] = useState("");
  const [refining, setRefining] = useState(false);

  const [saveState, setSaveState] = useState("idle");

  useEffect(() => {
    apiFetch(`/api/schema/session/${sessionId}`)
      .then(setSession)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Couldn't load this session."))
      .finally(() => setLoading(false));
  }, [sessionId]);

  async function handleRefine() {
    if (!refineNote.trim()) return;
    setRefining(true);
    setError(null);
    try {
      const updated = await apiFetch(`/api/schema/refine/${sessionId}`, {
        method: "POST",
        body: JSON.stringify({ refinement: refineNote }),
      });
      setSession(updated);
      setRefineNote("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Couldn't refine the schema.");
    } finally {
      setRefining(false);
    }
  }

  async function handleSaveToHistory() {
    setSaveState("saving");
    try {
      const updated = await apiFetch(`/api/schema/session/${sessionId}/save`, {
        method: "POST",
      });
      setSession(updated);
      setSaveState("saved");
    } catch {
      setSaveState("idle");
    }
  }

  function handleDownload() {
    window.open(apiUrl(`/api/schema/download/${sessionId}`), "_blank");
  }

  if (loading) {
    return (
      <div className="min-h-[calc(100vh-64px)] bg-slate-50">
        <div className="mx-auto flex min-h-[500px] max-w-5xl items-center justify-center px-4">
          <div className="text-center">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-white shadow-lg shadow-slate-200">
              <div className="h-5 w-5 animate-spin rounded-full border-2 border-slate-200 border-t-accent" />
            </div>
            <p className="mt-4 text-sm font-medium text-slate-600">
              Loading your schema...
            </p>
            <p className="mt-1 text-xs text-slate-400">
              Preparing your generated database
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (error && !session) {
    return (
      <div className="min-h-[calc(100vh-64px)] bg-slate-50">
        <div className="mx-auto flex min-h-[500px] max-w-5xl items-center justify-center px-4">
          <div className="w-full max-w-md rounded-3xl border border-red-200 bg-white p-8 text-center shadow-xl shadow-red-100/50">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-red-50 text-red-600">
              !
            </div>

            <h2 className="mt-4 text-lg font-semibold text-slate-900">
              Couldn't load this session
            </h2>

            <p className="mt-2 text-sm leading-6 text-red-600">
              {error}
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (!session) return null;

  const modelLanguage = HIGHLIGHT_LANGUAGE[session.language] || "java";
  const migrationLanguage = session.databaseType === "MONGODB"
    ? (session.language === "JAVA" ? "java" : "javascript")
    : "yaml";

  return (
    <div className="relative min-h-[calc(100vh-64px)] overflow-hidden bg-slate-50">

      {/* Background */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -top-60 left-1/2 h-[600px] w-[850px] -translate-x-1/2 rounded-full bg-accent/[0.07] blur-3xl" />
        <div className="absolute -right-40 top-[30%] h-[400px] w-[400px] rounded-full bg-indigo-100/40 blur-3xl" />
      </div>

      <div className="relative mx-auto max-w-6xl px-4 py-8 sm:px-6 sm:py-10 lg:px-8">

        {/* Header */}
        <div className="rounded-3xl border border-slate-200/80 bg-white p-6 shadow-xl shadow-slate-200/40 sm:p-8">

          <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">

            {/* Session information */}
            <div className="min-w-0 flex-1">

              <div className="flex flex-wrap items-center gap-2">
                <span className="inline-flex items-center gap-2 rounded-full border border-accent/20 bg-accent/5 px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider text-accent">
                  <span className="h-1.5 w-1.5 rounded-full bg-accent" />
                  {session.databaseType}
                </span>

                <span className="text-slate-300">•</span>

                <span className="rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-[11px] font-semibold text-slate-600">
                  {session.language}
                </span>
              </div>

              <h1 className="mt-4 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">
                Your database schema is ready
              </h1>

              <p className="mt-3 max-w-3xl text-sm leading-6 text-slate-500">
                {session.description}
              </p>

            </div>

            {/* Actions */}
            <div className="flex shrink-0 flex-col gap-2 sm:flex-row lg:flex-col xl:flex-row">

              {user && !session.owned && (
                <button
                  onClick={handleSaveToHistory}
                  disabled={saveState !== "idle"}
                  className="inline-flex items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition-all hover:-translate-y-0.5 hover:border-slate-300 hover:bg-slate-50 hover:shadow-md disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {saveState === "saved" ? (
                    <>
                      <span className="text-emerald-500">✓</span>
                      Saved!
                    </>
                  ) : saveState === "saving" ? (
                    <>
                      <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-slate-300 border-t-slate-700" />
                      Saving...
                    </>
                  ) : (
                    <>
                      <span>♡</span>
                      Save to history
                    </>
                  )}
                </button>
              )}

              <button
                onClick={handleDownload}
                className="group inline-flex items-center justify-center gap-2 rounded-xl bg-slate-950 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-slate-900/10 transition-all duration-300 hover:-translate-y-0.5 hover:bg-accent hover:shadow-xl hover:shadow-accent/20"
              >
                <svg
                  className="h-4 w-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M12 3v12" />
                  <path d="m7 10 5 5 5-5" />
                  <path d="M5 21h14" />
                </svg>

                Download ZIP

                <span className="transition-transform duration-300 group-hover:translate-y-0.5">
                  ↓
                </span>
              </button>

            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="mt-6 overflow-hidden rounded-2xl border border-slate-200/80 bg-white shadow-sm">

          <div className="flex overflow-x-auto scrollbar-none">
            {TABS.map((tab) => {
              const isActive = activeTab === tab;

              return (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`relative shrink-0 px-4 py-4 text-sm font-semibold transition-all sm:px-6 ${
                    isActive
                      ? "text-accent"
                      : "text-slate-400 hover:bg-slate-50 hover:text-slate-700"
                  }`}
                >
                  {tab}

                  {isActive && (
                    <span className="absolute inset-x-4 bottom-0 h-0.5 rounded-full bg-accent sm:inset-x-6" />
                  )}
                </button>
              );
            })}
          </div>

        </div>

        {/* Content */}
        <div
          className={`mt-5 transition-opacity duration-200 ${
            refining ? "pointer-events-none opacity-50" : ""
          }`}
        >

          {activeTab === "Model Classes" && (
            <div className="overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-xl shadow-slate-200/40">

              <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/70 px-5 py-3.5 sm:px-6">
                <div className="flex items-center gap-2.5">
                  <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-accent/10 text-xs text-accent">
                    &lt;/&gt;
                  </span>

                  <div>
                    <p className="text-xs font-semibold text-slate-700">
                      Model Classes
                    </p>
                    <p className="text-[10px] text-slate-400">
                      {session.language} source code
                    </p>
                  </div>
                </div>

                <span className="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-medium text-slate-400">
                  {modelLanguage}
                </span>
              </div>

              <CodeTab
                code={session.modelClasses}
                language={modelLanguage}
              />
            </div>
          )}

          {activeTab === "Migration Script" && (
            <div className="overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-xl shadow-slate-200/40">

              <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/70 px-5 py-3.5 sm:px-6">
                <div className="flex items-center gap-2.5">
                  <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-indigo-50 text-xs text-indigo-500">
                    DB
                  </span>

                  <div>
                    <p className="text-xs font-semibold text-slate-700">
                      Migration Script
                    </p>
                    <p className="text-[10px] text-slate-400">
                      Database migration
                    </p>
                  </div>
                </div>

                <span className="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-medium text-slate-400">
                  {migrationLanguage}
                </span>
              </div>

              <CodeTab
                code={session.migrationScript}
                language={migrationLanguage}
              />
            </div>
          )}

          {activeTab === "ER Diagram" && (
            <div className="overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-xl shadow-slate-200/40">

              <div className="border-b border-slate-100 bg-slate-50/70 px-5 py-4 sm:px-6">
                <p className="text-xs font-semibold text-slate-700">
                  Entity Relationship Diagram
                </p>

                <p className="mt-1 text-[11px] text-slate-400">
                  Visual representation of your database relationships
                </p>
              </div>

              <div className="min-h-[400px] p-4 sm:p-8">
                <MermaidDiagram diagram={session.erDiagram} />
              </div>

            </div>
          )}

          {activeTab === "Explanation" && (
            <div className="overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-xl shadow-slate-200/40">

              <div className="border-b border-slate-100 bg-slate-50/70 px-5 py-4 sm:px-6">
                <div className="flex items-center gap-2.5">
                  <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-purple-50 text-sm text-purple-500">
                    ✦
                  </span>

                  <div>
                    <p className="text-xs font-semibold text-slate-700">
                      Schema Explanation
                    </p>

                    <p className="mt-0.5 text-[10px] text-slate-400">
                      AI-generated explanation of your database design
                    </p>
                  </div>
                </div>
              </div>

              <div className="p-6 text-sm leading-7 text-slate-600 whitespace-pre-wrap sm:p-8">
                {session.explanation || "No explanation available for this session."}
              </div>

            </div>
          )}

          {activeTab === "Raw JSON" && (
            <div className="overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-xl shadow-slate-200/40">

              <div className="flex items-center justify-between border-b border-slate-100 bg-slate-50/70 px-5 py-3.5 sm:px-6">
                <div>
                  <p className="text-xs font-semibold text-slate-700">
                    Raw JSON
                  </p>

                  <p className="mt-0.5 text-[10px] text-slate-400">
                    Complete generated schema response
                  </p>
                </div>

                <span className="rounded-md border border-slate-200 bg-white px-2 py-1 text-[10px] font-medium text-slate-400">
                  JSON
                </span>
              </div>

              <CodeTab
                code={session.rawJson}
                language="json"
              />
            </div>
          )}

        </div>

        {/* Error */}
        {error && (
          <div className="mt-5 flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 px-4 py-3.5 text-sm text-red-700">
            <div className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-100 font-bold">
              !
            </div>

            <p className="leading-5">
              {error}
            </p>
          </div>
        )}

        {/* Refine */}
        <div className="mt-6 overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-xl shadow-slate-200/40">

          <div className="border-b border-slate-100 bg-gradient-to-r from-slate-50/80 to-white px-5 py-4 sm:px-6">
            <div className="flex items-center gap-3">

              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-accent/10 text-accent">
                ✦
              </div>

              <div>
                <p className="text-sm font-semibold text-slate-800">
                  Refine your schema
                </p>

                <p className="mt-0.5 text-xs text-slate-400">
                  Tell the AI what you want to change
                </p>
              </div>

            </div>
          </div>

          <div className="p-5 sm:p-6">

            <div className="flex flex-col gap-3 sm:flex-row">

              <input
                value={refineNote}
                onChange={(e) => setRefineNote(e.target.value)}
                placeholder="e.g. Add a loyalty points field to customers"
                className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50/50 px-4 py-3 text-sm text-slate-800 placeholder:text-slate-400 outline-none transition-all focus:border-accent focus:bg-white focus:ring-4 focus:ring-accent/10"
              />

              <button
                onClick={handleRefine}
                disabled={refining || !refineNote.trim()}
                className="group inline-flex shrink-0 items-center justify-center gap-2 rounded-xl bg-slate-950 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-slate-900/10 transition-all duration-300 hover:-translate-y-0.5 hover:bg-accent hover:shadow-lg hover:shadow-accent/20 disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:translate-y-0 disabled:hover:bg-slate-950"
              >
                {refining && (
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                )}

                {refining ? "Updating..." : "Update"}

                {!refining && (
                  <span className="transition-transform duration-300 group-hover:translate-x-1">
                    →
                  </span>
                )}
              </button>

            </div>

            <p className="mt-3 text-[11px] text-slate-400">
              Example: “Add a status field to orders” or “Create a relationship between users and reviews”.
            </p>

          </div>
        </div>

        {/* Footer hint */}
        <div className="flex justify-center py-6">
          <p className="text-[11px] text-slate-400">
            Generated with SchemaGenie AI
          </p>
        </div>

      </div>
    </div>
  );
}

