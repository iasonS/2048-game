package com.game2048.controller;

import com.game2048.ai.AiSolver;
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

@Controller("/api/game")
public class GameController {

    private final GameEngine gameEngine;
    private final TileSpawner tileSpawner;
    private final AiSolver aiSolver;

    public GameController(GameEngine gameEngine, TileSpawner tileSpawner, AiSolver aiSolver) {
        this.gameEngine = gameEngine;
        this.tileSpawner = tileSpawner;
        this.aiSolver = aiSolver;
    }

    @Post("/new")
    public NewGameResponse newGame() {
        Board board = new Board();
        tileSpawner.spawnInitial(board);
        tileSpawner.spawnInitial(board);
        return new NewGameResponse(board.toArray(), 0);
    }

    @Post("/move")
    public MoveResponse move(@Body MoveRequest request) {
        Board board = new Board(request.board());
        GameEngine.MoveResult result = gameEngine.applyMove(board, request.direction(), request.score());
        GameState state = gameEngine.determineState(result.board());
        List<TileMoveDto> moveDtos = result.moves().stream()
            .map(m -> new TileMoveDto(m.fromRow(), m.fromCol(), m.toRow(), m.toCol(), m.merged()))
            .toList();
        return new MoveResponse(result.board().toArray(), result.score(), state, result.spawnedCell(), moveDtos);
    }

    @Post("/ai-suggest")
    public HttpResponse<AiResponse> aiSuggest(@Body AiRequest request) {
        Board board = new Board(request.board());
        Direction best = aiSolver.findBestMove(board);
        if (best == null) {
            return HttpResponse.ok(new AiResponse(null));
        }
        return HttpResponse.ok(new AiResponse(best));
    }
}
