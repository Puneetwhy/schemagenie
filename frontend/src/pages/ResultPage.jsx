import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { apiFetch, ApiError } from "../lib/api.js";
import CodeTab from "../components/CodeTab.jsx";
import MermaidDiagram from "../components/MermaidDiagram.jsx";

const TABS = ["Model Classes", "Migration Script", "ER Diagram", "Raw JSON"];

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
    window.open(`/api/schema/download/${sessionId}`, "_blank");
  }

  if (loading) {
    return <div className="max-w-4xl mx-auto px-4 py-16 text-center text-gray-500">Loading...</div>;
  }

  if (error && !session) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <p className="text-red-600">{error}</p>
      </div>
    );
  }

  if (!session) return null;

  const modelLanguage = "java";
  const migrationLanguage = session.databaseType === "MONGODB" ? "java" : "yaml";

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <span className="text-xs font-medium uppercase tracking-wide text-accent">
            {session.databaseType}
          </span>
          <p className="mt-1 text-gray-700 max-w-2xl">{session.description}</p>
        </div>
        <div className="flex gap-2">
          {user && !session.owned && (
            <button
              onClick={handleSaveToHistory}
              disabled={saveState !== "idle"}
              className="rounded-full border border-gray-300 px-4 py-2 text-sm hover:bg-gray-100 disabled:opacity-50"
            >
              {saveState === "saved" ? "Saved!" : saveState === "saving" ? "Saving..." : "Save to my history"}
            </button>
          )}
          <button
            onClick={handleDownload}
            className="rounded-full bg-accent text-white px-4 py-2 text-sm hover:bg-accent-dark"
          >
            Download ZIP
          </button>
        </div>
      </div>

      <div className="mt-8 border-b border-gray-200 flex gap-6 overflow-x-auto">
        {TABS.map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`pb-3 text-sm font-medium whitespace-nowrap border-b-2 transition ${
              activeTab === tab
                ? "border-accent text-accent"
                : "border-transparent text-gray-500 hover:text-gray-700"
            }`}
          >
            {tab}
          </button>
        ))}
      </div>

      <div className={`mt-6 ${refining ? "opacity-50 pointer-events-none" : ""}`}>
        {activeTab === "Model Classes" && <CodeTab code={session.modelClasses} language={modelLanguage} />}
        {activeTab === "Migration Script" && (
          <CodeTab code={session.migrationScript} language={migrationLanguage} />
        )}
        {activeTab === "ER Diagram" && (
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <MermaidDiagram diagram={session.erDiagram} />
          </div>
        )}
        {activeTab === "Raw JSON" && <CodeTab code={session.rawJson} language="json" />}
      </div>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      <div className="mt-8 bg-white rounded-xl border border-gray-200 p-4 flex flex-col sm:flex-row gap-3">
        <input
          value={refineNote}
          onChange={(e) => setRefineNote(e.target.value)}
          placeholder="Refine your schema... e.g. 'Add a loyalty points field to customers'"
          className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent"
        />
        <button
          onClick={handleRefine}
          disabled={refining || !refineNote.trim()}
          className="rounded-full bg-accent text-white px-5 py-2 text-sm font-medium hover:bg-accent-dark disabled:opacity-50 whitespace-nowrap"
        >
          {refining ? "Updating..." : "Update"}
        </button>
      </div>
    </div>
  );
}
