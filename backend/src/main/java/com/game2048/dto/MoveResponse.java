package com.game2048.dto;

import com.game2048.model.GameState;

import java.util.List;

public record MoveResponse(
    Integer[][] board,
    int score,
    GameState gameState,
    int[] spawnedCell,
    List<TileMoveDto> moves
) {}
