import { useState, useEffect, useRef } from "react";

const STATUS_STYLES = {
  applied:   { bg: "#dbeafe", color: "#1e40af" },
  interview: { bg: "#dcfce7", color: "#166534" },
  "phone screen": { bg: "#dcfce7", color: "#166534" },
  oa:        { bg: "#fef9c3", color: "#854d0e" },
  "online assessment": { bg: "#fef9c3", color: "#854d0e" },
  rejected:  { bg: "#fee2e2", color: "#991b1b" },
  offer:     { bg: "#d1fae5", color: "#065f46" },
  ghosted:   { bg: "#f1f0ec", color: "#57574f" },
};

const AGENT_STYLES = {
  decision:     { label: "decision",     bg: "#fef9c3", color: "#854d0e" },
  career:       { label: "career",       bg: "#dbeafe", color: "#1e40af" },
  motivational: { label: "motivational", bg: "#dcfce7", color: "#166534" },
  general:      { label: "general",      bg: "#f1f0ec", color: "#57574f" },
};

function Badge({ status }) {
  const key = (status || "").toLowerCase();
  const style = STATUS_STYLES[key] || STATUS_STYLES["applied"];
  return (
    <span style={{
      display: "inline-block",
      fontSize: 11,
      fontWeight: 500,
      padding: "3px 9px",
      borderRadius: 99,
      background: style.bg,
      color: style.color,
      whiteSpace: "nowrap",
    }}>
      {status || "—"}
    </span>
  );
}

function fmt(d) {
  if (!d) return "—";
  try { return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric" }); }
  catch { return d; }
}

function StatCard({ value, label }) {
  return (
    <div style={{
      background: "#fff",
      border: "0.5px solid rgba(0,0,0,0.1)",
      borderRadius: 8,
      padding: "14px 16px",
    }}>
      <div style={{ fontSize: 24, fontWeight: 500, fontFamily: "monospace" }}>{value}</div>
      <div style={{ fontSize: 11, color: "#6b6b67", textTransform: "uppercase", letterSpacing: "0.07em", marginTop: 3 }}>{label}</div>
    </div>
  );
}

function ApplicationsTable({ apps, loading }) {
  return (
    <div style={{ background: "#fff", border: "0.5px solid rgba(0,0,0,0.1)", borderRadius: 12, overflow: "hidden" }}>
      <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13, tableLayout: "fixed" }}>
        <colgroup>
          <col style={{ width: "26%" }} />
          <col style={{ width: "34%" }} />
          <col style={{ width: "20%" }} />
          <col style={{ width: "20%" }} />
        </colgroup>
        <thead>
          <tr style={{ background: "#f3f2ef", borderBottom: "0.5px solid rgba(0,0,0,0.1)" }}>
            {["company", "role", "status", "applied"].map(h => (
              <th key={h} style={{ padding: "10px 16px", textAlign: "left", fontSize: 10, fontWeight: 500, textTransform: "uppercase", letterSpacing: "0.08em", color: "#6b6b67" }}>
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr><td colSpan={4} style={{ padding: 24, textAlign: "center", color: "#9b9b96", fontSize: 13 }}>loading...</td></tr>
          ) : apps.length === 0 ? (
            <tr><td colSpan={4} style={{ padding: 24, textAlign: "center", color: "#9b9b96", fontSize: 13 }}>no applications yet</td></tr>
          ) : apps.map((a, i) => (
            <tr key={i} style={{ borderBottom: i < apps.length - 1 ? "0.5px solid rgba(0,0,0,0.08)" : "none" }}
              onMouseEnter={e => e.currentTarget.style.background = "#f3f2ef"}
              onMouseLeave={e => e.currentTarget.style.background = "transparent"}>
              <td style={{ padding: "11px 16px", fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{a.company || "—"}</td>
              <td style={{ padding: "11px 16px", color: "#6b6b67", fontSize: 12, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{a.role || "—"}</td>
              <td style={{ padding: "11px 16px" }}><Badge status={a.status} /></td>
              <td style={{ padding: "11px 16px", color: "#6b6b67", fontSize: 12 }}>{fmt(a.dateApplied)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function Chat() {
  const [messages, setMessages] = useState([{ role: "assistant", text: "hey — ask me anything about your search." }]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages]);

  async function send() {
    const q = input.trim();
    if (!q || loading) return;
    setInput("");
    setMessages(m => [...m, { role: "user", text: q }, { role: "thinking", text: "thinking..." }]);
    setLoading(true);
    try {
      const res = await fetch("/v1/talk", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: "user-123",
          sessionId: "session-abc",
          message: q
        }),
      });
      const text = await res.text();
      setMessages(m => [...m.slice(0, -1), { role: "assistant", text }]);
    } catch {
      setMessages(m => [...m.slice(0, -1), { role: "assistant", text: "error reaching /v1/talk" }]);
    }
    setLoading(false);
  }
  return (
    <div style={{ background: "#fff", border: "0.5px solid rgba(0,0,0,0.1)", borderRadius: 12, display: "flex", flexDirection: "column", minHeight: 460, overflow: "hidden" }}>
      <div style={{ flex: 1, overflowY: "auto", padding: 16, display: "flex", flexDirection: "column", gap: 10 }}>
        {messages.map((m, i) => (
          <div key={i} style={{ alignSelf: m.role === "user" ? "flex-end" : "flex-start", maxWidth: "88%" }}>
            {m.role === "assistant" && m.agent && (() => {
              const s = AGENT_STYLES[m.agent] || AGENT_STYLES.general;
              return (
                <span style={{
                  fontSize: 10, fontWeight: 500, padding: "2px 7px",
                  borderRadius: 99, background: s.bg, color: s.color,
                  display: "inline-block", marginBottom: 4,
                }}>
                  {s.label}
                </span>
              );
            })()}
            <div style={{
              padding: "9px 13px",
              borderRadius: 12,
              borderBottomRightRadius: m.role === "user" ? 3 : 12,
              borderBottomLeftRadius: m.role !== "user" ? 3 : 12,
              fontSize: 13,
              lineHeight: 1.55,
              background: m.role === "user" ? "#1a1a18" : "#f3f2ef",
              color: m.role === "user" ? "#f3f2ef" : m.role === "thinking" ? "#9b9b96" : "#1a1a18",
              fontStyle: m.role === "thinking" ? "italic" : "normal",
            }}>
              {m.text}
            </div>
          </div>
        ))}
        <div ref={bottomRef} />
      </div>
      <div style={{ borderTop: "0.5px solid rgba(0,0,0,0.1)", padding: "10px 12px", display: "flex", gap: 8 }}>
        <input
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === "Enter" && send()}
          placeholder="ask something..."
          style={{ flex: 1, fontFamily: "inherit", fontSize: 13, padding: "8px 12px", border: "0.5px solid rgba(0,0,0,0.18)", borderRadius: 8, background: "#f3f2ef", color: "#1a1a18", outline: "none" }}
        />
        <button
          onClick={send}
          disabled={loading}
          style={{ fontFamily: "inherit", fontSize: 13, padding: "8px 14px", border: "0.5px solid rgba(0,0,0,0.18)", borderRadius: 8, background: "#fff", color: "#1a1a18", cursor: loading ? "not-allowed" : "pointer", opacity: loading ? 0.4 : 1 }}
        >
          send
        </button>
      </div>
    </div>
  );
}

export default function App() {
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("/v1/get-applications")
      .then(r => r.json())
      .then(data => { setApps(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  const rejected = apps.filter(a => (a.status || "").toLowerCase() === "rejected").length;
  const active = apps.filter(a => !["rejected", "offer"].includes((a.status || "").toLowerCase())).length;

  return (
    <div style={{ fontFamily: "'DM Sans', system-ui, sans-serif", background: "#f9f8f6", minHeight: "100vh", padding: "32px 24px", color: "#1a1a18" }}>
      <header style={{ marginBottom: 28 }}>
        <h1 style={{ fontSize: 22, fontWeight: 500, letterSpacing: "-0.01em" }}>job search</h1>
        <p style={{ fontSize: 13, color: "#6b6b67", marginTop: 3 }}>
          {loading ? "loading..." : `${apps.length} companies tracked`}
        </p>
      </header>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 320px", gap: 20, alignItems: "start" }}>
        <div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 10, marginBottom: 16 }}>
            <StatCard value={loading ? "—" : apps.length} label="applied" />
            <StatCard value={loading ? "—" : active} label="active" />
            <StatCard value={loading ? "—" : rejected} label="rejected" />
          </div>
          <ApplicationsTable apps={apps} loading={loading} />
        </div>

        <div>
          <p style={{ fontSize: 10, fontWeight: 500, textTransform: "uppercase", letterSpacing: "0.08em", color: "#6b6b67", marginBottom: 10 }}>chat</p>
          <Chat />
        </div>
      </div>
    </div>
  );
}