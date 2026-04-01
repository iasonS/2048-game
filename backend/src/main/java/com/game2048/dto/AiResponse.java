package com.game2048.dto;

import com.game2048.model.Direction;

public record AiResponse(Direction recommendedDirection) {}
