import type { GameStatus } from '../api/gameApi';

interface GameOverlayProps {
  gameStatus: GameStatus;
  onNewGame: () => void;
  onKeepPlaying: () => void;
}

export function GameOverlay({ gameStatus, onNewGame, onKeepPlaying }: GameOverlayProps) {
  if (gameStatus === 'PLAYING') return null;

  const isWin = gameStatus === 'WON';

  return (
    <div className={`overlay ${isWin ? 'overlay-win' : 'overlay-lose'}`}>
      <div className="overlay-content">
        <h2>{isWin ? 'You Win!' : 'Game Over'}</h2>
        <div className="overlay-buttons">
          {isWin && (
            <button className="btn" onClick={onKeepPlaying}>Keep Playing</button>
          )}
          <button className="btn" onClick={onNewGame}>
            {isWin ? 'New Game' : 'Try Again'}
          </button>
        </div>
      </div>
    </div>
  );
}
