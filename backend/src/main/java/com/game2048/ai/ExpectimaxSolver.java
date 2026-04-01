package com.game2048.ai;

import com.game2048.engine.GameEngine;
import com.game2048.model.Board;
import com.game2048.model.Direction;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class ExpectimaxSolver implements AiProvider {

  private static final int DEFAULT_DEPTH = 3;
  private final GameEngine gameEngine;

  /**
   * Returns the best move for the given board using expectimax search. Returns null if no valid
   * move exists.
   */
  public Direction findBestMove(Board board) {
    double bestScore = Double.NEGATIVE_INFINITY;
    Direction bestDir = null;

    for (Direction dir : Direction.values()) {
      GameEngine.ApplyResult result = gameEngine.applyMoveRaw(board, dir);
      if (!result.changed()) continue;

      int depth = board.emptyCells().size() > 6 ? 2 : DEFAULT_DEPTH;
      double score = expectimaxChance(result.board(), depth);
      if (score > bestScore) {
        bestScore = score;
        bestDir = dir;
      }
    }

    return bestDir;
  }

  /** Chance node: average over all possible tile spawns. */
  private double expectimaxChance(Board board, int depth) {
    List<int[]> empty = board.emptyCells();
    if (empty.isEmpty()) {
      return Heuristics.evaluate(board);
    }

    double total = 0;
    for (int[] cell : empty) {
      // Try spawning a 2 (90%)
      board.set(cell[0], cell[1], 2);
      total += 0.9 * expectimaxPlayer(board, depth - 1);

      // Try spawning a 4 (10%)
      board.set(cell[0], cell[1], 4);
      total += 0.1 * expectimaxPlayer(board, depth - 1);

      // Restore
      board.set(cell[0], cell[1], null);
    }

    return total / empty.size();
  }

  /** Player node: maximize over all valid moves. */
  private double expectimaxPlayer(Board board, int depth) {
    if (depth <= 0) {
      return Heuristics.evaluate(board);
    }

    double maxScore = Double.NEGATIVE_INFINITY;
    boolean anyValid = false;

    for (Direction dir : Direction.values()) {
      GameEngine.ApplyResult result = gameEngine.applyMoveRaw(board, dir);
      if (!result.changed()) continue;
      anyValid = true;
      double score = expectimaxChance(result.board(), depth);
      if (score > maxScore) {
        maxScore = score;
      }
    }

    return anyValid ? maxScore : Heuristics.evaluate(board);
  }
}
