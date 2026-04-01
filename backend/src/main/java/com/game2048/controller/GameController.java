package com.game2048.controller;

import com.game2048.ai.AiProvider;
import com.game2048.dto.*;
import com.game2048.engine.GameEngine;
import com.game2048.engine.TileSpawner;
import com.game2048.model.Board;
import com.game2048.model.Direction;
import com.game2048.model.GameState;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Controller("/api/game")
@RequiredArgsConstructor
public class GameController {

  private final GameEngine gameEngine;
  private final TileSpawner tileSpawner;
  private final AiProvider aiProvider;

  @Post("/new")
  public NewGameResponse newGame() {
    Board board = new Board();
    tileSpawner.spawnInitialTiles(board);
    return new NewGameResponse(board.toArray(), 0);
  }

  @Post("/move")
  public HttpResponse<MoveResponse> move(@Body MoveRequest request) {
    String boardError = validateBoard(request.board());
    if (boardError != null) return HttpResponse.badRequest();
    if (request.direction() == null) return HttpResponse.badRequest();

    Board board = new Board(request.board());
    GameEngine.MoveResult result =
        gameEngine.applyMove(board, request.direction(), request.score());
    GameState state = gameEngine.determineState(result.board());
    List<TileMoveDto> moveDtos =
        result.moves().stream()
            .map(m -> new TileMoveDto(m.fromRow(), m.fromCol(), m.toRow(), m.toCol(), m.merged()))
            .toList();
    return HttpResponse.ok(
        new MoveResponse(
            result.board().toArray(), result.score(), state, result.spawnedCell(), moveDtos));
  }

  @Post("/ai-suggest")
  public HttpResponse<AiResponse> aiSuggest(@Body AiRequest request) {
    String boardError = validateBoard(request.board());
    if (boardError != null) return HttpResponse.badRequest();

    Board board = new Board(request.board());
    Direction best = aiProvider.findBestMove(board);
    if (best == null) {
      return HttpResponse.ok(new AiResponse(null));
    }
    return HttpResponse.ok(new AiResponse(best));
  }

  private String validateBoard(Integer[][] board) {
    if (board == null || board.length != Board.SIZE) return "Board must be 4x4";
    for (Integer[] row : board) {
      if (row == null || row.length != Board.SIZE) return "Board must be 4x4";
      for (Integer val : row) {
        if (val != null && (val <= 0 || (val & (val - 1)) != 0))
          return "Cell values must be null or a positive power of 2";
      }
    }
    return null;
  }
}
