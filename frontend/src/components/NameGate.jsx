import { useState } from "react";

export default function NameGate({ onContinue, checking, error }) {
  const [name, setName] = useState("");

  function handleSubmit(event) {
    event.preventDefault();

    if (name.trim()) {
      onContinue(name.trim());
    }
  }

  return (
    <div className="name-gate">
      <h1>AI Personal Lifestyle Coach</h1>
      <p>What's your name?</p>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          autoFocus
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Your name"
        />
        <button type="submit" disabled={checking || !name.trim()}>
          {checking ? "Checking..." : "Continue"}
        </button>
      </form>
      {error && <p className="name-gate__error">{error}</p>}
    </div>
  );
}
