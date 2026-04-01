package com.game2048.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.game2048.dto.*;
import com.game2048.model.Direction;
import com.game2048.model.GameState;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class GameControllerTest {

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void testNewGameReturnsBoardWithRandomNumberOfTwos() {
    NewGameResponse res =
        client.toBlocking().retrieve(HttpRequest.POST("/api/game/new", ""), NewGameResponse.class);

    assertNotNull(res.board());
    assertEquals(4, res.board().length);
    assertEquals(0, res.score());

    // Count non-null cells — should be exactly 2, all values must be 2
    int count = 0;
    for (Integer[] row : res.board()) {
      assertEquals(4, row.length);
      for (Integer val : row) {
        if (val != null) {
          assertEquals(2, val); // initial tiles are always 2
          count++;
        }
      }
    }
    assertEquals(2, count, "Expected exactly 2 initial tiles, got " + count);
  }

  @Test
  void testMoveLeftMatchesSpec() {
    // From the spec example
    Integer[][] board = {
      {null, 8, 2, 2},
      {4, 2, null, 2},
      {null, null, null, null},
      {null, null, null, 2}
    };

    MoveResponse res =
        client
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/api/game/move", new MoveRequest(board, Direction.LEFT, 0)),
                MoveResponse.class);

    // Row 0: [null, 8, 2, 2] -> [8, 4, ?, ?] (the ? cells may have a spawn)
    assertEquals(8, res.board()[0][0]);
    assertEquals(4, res.board()[0][1]);
    // Row 1: [4, 2, null, 2] -> [4, 4, ?, ?]
    assertEquals(4, res.board()[1][0]);
    assertEquals(4, res.board()[1][1]);
    // Row 3: [null, null, null, 2] -> [2, ?, ?, ?]
    assertEquals(2, res.board()[3][0]);

    assertEquals(8, res.score()); // 4 + 4 from merges
    assertEquals(GameState.PLAYING, res.gameState());
    assertNotNull(res.spawnedCell());
  }

  @Test
  void testNoOpMoveReturnsNullSpawnedCell() {
    Integer[][] board = {
      {2, 4, null, null},
      {null, null, null, null},
      {null, null, null, null},
      {null, null, null, null}
    };

    MoveResponse res =
        client
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/api/game/move", new MoveRequest(board, Direction.LEFT, 0)),
                MoveResponse.class);

    assertNull(res.spawnedCell()); // no change, no spawn
    assertEquals(0, res.score());
  }

  @Test
  void testAiSuggestReturnsDirection() {
    Integer[][] board = {
      {2, null, null, null},
      {null, 4, null, null},
      {null, null, null, null},
      {null, null, null, 2}
    };

    AiResponse res =
        client
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/api/game/ai-suggest", new AiRequest(board)), AiResponse.class);

    assertNotNull(res.recommendedDirection());
  }

  @Test
  void testWinDetection() {
    Integer[][] board = {
      {1024, 1024, null, null},
      {null, null, null, null},
      {null, null, null, null},
      {null, null, null, null}
    };

    MoveResponse res =
        client
            .toBlocking()
            .retrieve(
                HttpRequest.POST("/api/game/move", new MoveRequest(board, Direction.LEFT, 0)),
                MoveResponse.class);

    assertEquals(GameState.WON, res.gameState());
    assertEquals(2048, res.board()[0][0]);
  }

  @Test
  void testMoveRejects3x3Board() {
    Integer[][] board = {
      {2, null, null},
      {null, null, null},
      {null, null, 2}
    };

    HttpClientResponseException ex =
        assertThrows(
            HttpClientResponseException.class,
            () ->
                client
                    .toBlocking()
                    .retrieve(
                        HttpRequest.POST(
                            "/api/game/move", new MoveRequest(board, Direction.LEFT, 0)),
                        MoveResponse.class));
    assertEquals(400, ex.getStatus().getCode());
  }

  @Test
  void testMoveRejectsInvalidCellValue() {
    Integer[][] board = {
      {3, null, null, null},
      {null, null, null, null},
      {null, null, null, null},
      {null, null, null, null}
    };

    HttpClientResponseException ex =
        assertThrows(
            HttpClientResponseException.class,
            () ->
                client
                    .toBlocking()
                    .retrieve(
                        HttpRequest.POST(
                            "/api/game/move", new MoveRequest(board, Direction.LEFT, 0)),
                        MoveResponse.class));
    assertEquals(400, ex.getStatus().getCode());
  }
}
