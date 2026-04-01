package com.game2048.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void testEmptyBoardHas16EmptyCells() {
        Board board = new Board();
        assertEquals(16, board.emptyCells().size());
    }

    @Test
    void testSetAndGet() {
        Board board = new Board();
        board.set(1, 2, 4);
        assertEquals(4, board.get(1, 2));
        assertNull(board.get(0, 0));
    }

    @Test
    void testCopyIsDeep() {
        Board board = new Board();
        board.set(0, 0, 2);
        Board copy = board.copy();
        copy.set(0, 0, 4);
        assertEquals(2, board.get(0, 0));
        assertEquals(4, copy.get(0, 0));
    }

    @Test
    void testEqualsIdentical() {
        Board a = new Board(new Integer[][]{
            {2, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, 4}
        });
        Board b = new Board(new Integer[][]{
            {2, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, 4}
        });
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testEqualsDifferent() {
        Board a = new Board();
        a.set(0, 0, 2);
        Board b = new Board();
        b.set(0, 0, 4);
        assertNotEquals(a, b);
    }

    @Test
    void testEqualsNull() {
        Board board = new Board();
        assertNotEquals(board, null);
    }

    @Test
    void testToArrayIsDeepCopy() {
        Board board = new Board();
        board.set(0, 0, 2);
        Integer[][] arr = board.toArray();
        arr[0][0] = 99;
        assertEquals(2, board.get(0, 0));
    }

    @Test
    void testEmptyCellsExcludesOccupied() {
        Board board = new Board();
        board.set(0, 0, 2);
        board.set(1, 1, 4);
        board.set(2, 2, 8);
        assertEquals(13, board.emptyCells().size());
    }
}
