import type { Direction } from '../api/gameApi';

interface GameControlsProps {
  onNewGame: () => void;
  onAiHint: () => void;
  onToggleAutoPlay: () => void;
  isLoading: boolean;
  aiHint: Direction | null;
  autoPlay: boolean;
}

const ARROW: Record<Direction, string> = {
  UP: '\u2191', DOWN: '\u2193', LEFT: '\u2190', RIGHT: '\u2192',
};

export function GameControls({ onNewGame, onAiHint, onToggleAutoPlay, isLoading, aiHint, autoPlay }: GameControlsProps) {
  return (
    <div className="controls">
      <button className="btn" onClick={onNewGame} disabled={isLoading}>
        New Game
      </button>
      <button className="btn btn-ai" onClick={onAiHint} disabled={isLoading || autoPlay}>
        {aiHint ? `AI: ${ARROW[aiHint]} ${aiHint}` : 'AI Hint'}
      </button>
      <button
        className={`btn btn-auto ${autoPlay ? 'btn-auto-active' : ''}`}
        onClick={onToggleAutoPlay}
      >
        {autoPlay ? 'Stop AI' : 'Auto Play'}
      </button>
    </div>
  );
}
