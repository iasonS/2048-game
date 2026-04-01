package com.game2048.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.game2048.model.Board;
import com.game2048.model.Direction;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ClaudeAiSolver. Uses a dummy API key so Claude API calls fail, verifying
 * that the solver correctly falls back to expectimax.
 */
@MicronautTest(propertySources = "classpath:application-claude-test.yml")
class ClaudeAiSolverTest {

  @Inject AiProvider aiProvider;

  @Test
  void testClaudeProviderIsInjectedWhenConfigured() {
    assertInstanceOf(ClaudeAiSolver.class, aiProvider);
  }

  @Test
  void testFallsBackToExpectimaxOnApiFailure() {
    Board board = new Board();
    board.set(0, 0, 2);
    board.set(1, 0, 4);

    // Claude will fail (dummy key), should fall back to expectimax and return a valid direction
    Direction result = aiProvider.findBestMove(board);
    assertNotNull(result, "Should fall back to expectimax and return a valid direction");
  }

  @Test
  void testFallbackReturnsNullForNoMoves() {
    Board board =
        new Board(
            new Integer[][] {
              {2, 4, 2, 4},
              {4, 2, 4, 2},
              {2, 4, 2, 4},
              {4, 2, 4, 2}
            });

    Direction result = aiProvider.findBestMove(board);
    assertNull(result, "No valid moves should return null");
  }
}
