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
    <div className="relative">
      <button
        onClick={handleCopy}
        className="absolute top-3 right-3 z-10 text-xs bg-gray-800 text-gray-200 px-3 py-1 rounded-full hover:bg-gray-700"
      >
        {copied ? "Copied!" : "Copy"}
      </button>
      <SyntaxHighlighter
        language={language}
        style={oneDark}
        customStyle={{ borderRadius: "0.75rem", fontSize: "0.8rem", padding: "1.5rem 1rem" }}
        wrapLongLines
      >
        {code || ""}
      </SyntaxHighlighter>
    </div>
  );
}
