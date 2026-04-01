package com.game2048.ai;

import com.game2048.model.Board;

public final class Heuristics {

    private static final double EMPTY_CELL_WEIGHT = 2.7;
    private static final double MONOTONICITY_WEIGHT = 1.0;
    private static final double SMOOTHNESS_WEIGHT = 0.1;
    private static final double CORNER_BONUS_WEIGHT = 1.0;

    private Heuristics() {}

    public static double evaluate(Board board) {
        double empty = Math.log(Math.max(1, board.emptyCells().size())) * EMPTY_CELL_WEIGHT;
        double mono = monotonicity(board) * MONOTONICITY_WEIGHT;
        double smooth = smoothness(board) * SMOOTHNESS_WEIGHT;
        double corner = cornerBonus(board) * CORNER_BONUS_WEIGHT;
        return empty + mono + smooth + corner;
    }

    /**
     * Measures how well tiles are ordered monotonically along rows and columns.
     * For each row/column, takes the better of increasing vs decreasing order.
     */
    private static double monotonicity(Board board) {
        double total = 0;

        for (int r = 0; r < Board.SIZE; r++) {
            double incr = 0, decr = 0;
            for (int c = 0; c < Board.SIZE - 1; c++) {
                double curr = log2(board.get(r, c));
                double next = log2(board.get(r, c + 1));
                if (curr > next) decr += next - curr;
                else if (next > curr) incr += curr - next;
            }
            total += Math.max(incr, decr);
        }

        for (int c = 0; c < Board.SIZE; c++) {
            double incr = 0, decr = 0;
            for (int r = 0; r < Board.SIZE - 1; r++) {
                double curr = log2(board.get(r, c));
                double next = log2(board.get(r + 1, c));
                if (curr > next) decr += next - curr;
                else if (next > curr) incr += curr - next;
            }
            total += Math.max(incr, decr);
        }

        return total;
    }

    /**
     * Penalizes differences between adjacent tiles. Lower (more negative) = less smooth.
     */
    private static double smoothness(Board board) {
        double smoothness = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                double val = log2(board.get(r, c));
                if (val == 0) continue;
                if (c + 1 < Board.SIZE) {
                    double right = log2(board.get(r, c + 1));
                    if (right != 0) smoothness -= Math.abs(val - right);
                }
                if (r + 1 < Board.SIZE) {
                    double down = log2(board.get(r + 1, c));
                    if (down != 0) smoothness -= Math.abs(val - down);
                }
            }
        }
        return smoothness;
    }

    /**
     * Bonus if the max tile is in a corner.
     */
    private static double cornerBonus(Board board) {
        int maxVal = 0;
        int maxR = 0, maxC = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Integer v = board.get(r, c);
                if (v != null && v > maxVal) {
                    maxVal = v;
                    maxR = r;
                    maxC = c;
                }
            }
        }
        if (maxVal == 0) return 0;
        boolean isCorner = (maxR == 0 || maxR == Board.SIZE - 1)
                && (maxC == 0 || maxC == Board.SIZE - 1);
        return isCorner ? log2(maxVal) : 0;
    }

    private static double log2(Integer value) {
        if (value == null || value <= 0) return 0;
        return Math.log(value) / Math.log(2);
    }
}
