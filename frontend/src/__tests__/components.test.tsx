import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { ScoreBoard } from "../components/ScoreBoard";
import { GameOverlay } from "../components/GameOverlay";
import { GameControls } from "../components/GameControls";

describe("ScoreBoard", () => {
  it("renders score and best score", () => {
    render(<ScoreBoard score={128} bestScore={512} />);
    expect(screen.getByText("128")).toBeDefined();
    expect(screen.getByText("512")).toBeDefined();
  });
});

describe("GameOverlay", () => {
  it("renders nothing when PLAYING", () => {
    const { container } = render(
      <GameOverlay
        gameStatus="PLAYING"
        onNewGame={() => {}}
        onKeepPlaying={() => {}}
      />,
    );
    expect(container.innerHTML).toBe("");
  });

  it("shows Game Over on LOST", () => {
    render(
      <GameOverlay
        gameStatus="LOST"
        onNewGame={() => {}}
        onKeepPlaying={() => {}}
      />,
    );
    expect(screen.getByText("Game Over")).toBeDefined();
    expect(screen.getByText("Try Again")).toBeDefined();
  });

  it("shows You Win with Keep Playing on WON", () => {
    render(
      <GameOverlay
        gameStatus="WON"
        onNewGame={() => {}}
        onKeepPlaying={() => {}}
      />,
    );
    expect(screen.getByText("You Win!")).toBeDefined();
    expect(screen.getByText("Keep Playing")).toBeDefined();
    expect(screen.getByText("New Game")).toBeDefined();
  });
});

describe("GameControls", () => {
  it("shows AI hint direction when provided", () => {
    render(
      <GameControls
        onNewGame={() => {}}
        onAiHint={() => {}}
        onToggleAutoPlay={() => {}}
        isLoading={false}
        aiHint="UP"
        autoPlay={false}
      />,
    );
    expect(screen.getByText(/AI:.*UP/)).toBeDefined();
  });

  it("shows Stop AI when auto-playing", () => {
    render(
      <GameControls
        onNewGame={() => {}}
        onAiHint={() => {}}
        onToggleAutoPlay={() => {}}
        isLoading={false}
        aiHint={null}
        autoPlay={true}
      />,
    );
    expect(screen.getByText("Stop AI")).toBeDefined();
  });
});
