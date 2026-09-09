import { useEffect, useRef, useState } from "react";

export default function MermaidDiagram({ diagram }) {
  const containerRef = useRef(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function render() {
      try {
        const mermaid = (await import("mermaid")).default;

        mermaid.initialize({
          startOnLoad: false,
          theme: "default",
        });

        const id = `mermaid-${Math.random().toString(36).slice(2)}`;

        const { svg } = await mermaid.render(id, diagram);

        if (!cancelled && containerRef.current) {
          containerRef.current.innerHTML = svg;
        }
      } catch (err) {
        if (!cancelled) {
          setError("Couldn't render the diagram.");
        }
      }
    }

    render();

    return () => {
      cancelled = true;
    };
  }, [diagram]);

  if (error) {
    return (
      <div className="mermaid-error">
        <div className="mermaid-error-icon">!</div>

        <div>
          <p className="font-semibold text-red-700">
            Diagram couldn't be rendered
          </p>

          <p className="mt-0.5 text-xs text-red-500">
            Please check the generated schema and try again.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="mermaid-wrapper">
      {/* Ambient background */}
      <div className="mermaid-ambient mermaid-ambient-one" />
      <div className="mermaid-ambient mermaid-ambient-two" />

      {/* Top label */}
      <div className="mermaid-toolbar">
        <div className="flex items-center gap-2">
          <span className="mermaid-live-dot" />

          <span className="text-xs font-semibold text-gray-700">
            Entity Relationship Diagram
          </span>
        </div>

        <span className="mermaid-badge">
          ER DIAGRAM
        </span>
      </div>

      {/* Diagram canvas */}
      <div className="mermaid-canvas">
        <div
          ref={containerRef}
          className="mermaid-container overflow-auto"
        />
      </div>

      {/* Bottom hint */}
      <div className="mermaid-footer">
        <span>Scroll to explore</span>

        <span className="mermaid-footer-dot" />

        <span>Generated from your schema</span>
      </div>
    </div>
  );
}