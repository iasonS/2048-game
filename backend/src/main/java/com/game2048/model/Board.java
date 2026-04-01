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
    forEach((row, col) -> this.grid[row][col] = grid[row][col]);
  }

  public Integer get(int row, int col) {
    return grid[row][col];
  }

  public void set(int row, int col, Integer value) {
    grid[row][col] = value;
  }

  public Integer[][] toArray() {
    Integer[][] copy = new Integer[SIZE][SIZE];
    for (int row = 0; row < SIZE; row++) {
      System.arraycopy(grid[row], 0, copy[row], 0, SIZE);
    }
    return copy;
  }

  public List<int[]> emptyCells() {
    List<int[]> cells = new ArrayList<>();
    forEach(
        (row, col) -> {
          if (grid[row][col] == null) {
            cells.add(new int[] {row, col});
          }
        });
    return cells;
  }

  public boolean containsValue(int value) {
    for (int row = 0; row < SIZE; row++) {
      for (int col = 0; col < SIZE; col++) {
        if (grid[row][col] != null && grid[row][col] == value) return true;
      }
    }
    return false;
  }

  public Board copy() {
    return new Board(grid);
  }

  /** Iterates over all cells in the grid. */
  public void forEach(BiConsumer<Integer, Integer> action) {
    for (int row = 0; row < SIZE; row++) {
      for (int col = 0; col < SIZE; col++) {
        action.accept(row, col);
      }
    }
  }

  /** Returns the first non-null result from the function, or null. */
  public <T> T findFirst(BiFunction<Integer, Integer, T> fn) {
    for (int row = 0; row < SIZE; row++) {
      for (int col = 0; col < SIZE; col++) {
        T result = fn.apply(row, col);
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
    return findFirst(
            (row, col) -> !Objects.equals(grid[row][col], other.grid[row][col]) ? true : null)
        == null;
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(grid);
  }
}
