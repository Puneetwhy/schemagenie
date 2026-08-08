import { useEffect, useRef, useState } from "react";

export default function MermaidDiagram({ diagram }) {
  const containerRef = useRef(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function render() {
      try {
        const mermaid = (await import("mermaid")).default;
        mermaid.initialize({ startOnLoad: false, theme: "default" });
        const id = `mermaid-${Math.random().toString(36).slice(2)}`;
        const { svg } = await mermaid.render(id, diagram);
        if (!cancelled && containerRef.current) {
          containerRef.current.innerHTML = svg;
        }
      } catch (err) {
        if (!cancelled) setError("Couldn't render the diagram.");
      }
    }

    render();
    return () => {
      cancelled = true;
    };
  }, [diagram]);

  if (error) {
    return <p className="text-sm text-red-600">{error}</p>;
  }

  return <div ref={containerRef} className="overflow-auto" />;
}
