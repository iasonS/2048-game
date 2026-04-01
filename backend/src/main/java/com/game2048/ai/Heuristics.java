package com.game2048.ai;

import com.game2048.model.Board;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Heuristics {

  private static final double EMPTY_CELL_WEIGHT = 2.7;
  private static final double MONOTONICITY_WEIGHT = 1.0;
  private static final double SMOOTHNESS_WEIGHT = 0.1;
  private static final double CORNER_BONUS_WEIGHT = 1.0;

  public static double evaluate(Board board) {
    double empty = Math.log(Math.max(1, board.emptyCells().size())) * EMPTY_CELL_WEIGHT;
    double mono = monotonicity(board) * MONOTONICITY_WEIGHT;
    double smooth = smoothness(board) * SMOOTHNESS_WEIGHT;
    double corner = cornerBonus(board) * CORNER_BONUS_WEIGHT;
    return empty + mono + smooth + corner;
  }

  /**
   * Measures how well tiles are ordered monotonically along rows and columns. For each line, takes
   * the better of increasing vs decreasing order.
   */
  private static double monotonicity(Board board) {
    double total = 0;
    for (int row = 0; row < Board.SIZE; row++) {
      total += lineMonotonicity(board, row, true);
    }
    for (int col = 0; col < Board.SIZE; col++) {
      total += lineMonotonicity(board, col, false);
    }
    return total;
  }

  /** Computes monotonicity score for a single row (isRow=true) or column (isRow=false). */
  private static double lineMonotonicity(Board board, int index, boolean isRow) {
    double incr = 0;
    double decr = 0;
    for (int i = 0; i < Board.SIZE - 1; i++) {
      double curr = isRow ? log2(board.get(index, i)) : log2(board.get(i, index));
      double next = isRow ? log2(board.get(index, i + 1)) : log2(board.get(i + 1, index));
      if (curr > next) {
        decr += next - curr;
      } else if (next > curr) {
        incr += curr - next;
      }
    }
    return Math.max(incr, decr);
  }

  /** Penalizes differences between adjacent tiles. Lower (more negative) = less smooth. */
  private static double smoothness(Board board) {
    double total = 0;
    for (int row = 0; row < Board.SIZE; row++) {
      for (int col = 0; col < Board.SIZE; col++) {
        double val = log2(board.get(row, col));
        if (val == 0) continue;

        if (col + 1 < Board.SIZE) {
          double right = log2(board.get(row, col + 1));
          if (right != 0) {
            total -= Math.abs(val - right);
          }
        }
        if (row + 1 < Board.SIZE) {
          double down = log2(board.get(row + 1, col));
          if (down != 0) {
            total -= Math.abs(val - down);
          }
        }
      }
    }
    return total;
  }

  /** Bonus if the max tile is in a corner. */
  private static double cornerBonus(Board board) {
    int maxVal = 0;
    int maxRow = 0;
    int maxCol = 0;

    for (int row = 0; row < Board.SIZE; row++) {
      for (int col = 0; col < Board.SIZE; col++) {
        Integer val = board.get(row, col);
        if (val != null && val > maxVal) {
          maxVal = val;
          maxRow = row;
          maxCol = col;
        }
      }
    }

    if (maxVal == 0) return 0;

    boolean isCorner =
        (maxRow == 0 || maxRow == Board.SIZE - 1) && (maxCol == 0 || maxCol == Board.SIZE - 1);
    return isCorner ? log2(maxVal) : 0;
  }

  private static double log2(Integer value) {
    if (value == null || value <= 0) return 0;
    return Math.log(value) / Math.log(2);
  }
}
