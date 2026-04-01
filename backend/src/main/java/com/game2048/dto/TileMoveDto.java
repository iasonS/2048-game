package com.game2048.dto;

public record TileMoveDto(int fromRow, int fromCol, int toRow, int toCol, boolean merged) {}
