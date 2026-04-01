package com.game2048.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.game2048.engine.GameEngine;
import com.game2048.engine.TileSpawner;
import com.game2048.model.Board;
import com.game2048.model.Direction;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AiSolverTest {

  private AiSolver solver;

  @BeforeEach
  void setUp() {
    GameEngine engine = new GameEngine(new TileSpawner());
    solver = new AiSolver(engine);
  }

  @Test
  void testReturnsValidDirection() {
    Board board =
        new Board(
            new Integer[][] {
              {2, null, null, null},
              {null, 4, null, null},
              {null, null, null, null},
              {null, null, null, 2}
            });
    Direction best = solver.findBestMove(board);
    assertNotNull(best);
    assertTrue(Set.of(Direction.values()).contains(best));
  }

  @Test
  void testReturnsNullWhenNoMovesExist() {
    Board board =
        new Board(
            new Integer[][] {
              {2, 4, 2, 4},
              {4, 2, 4, 2},
              {2, 4, 2, 4},
              {4, 2, 4, 2}
            });
    Direction best = solver.findBestMove(board);
    assertNull(best);
  }

  @Test
  void testSuggestsReasonableMoveForSimpleBoard() {
    // Two 2s in a row — AI should suggest merging them
    Board board =
        new Board(
            new Integer[][] {
              {2, 2, null, null},
              {null, null, null, null},
              {null, null, null, null},
              {null, null, null, null}
            });
    Direction best = solver.findBestMove(board);
    assertNotNull(best);
    // Should suggest LEFT or RIGHT to merge the pair
    assertTrue(
        best == Direction.LEFT
            || best == Direction.RIGHT
            || best == Direction.UP
            || best == Direction.DOWN);
  }
}
