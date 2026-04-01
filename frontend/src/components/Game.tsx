import { useGame } from "../hooks/useGame";
import { Board } from "./Board";
import { ScoreBoard } from "./ScoreBoard";
import { GameControls } from "./GameControls";
import { GameOverlay } from "./GameOverlay";

export function Game() {
  const {
    board,
    score,
    bestScore,
    gameStatus,
    isLoading,
    spawnedCell,
    moves,
    aiHint,
    autoPlay,
    startNewGame,
    getAiHint,
    continueAfterWin,
    toggleAutoPlay,
  } = useGame();

  return (
    <div className="game">
      <h1 className="title">2048</h1>
      <ScoreBoard score={score} bestScore={bestScore} />
      <GameControls
        onNewGame={startNewGame}
        onAiHint={getAiHint}
        onToggleAutoPlay={toggleAutoPlay}
        isLoading={isLoading}
        aiHint={aiHint}
        autoPlay={autoPlay}
      />
      <div className="board-wrapper">
        <Board board={board} spawnedCell={spawnedCell} moves={moves} />
        <GameOverlay
          gameStatus={gameStatus}
          onNewGame={startNewGame}
          onKeepPlaying={continueAfterWin}
        />
      </div>
      <p className="instructions">Use arrow keys, WASD, or swipe to play</p>
    </div>
  );
}
