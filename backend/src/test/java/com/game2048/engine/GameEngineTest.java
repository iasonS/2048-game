package com.game2048.engine;

import com.game2048.model.Board;
import com.game2048.model.Direction;
import com.game2048.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine(new TileSpawner());
    }

    @Test
    void testSlideLeftMergesAdjacentEqual() {
        // From spec: [null, 8, 2, 2] -> [8, 4, null, null]
        Board board = boardFromRows(
            new Integer[]{null, 8, 2, 2},
            new Integer[]{4, 2, null, 2},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, 2}
        );

        GameEngine.ApplyResult result = engine.applyMoveRaw(board, Direction.LEFT);
        Integer[][] grid = result.board().toArray();

        assertArrayEquals(new Integer[]{8, 4, null, null}, grid[0]);
        assertArrayEquals(new Integer[]{4, 4, null, null}, grid[1]);
        assertArrayEquals(new Integer[]{null, null, null, null}, grid[2]);
        assertArrayEquals(new Integer[]{2, null, null, null}, grid[3]);
        assertTrue(result.changed());
        assertEquals(4 + 4, result.scoreGained()); // 2+2=4 in row 0, 2+2=4 in row 1
    }

    @Test
    void testSlideRight() {
        // From spec
        Board board = boardFromRows(
            new Integer[]{null, 8, 2, 2},
            new Integer[]{4, 2, null, 2},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, 2}
        );

        GameEngine.ApplyResult result = engine.applyMoveRaw(board, Direction.RIGHT);
        Integer[][] grid = result.board().toArray();

        assertArrayEquals(new Integer[]{null, null, 8, 4}, grid[0]);
        assertArrayEquals(new Integer[]{null, null, 4, 4}, grid[1]);
        assertArrayEquals(new Integer[]{null, null, null, null}, grid[2]);
        assertArrayEquals(new Integer[]{null, null, null, 2}, grid[3]);
        assertTrue(result.changed());
    }

    @Test
    void testSlideUp() {
        // From spec
        Board board = boardFromRows(
            new Integer[]{null, 8, 2, 2},
            new Integer[]{4, 2, null, 2},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, 2}
        );

        GameEngine.ApplyResult result = engine.applyMoveRaw(board, Direction.UP);
        Integer[][] grid = result.board().toArray();

        assertArrayEquals(new Integer[]{4, 8, 2, 4}, grid[0]);
        assertArrayEquals(new Integer[]{null, 2, null, 2}, grid[1]);
        assertArrayEquals(new Integer[]{null, null, null, null}, grid[2]);
        assertArrayEquals(new Integer[]{null, null, null, null}, grid[3]);
        assertTrue(result.changed());
    }

    @Test
    void testSlideDown() {
        Board board = boardFromRows(
            new Integer[]{2, null, null, null},
            new Integer[]{2, null, null, null},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, null}
        );

        GameEngine.ApplyResult result = engine.applyMoveRaw(board, Direction.DOWN);
        Integer[][] grid = result.board().toArray();

        assertArrayEquals(new Integer[]{null, null, null, null}, grid[0]);
        assertArrayEquals(new Integer[]{null, null, null, null}, grid[1]);
        assertArrayEquals(new Integer[]{null, null, null, null}, grid[2]);
        assertArrayEquals(new Integer[]{4, null, null, null}, grid[3]);
        assertTrue(result.changed());
    }

    @Test
    void testSingleMergePerTile() {
        // [2, 2, 2, 2] should become [4, 4, null, null], not [8, null, null, null]
        Board board = boardFromRows(
            new Integer[]{2, 2, 2, 2},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, null}
        );

        GameEngine.ApplyResult result = engine.applyMoveRaw(board, Direction.LEFT);
        Integer[][] grid = result.board().toArray();

        assertArrayEquals(new Integer[]{4, 4, null, null}, grid[0]);
        assertEquals(8, result.scoreGained()); // 4 + 4
    }

    @Test
    void testNoOpMoveReturnsFalseChanged() {
        Board board = boardFromRows(
            new Integer[]{2, 4, null, null},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, null}
        );

        GameEngine.ApplyResult result = engine.applyMoveRaw(board, Direction.LEFT);
        assertFalse(result.changed());
    }

    @Test
    void testWinDetection() {
        Board board = boardFromRows(
            new Integer[]{4, null, null, 2},
            new Integer[]{2048, null, null, null},
            new Integer[]{4, 2, null, null},
            new Integer[]{4, null, null, null}
        );

        assertEquals(GameState.WON, engine.determineState(board));
    }

    @Test
    void testLoseDetection() {
        Board board = boardFromRows(
            new Integer[]{2, 4, 2, 4},
            new Integer[]{4, 2, 4, 2},
            new Integer[]{2, 4, 2, 4},
            new Integer[]{4, 2, 4, 2}
        );

        assertEquals(GameState.LOST, engine.determineState(board));
    }

    @Test
    void testPlayingState() {
        Board board = boardFromRows(
            new Integer[]{2, null, null, null},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, null},
            new Integer[]{null, null, null, null}
        );

        assertEquals(GameState.PLAYING, engine.determineState(board));
    }

    private Board boardFromRows(Integer[] r0, Integer[] r1, Integer[] r2, Integer[] r3) {
        return new Board(new Integer[][]{r0, r1, r2, r3});
    }
}
