package com.game2048.ai;

import com.game2048.model.Board;
import com.game2048.model.Direction;

/** Strategy interface for AI move suggestions. */
public interface AiProvider {

  /** Returns the best move for the given board, or null if no valid move exists. */
  Direction findBestMove(Board board);
}
