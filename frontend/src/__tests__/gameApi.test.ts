import { describe, it, expect, vi, beforeEach } from "vitest";
import { newGame, move, aiSuggest } from "../api/gameApi";

const mockBoard = [
  [2, null, null, null],
  [null, null, null, null],
  [null, null, null, null],
  [null, null, null, 2],
];

beforeEach(() => {
  vi.restoreAllMocks();
});

describe("gameApi", () => {
  it("newGame calls POST /api/game/new", async () => {
    const mockResponse = { board: mockBoard, score: 0 };
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      }),
    );

    const result = await newGame();
    expect(fetch).toHaveBeenCalledWith(
      "/api/game/new",
      expect.objectContaining({ method: "POST" }),
    );
    expect(result.board).toEqual(mockBoard);
    expect(result.score).toBe(0);
  });

  it("move sends board, direction, and score", async () => {
    const mockResponse = {
      board: mockBoard,
      score: 4,
      gameState: "PLAYING",
      spawnedCell: [1, 0],
      moves: [],
    };
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      }),
    );

    const result = await move(mockBoard, "LEFT", 0);
    const callBody = JSON.parse(
      (fetch as ReturnType<typeof vi.fn>).mock.calls[0][1].body,
    );
    expect(callBody.board).toEqual(mockBoard);
    expect(callBody.direction).toBe("LEFT");
    expect(callBody.score).toBe(0);
    expect(result.gameState).toBe("PLAYING");
  });

  it("aiSuggest sends board only", async () => {
    const mockResponse = { recommendedDirection: "DOWN" };
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      }),
    );

    const result = await aiSuggest(mockBoard);
    const callBody = JSON.parse(
      (fetch as ReturnType<typeof vi.fn>).mock.calls[0][1].body,
    );
    expect(callBody.board).toEqual(mockBoard);
    expect(callBody).not.toHaveProperty("score");
    expect(result.recommendedDirection).toBe("DOWN");
  });

  it("throws on non-OK response", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({ ok: false, status: 500 }),
    );
    await expect(newGame()).rejects.toThrow("API error: 500");
  });
});
