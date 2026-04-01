package com.game2048.dto;

import com.game2048.model.Direction;

public record MoveRequest(Integer[][] board, Direction direction, int score) {}
