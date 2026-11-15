import { useEffect, useMemo, useRef, useState } from "react";
import { sendChatMessage } from "../api";
import NutritionDashboard from "./NutritionDashboard";
import { computeDailyGoals } from "../nutritionGoals";

export default function ChatView({ name, profile, onSwitchUser }) {
  const [messages, setMessages] = useState([
    {
      role: "coach",
      text: `Welcome back, ${name}! Ask me anything, or tell me what you ate.`,
    },
  ]);
  const [draft, setDraft] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);
  const [nutrition, setNutrition] = useState(null);
  const bottomRef = useRef(null);

  const goals = useMemo(() => computeDailyGoals(profile), [profile]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function handleSend(event) {
    event.preventDefault();

    const message = draft.trim();

    if (!message || sending) {
      return;
    }

    setMessages((prev) => [...prev, { role: "user", text: message }]);
    setDraft("");
    setSending(true);
    setError(null);

    try {
      const response = await sendChatMessage(name, message);

      setMessages((prev) => [
        ...prev,
        { role: "coach", text: response.reply },
      ]);
      setNutrition(response.nutrition);
    } catch (err) {
      setError(err.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="chat-view">
      <header className="chat-view__header">
        <div>
          <h1>AI Personal Lifestyle Coach</h1>
          <p>Chatting as {name}</p>
        </div>
        <button className="chat-view__switch" onClick={onSwitchUser}>
          Switch user
        </button>
      </header>

      <NutritionDashboard nutrition={nutrition} goals={goals} />

      <div className="chat-view__messages">
        {messages.map((message, index) => (
          <div
            key={index}
            className={`chat-bubble chat-bubble--${message.role}`}
          >
            <span className="chat-bubble__label">
              {message.role === "user" ? "You" : "Coach"}
            </span>
            <p>{message.text}</p>
          </div>
        ))}
        {sending && (
          <div className="chat-bubble chat-bubble--coach chat-bubble--pending">
            <span className="chat-bubble__label">Coach</span>
            <p>Thinking...</p>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {error && <p className="chat-view__error">{error}</p>}

      <form className="chat-view__composer" onSubmit={handleSend}>
        <input
          type="text"
          placeholder="Ask me anything, or tell me what you ate..."
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          disabled={sending}
        />
        <button type="submit" disabled={sending || !draft.trim()}>
          Send
        </button>
      </form>
    </div>
  );
}
