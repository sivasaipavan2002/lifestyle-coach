import { useState } from "react";
import NameGate from "./components/NameGate";
import SignupForm from "./components/SignupForm";
import AnalysisView from "./components/AnalysisView";
import ChatView from "./components/ChatView";
import { getProfile, saveProfile } from "./api";
import "./App.css";

const STORAGE_KEY = "lifestyle-coach.name";

export default function App() {
  const [name, setName] = useState(null);
  const [profile, setProfile] = useState(null);
  const [stage, setStage] = useState("name-gate"); // name-gate | signup | analysis | chat
  const [checking, setChecking] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [analysis, setAnalysis] = useState(null);

  async function handleContinue(enteredName) {
    setChecking(true);
    setError(null);

    try {
      const existingProfile = await getProfile(enteredName);
      setName(enteredName);
      setProfile(existingProfile);
      localStorage.setItem(STORAGE_KEY, enteredName);
      setStage("chat");
    } catch (err) {
      setName(enteredName);
      setStage("signup");
    } finally {
      setChecking(false);
    }
  }

  async function handleSignup(profilePayload) {
    setSubmitting(true);
    setError(null);

    try {
      const response = await saveProfile(profilePayload);
      localStorage.setItem(STORAGE_KEY, profilePayload.name);
      setProfile(response.profile);

      if (response.analysis) {
        setAnalysis(response.analysis);
        setStage("analysis");
      } else {
        setStage("chat");
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  function handleSwitchUser() {
    localStorage.removeItem(STORAGE_KEY);
    setName(null);
    setProfile(null);
    setAnalysis(null);
    setError(null);
    setStage("name-gate");
  }

  return (
    <div className="app-shell">
      {stage === "name-gate" && (
        <NameGate
          onContinue={handleContinue}
          checking={checking}
          error={error}
        />
      )}

      {stage === "signup" && (
        <SignupForm
          name={name}
          onSubmit={handleSignup}
          submitting={submitting}
          error={error}
        />
      )}

      {stage === "analysis" && (
        <AnalysisView
          name={name}
          analysis={analysis}
          onContinue={() => setStage("chat")}
        />
      )}

      {stage === "chat" && (
        <ChatView name={name} profile={profile} onSwitchUser={handleSwitchUser} />
      )}
    </div>
  );
}
