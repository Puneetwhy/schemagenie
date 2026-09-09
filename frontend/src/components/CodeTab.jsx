import { useState } from "react";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { oneDark } from "react-syntax-highlighter/dist/esm/styles/prism";

export default function CodeTab({ code, language }) {
  const [copied, setCopied] = useState(false);

  async function handleCopy() {
    await navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  }

  return (
    <div className="code-tab relative overflow-hidden rounded-2xl border border-slate-800/80 bg-[#0b0d12] shadow-[0_20px_60px_rgba(15,23,42,0.12)]">
      {/* Top bar */}
      <div className="code-toolbar flex h-12 items-center justify-between border-b border-white/[0.07] bg-white/[0.025] px-4">
        <div className="flex items-center gap-3">
          {/* Traffic lights */}
          <div className="flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-full bg-red-400/80" />
            <span className="h-2.5 w-2.5 rounded-full bg-yellow-400/80" />
            <span className="h-2.5 w-2.5 rounded-full bg-green-400/80" />
          </div>

          {/* Language */}
          <span className="rounded-md border border-white/[0.06] bg-white/[0.035] px-2 py-1 text-[10px] font-medium uppercase tracking-wider text-slate-400">
            {language || "code"}
          </span>
        </div>

        {/* Copy */}
        <button
          type="button"
          onClick={handleCopy}
          className={`code-copy-button group inline-flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs font-medium transition-all duration-200 ${
            copied
              ? "border-emerald-400/20 bg-emerald-400/10 text-emerald-300"
              : "border-white/[0.08] bg-white/[0.04] text-slate-400 hover:border-white/[0.14] hover:bg-white/[0.08] hover:text-white"
          }`}
        >
          {copied ? <CheckIcon /> : <CopyIcon />}

          <span>{copied ? "Copied!" : "Copy"}</span>
        </button>
      </div>

      {/* Code */}
      <div className="code-scrollbar max-h-[650px] overflow-auto">
        <SyntaxHighlighter
          language={language}
          style={oneDark}
          customStyle={{
            margin: 0,
            borderRadius: 0,
            background: "transparent",
            fontSize: "0.8rem",
            lineHeight: "1.75",
            padding: "1.5rem",
            minHeight: "180px",
          }}
          codeTagProps={{
            style: {
              fontFamily:
                "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace",
            },
          }}
          wrapLongLines
        >
          {code || ""}
        </SyntaxHighlighter>
      </div>

      {/* Bottom glow */}
      <div className="pointer-events-none absolute bottom-0 left-1/2 h-px w-2/3 -translate-x-1/2 bg-gradient-to-r from-transparent via-indigo-400/30 to-transparent" />
    </div>
  );
}

function CopyIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-3.5 w-3.5"
      aria-hidden="true"
    >
      <rect
        x="7"
        y="7"
        width="9"
        height="9"
        rx="1.8"
        stroke="currentColor"
        strokeWidth="1.4"
      />

      <path
        d="M13 7V5.5A1.5 1.5 0 0 0 11.5 4h-6A1.5 1.5 0 0 0 4 5.5v6A1.5 1.5 0 0 0 5.5 13H7"
        stroke="currentColor"
        strokeWidth="1.4"
      />
    </svg>
  );
}

function CheckIcon() {
  return (
    <svg
      viewBox="0 0 20 20"
      fill="none"
      className="h-3.5 w-3.5"
      aria-hidden="true"
    >
      <path
        d="m5 10.2 3.1 3.1L15.5 6"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}