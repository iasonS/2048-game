package com.game2048.engine;

import com.game2048.model.Board;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Random;

@Singleton
public class TileSpawner {

  private final Random random = new Random();

  /** Places a random number of tiles (2–8) of value 2 at random empty cells. */
  public void spawnInitialTiles(Board board) {
    int count = 2 + random.nextInt(7); // 2 inclusive, 8 inclusive
    for (int i = 0; i < count; i++) {
      List<int[]> empty = board.emptyCells();
      if (empty.isEmpty()) return;
      int[] cell = empty.get(random.nextInt(empty.size()));
      board.set(cell[0], cell[1], 2);
    }
  }

  /**
   * Places a 2 (90%) or 4 (10%) at a random empty cell. Returns the {row, col} of the spawned cell,
   * or null if the board is full.
   */
  public int[] spawn(Board board) {
    List<int[]> empty = board.emptyCells();
    if (empty.isEmpty()) {
      return null;
    }
    int[] cell = empty.get(random.nextInt(empty.size()));
    int value = random.nextDouble() < 0.9 ? 2 : 4;
    board.set(cell[0], cell[1], value);
    return cell;
  }
}
