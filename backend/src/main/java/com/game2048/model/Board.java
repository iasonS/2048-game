package com.game2048.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Board {

    public static final int SIZE = 4;
    private final Integer[][] grid;

    public Board() {
        this.grid = new Integer[SIZE][SIZE];
    }

    public Board(Integer[][] grid) {
        this.grid = new Integer[SIZE][SIZE];
        forEach((r, c) -> this.grid[r][c] = grid[r][c]);
    }

    public Integer get(int row, int col) {
        return grid[row][col];
    }

    public void set(int row, int col, Integer value) {
        grid[row][col] = value;
    }

    public Integer[][] toArray() {
        Integer[][] copy = new Integer[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
        }
        return copy;
    }

    public List<int[]> emptyCells() {
        List<int[]> cells = new ArrayList<>();
        forEach((r, c) -> {
            if (grid[r][c] == null) {
                cells.add(new int[]{r, c});
            }
        });
        return cells;
    }

    public boolean containsValue(int value) {
        return findFirst((r, c) -> grid[r][c] != null && grid[r][c] == value ? true : null) != null;
    }

    public Board copy() {
        return new Board(grid);
    }

    /** Iterates over all cells in the grid. */
    public void forEach(BiConsumer<Integer, Integer> action) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                action.accept(r, c);
            }
        }
    }

    /** Returns the first non-null result from the function, or null. */
    public <T> T findFirst(BiFunction<Integer, Integer, T> fn) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                T result = fn.apply(r, c);
                if (result != null) return result;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Board other = (Board) obj;
        return findFirst((r, c) -> !Objects.equals(grid[r][c], other.grid[r][c]) ? true : null) == null;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }
}
