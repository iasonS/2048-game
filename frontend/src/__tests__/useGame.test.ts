import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, act, waitFor } from "@testing-library/react";
import * as api from "../api/gameApi";
import { useGame } from "../hooks/useGame";

vi.mock("../api/gameApi");

const emptyBoard: api.Board = Array.from({ length: 4 }, () =>
  Array(4).fill(null),
);

function makeBoard(overrides: [number, number, number][]): api.Board {
  const board: api.Board = Array.from({ length: 4 }, () =>
    Array(4).fill(null),
  );
  for (const [r, c, v] of overrides) {
    board[r][c] = v;
  }
  return board;
}

const initialBoard = makeBoard([
  [0, 0, 2],
  [1, 1, 2],
]);

beforeEach(() => {
  vi.restoreAllMocks();
  localStorage.clear();

  vi.mocked(api.newGame).mockResolvedValue({
    board: initialBoard,
    score: 0,
  });
});

describe("useGame", () => {
  it("initializes with a new game on mount", async () => {
    const { result } = renderHook(() => useGame());

    await waitFor(() => {
      expect(result.current.board).toEqual(initialBoard);
    });
    expect(result.current.score).toBe(0);
    expect(result.current.gameStatus).toBe("PLAYING");
  });

  it("updates board and score after a valid move", async () => {
    const movedBoard = makeBoard([
      [0, 0, 4],
      [3, 3, 2],
    ]);
    vi.mocked(api.move).mockResolvedValue({
      board: movedBoard,
      score: 4,
      gameState: "PLAYING",
      spawnedCell: [3, 3],
      moves: [{ fromRow: 0, fromCol: 0, toRow: 0, toCol: 0, merged: false }],
    });

    const { result } = renderHook(() => useGame());
    await waitFor(() => expect(result.current.board).toEqual(initialBoard));

    await act(async () => {
      result.current.doMove("LEFT");
    });

    await waitFor(() => {
      expect(result.current.board).toEqual(movedBoard);
      expect(result.current.score).toBe(4);
    });
  });

  it("does not update board on no-op move", async () => {
    vi.mocked(api.move).mockResolvedValue({
      board: initialBoard,
      score: 0,
      gameState: "PLAYING",
      spawnedCell: null,
      moves: [],
    });

    const { result } = renderHook(() => useGame());
    await waitFor(() => expect(result.current.board).toEqual(initialBoard));

    await act(async () => {
      result.current.doMove("LEFT");
    });

    // Board should not have changed
    await waitFor(() => {
      expect(result.current.board).toEqual(initialBoard);
      expect(result.current.score).toBe(0);
    });
  });

  it("detects win state", async () => {
    const winBoard = makeBoard([[0, 0, 2048]]);
    vi.mocked(api.move).mockResolvedValue({
      board: winBoard,
      score: 2048,
      gameState: "WON",
      spawnedCell: [1, 0],
      moves: [{ fromRow: 0, fromCol: 0, toRow: 0, toCol: 0, merged: false }],
    });

    const { result } = renderHook(() => useGame());
    await waitFor(() => expect(result.current.board).toEqual(initialBoard));

    await act(async () => {
      result.current.doMove("LEFT");
    });

    await waitFor(() => {
      expect(result.current.gameStatus).toBe("WON");
    });
  });

  it("allows continuing after win", async () => {
    const winBoard = makeBoard([[0, 0, 2048]]);
    vi.mocked(api.move).mockResolvedValue({
      board: winBoard,
      score: 2048,
      gameState: "WON",
      spawnedCell: [1, 0],
      moves: [{ fromRow: 0, fromCol: 0, toRow: 0, toCol: 0, merged: false }],
    });

    const { result } = renderHook(() => useGame());
    await waitFor(() => expect(result.current.board).toEqual(initialBoard));

    await act(async () => {
      result.current.doMove("LEFT");
    });
    await waitFor(() => expect(result.current.gameStatus).toBe("WON"));

    // Continue playing
    act(() => {
      result.current.continueAfterWin();
    });
    expect(result.current.gameStatus).toBe("PLAYING");
  });

  it("detects lose state", async () => {
    vi.mocked(api.move).mockResolvedValue({
      board: initialBoard,
      score: 0,
      gameState: "LOST",
      spawnedCell: [2, 0],
      moves: [{ fromRow: 0, fromCol: 0, toRow: 0, toCol: 0, merged: false }],
    });

    const { result } = renderHook(() => useGame());
    await waitFor(() => expect(result.current.board).toEqual(initialBoard));

    await act(async () => {
      result.current.doMove("LEFT");
    });

    await waitFor(() => {
      expect(result.current.gameStatus).toBe("LOST");
    });
  });

  it("persists best score to localStorage", async () => {
    vi.mocked(api.move).mockResolvedValue({
      board: initialBoard,
      score: 512,
      gameState: "PLAYING",
      spawnedCell: [2, 0],
      moves: [{ fromRow: 0, fromCol: 0, toRow: 0, toCol: 0, merged: false }],
    });

    const { result } = renderHook(() => useGame());
    await waitFor(() => expect(result.current.board).toEqual(initialBoard));

    await act(async () => {
      result.current.doMove("LEFT");
    });

    await waitFor(() => {
      expect(result.current.bestScore).toBe(512);
    });
    expect(localStorage.getItem("2048-best")).toBe("512");
  });
});
