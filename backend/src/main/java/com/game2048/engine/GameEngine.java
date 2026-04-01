package com.game2048.engine;

import com.game2048.model.Board;
import com.game2048.model.Direction;
import com.game2048.model.GameState;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class GameEngine {

  private final TileSpawner spawner;

  /**
   * A tile movement: from (fromRow, fromCol) to (toRow, toCol). If merged is true, this tile was
   * consumed into a merge at the destination.
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
    boolean canMerge =
        board.findFirst(
                (row, col) -> {
                  Integer val = board.get(row, col);
                  if (val == null) return null;
                  if (col + 1 < Board.SIZE && val.equals(board.get(row, col + 1))) return true;
                  if (row + 1 < Board.SIZE && val.equals(board.get(row + 1, col))) return true;
                  return null;
                })
            != null;
    return canMerge ? GameState.PLAYING : GameState.LOST;
  }

  private int applyDirection(Board board, Direction direction, List<TileMove> moves) {
    switch (direction) {
      case RIGHT -> reverseRows(board);
      case UP -> transpose(board);
      case DOWN -> {
        transpose(board);
        reverseRows(board);
      }
      default -> {}
    }

    int score = 0;
    for (int row = 0; row < Board.SIZE; row++) {
      score += slideAndMergeRow(board, row, moves, direction);
    }

    switch (direction) {
      case RIGHT -> reverseRows(board);
      case UP -> transpose(board);
      case DOWN -> {
        reverseRows(board);
        transpose(board);
      }
      default -> {}
    }

    return score;
  }

  /**
   * Slides and merges a single row to the left in-place. Records tile movements in the moves list
   * (in original board coordinates).
   */
  private int slideAndMergeRow(Board board, int row, List<TileMove> moves, Direction direction) {
    Integer[] cells = new Integer[Board.SIZE];
    int[] originalCol = new int[Board.SIZE];
    for (int col = 0; col < Board.SIZE; col++) {
      cells[col] = board.get(row, col);
      originalCol[col] = col;
    }

    // Compact: shift non-nulls left, track original positions
    int writeIdx = 0;
    Integer[] compacted = new Integer[Board.SIZE];
    int[] compactedOrigin = new int[Board.SIZE];
    for (int col = 0; col < Board.SIZE; col++) {
      if (cells[col] != null) {
        compacted[writeIdx] = cells[col];
        compactedOrigin[writeIdx] = originalCol[col];
        writeIdx++;
      }
    }

    // Merge adjacent equal tiles
    int score = 0;
    Integer[] result = new Integer[Board.SIZE];
    int[] resultOrigin1 = new int[Board.SIZE];
    int[] resultOrigin2 = new int[Board.SIZE];
    int resultIdx = 0;
    for (int i = 0; i < Board.SIZE; i++) {
      resultOrigin2[resultIdx] = -1;
      if (compacted[i] == null) break;
      if (i + 1 < Board.SIZE && compacted[i].equals(compacted[i + 1])) {
        result[resultIdx] = compacted[i] * 2;
        score += result[resultIdx];
        resultOrigin1[resultIdx] = compactedOrigin[i];
        resultOrigin2[resultIdx] = compactedOrigin[i + 1];
        resultIdx++;
        i++; // skip merged tile
      } else {
        result[resultIdx] = compacted[i];
        resultOrigin1[resultIdx] = compactedOrigin[i];
        resultOrigin2[resultIdx] = -1;
        resultIdx++;
      }
    }

    // Record moves (converting from LEFT-transformed coords back to original)
    if (moves != null) {
      for (int col = 0; col < Board.SIZE; col++) {
        if (result[col] == null) continue;
        int[] from1 = untransform(row, resultOrigin1[col], direction);
        int[] to = untransform(row, col, direction);

        if (from1[0] != to[0] || from1[1] != to[1]) {
          moves.add(new TileMove(from1[0], from1[1], to[0], to[1], false));
        }

        if (resultOrigin2[col] != -1) {
          int[] from2 = untransform(row, resultOrigin2[col], direction);
          moves.add(new TileMove(from2[0], from2[1], to[0], to[1], true));
        }
      }
    }

    // Write back
    for (int col = 0; col < Board.SIZE; col++) {
      board.set(row, col, result[col]);
    }
    return score;
  }

  /**
   * Converts (row, col) from LEFT-transformed coordinates back to original coordinates based on the
   * direction that was applied.
   */
  private int[] untransform(int row, int col, Direction direction) {
    return switch (direction) {
      case LEFT -> new int[] {row, col};
      case RIGHT -> new int[] {row, Board.SIZE - 1 - col};
      case UP -> new int[] {col, row};
      case DOWN -> new int[] {Board.SIZE - 1 - col, row};
    };
  }

  private void transpose(Board board) {
    for (int row = 0; row < Board.SIZE; row++) {
      for (int col = row + 1; col < Board.SIZE; col++) {
        Integer temp = board.get(row, col);
        board.set(row, col, board.get(col, row));
        board.set(col, row, temp);
      }
    }
  }

  private void reverseRows(Board board) {
    for (int row = 0; row < Board.SIZE; row++) {
      for (int col = 0; col < Board.SIZE / 2; col++) {
        Integer temp = board.get(row, col);
        board.set(row, col, board.get(row, Board.SIZE - 1 - col));
        board.set(row, Board.SIZE - 1 - col, temp);
      }
    }
  }

  public record MoveResult(Board board, int score, int[] spawnedCell, List<TileMove> moves) {}

  public record ApplyResult(Board board, int scoreGained, boolean changed) {}
}
