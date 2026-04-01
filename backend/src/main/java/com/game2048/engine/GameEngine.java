package com.game2048.engine;

import com.game2048.model.Board;
import com.game2048.model.Direction;
import com.game2048.model.GameState;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class GameEngine {

    private final TileSpawner spawner;

    public GameEngine(TileSpawner spawner) {
        this.spawner = spawner;
    }

    /**
     * A tile movement: from (fromRow, fromCol) to (toRow, toCol).
     * If merged is true, this tile was consumed into a merge at the destination.
     */
    public record TileMove(int fromRow, int fromCol, int toRow, int toCol, boolean merged) {}

    public MoveResult applyMove(Board board, Direction direction, int currentScore) {
        Board original = board.copy();
        Board moved = board.copy();
        List<TileMove> moves = new ArrayList<>();
        int scoreGained = applyDirection(moved, direction, moves);

        if (original.equals(moved)) {
            return new MoveResult(original, currentScore, null, List.of());
        }

        int newScore = currentScore + scoreGained;
        int[] spawned = spawner.spawn(moved);
        return new MoveResult(moved, newScore, spawned, moves);
    }

    public ApplyResult applyMoveRaw(Board board, Direction direction) {
        Board moved = board.copy();
        int scoreGained = applyDirection(moved, direction, null);
        boolean changed = !board.equals(moved);
        return new ApplyResult(moved, scoreGained, changed);
    }

    public GameState determineState(Board board) {
        if (board.containsValue(2048)) {
            return GameState.WON;
        }
        if (!board.emptyCells().isEmpty()) {
            return GameState.PLAYING;
        }
        boolean canMerge = board.findFirst((r, c) -> {
            Integer val = board.get(r, c);
            if (val == null) return null;
            if (c + 1 < Board.SIZE && val.equals(board.get(r, c + 1))) return true;
            if (r + 1 < Board.SIZE && val.equals(board.get(r + 1, c))) return true;
            return null;
        }) != null;
        return canMerge ? GameState.PLAYING : GameState.LOST;
    }

    private int applyDirection(Board board, Direction direction, List<TileMove> moves) {
        switch (direction) {
            case RIGHT -> reverseRows(board);
            case UP -> transpose(board);
            case DOWN -> { transpose(board); reverseRows(board); }
            default -> {}
        }

        int score = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            score += slideAndMergeRow(board, r, moves, direction);
        }

        switch (direction) {
            case RIGHT -> reverseRows(board);
            case UP -> transpose(board);
            case DOWN -> { reverseRows(board); transpose(board); }
            default -> {}
        }

        return score;
    }

    /**
     * Slides and merges a single row to the left in-place.
     * Records tile movements in the moves list (in original board coordinates).
     */
    private int slideAndMergeRow(Board board, int row, List<TileMove> moves, Direction direction) {
        Integer[] cells = new Integer[Board.SIZE];
        int[] originalCol = new int[Board.SIZE]; // track where each value came from
        for (int c = 0; c < Board.SIZE; c++) {
            cells[c] = board.get(row, c);
            originalCol[c] = c;
        }

        // Compact: shift non-nulls left, track original positions
        int idx = 0;
        Integer[] compacted = new Integer[Board.SIZE];
        int[] compactedOrigin = new int[Board.SIZE];
        for (int c = 0; c < Board.SIZE; c++) {
            if (cells[c] != null) {
                compacted[idx] = cells[c];
                compactedOrigin[idx] = originalCol[c];
                idx++;
            }
        }

        // Merge adjacent equal tiles
        int score = 0;
        Integer[] result = new Integer[Board.SIZE];
        int[] resultOrigin1 = new int[Board.SIZE]; // primary source
        int[] resultOrigin2 = new int[Board.SIZE]; // secondary source (merge partner), -1 if none
        int ri = 0;
        for (int i = 0; i < Board.SIZE; i++) {
            resultOrigin2[ri] = -1;
            if (compacted[i] == null) break;
            if (i + 1 < Board.SIZE && compacted[i].equals(compacted[i + 1])) {
                result[ri] = compacted[i] * 2;
                score += result[ri];
                resultOrigin1[ri] = compactedOrigin[i];
                resultOrigin2[ri] = compactedOrigin[i + 1];
                ri++;
                i++; // skip next
            } else {
                result[ri] = compacted[i];
                resultOrigin1[ri] = compactedOrigin[i];
                resultOrigin2[ri] = -1;
                ri++;
            }
        }

        // Record moves (converting from LEFT-transformed coords back to original)
        if (moves != null) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (result[c] == null) continue;
                int fromCol1 = resultOrigin1[c];
                int[] from1 = untransform(row, fromCol1, direction);
                int[] to = untransform(row, c, direction);

                if (from1[0] != to[0] || from1[1] != to[1]) {
                    moves.add(new TileMove(from1[0], from1[1], to[0], to[1], false));
                }

                if (resultOrigin2[c] != -1) {
                    int fromCol2 = resultOrigin2[c];
                    int[] from2 = untransform(row, fromCol2, direction);
                    moves.add(new TileMove(from2[0], from2[1], to[0], to[1], true));
                }
            }
        }

        // Write back
        for (int c = 0; c < Board.SIZE; c++) {
            board.set(row, c, result[c]);
        }
        return score;
    }

    /**
     * Converts (row, col) from LEFT-transformed coordinates back to original coordinates
     * based on the direction that was applied.
     */
    private int[] untransform(int row, int col, Direction direction) {
        return switch (direction) {
            case LEFT -> new int[]{row, col};
            case RIGHT -> new int[]{row, Board.SIZE - 1 - col};
            case UP -> new int[]{col, row};
            case DOWN -> new int[]{Board.SIZE - 1 - col, row};
        };
    }

    private void transpose(Board board) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = r + 1; c < Board.SIZE; c++) {
                Integer temp = board.get(r, c);
                board.set(r, c, board.get(c, r));
                board.set(c, r, temp);
            }
        }
    }

    private void reverseRows(Board board) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE / 2; c++) {
                Integer temp = board.get(r, c);
                board.set(r, c, board.get(r, Board.SIZE - 1 - c));
                board.set(r, Board.SIZE - 1 - c, temp);
            }
        }
    }

    public record MoveResult(Board board, int score, int[] spawnedCell, List<TileMove> moves) {}
    public record ApplyResult(Board board, int scoreGained, boolean changed) {}
}
