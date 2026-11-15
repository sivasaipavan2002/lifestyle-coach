export default function AnalysisView({ name, analysis, onContinue }) {
  return (
    <div className="analysis-view">
      <h1>Your lifestyle analysis, {name}</h1>
      <div className="analysis-view__body">
        {analysis.split("\n").map((line, index) =>
          line.trim() ? <p key={index}>{line}</p> : null
        )}
      </div>
      <button onClick={onContinue}>Start chatting</button>
    </div>
  );
}
