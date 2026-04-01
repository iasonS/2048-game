interface ScoreBoardProps {
  score: number;
  bestScore: number;
}

export function ScoreBoard({ score, bestScore }: ScoreBoardProps) {
  return (
    <div className="score-container">
      <div className="score-box">
        <div className="score-label">Score</div>
        <div className="score-value">{score}</div>
      </div>
      <div className="score-box">
        <div className="score-label">Best</div>
        <div className="score-value">{bestScore}</div>
      </div>
    </div>
  );
}
