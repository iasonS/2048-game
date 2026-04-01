package com.game2048.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonSchemaLocalValidation;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.StructuredMessage;
import com.game2048.model.Board;
import com.game2048.model.Direction;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Primary
@Requires(property = "ai.provider", value = "claude")
public class ClaudeAiSolver implements AiProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ClaudeAiSolver.class);

  private final ExpectimaxSolver expectimaxFallback;

  @Property(name = "ANTHROPIC_API_KEY")
  private String apiKey;

  private AnthropicClient client;

  public ClaudeAiSolver(ExpectimaxSolver expectimaxFallback) {
    this.expectimaxFallback = expectimaxFallback;
  }

  /** Structured output response — Claude must return exactly this shape. */
  public record MoveResponse(String direction) {}

  @PostConstruct
  void init() {
    client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    LOG.info("Claude AI provider initialized (model: claude-haiku-4-5, fallback: expectimax)");
  }

  @PreDestroy
  void close() {
    if (client != null) {
      client.close();
    }
  }

  @Override
  public Direction findBestMove(Board board) {
    try {
      Direction result = askClaude(board);
      if (result != null) {
        return result;
      }
    } catch (Exception e) {
      LOG.warn("Claude API call failed: {}", e.getMessage());
    }
    LOG.info("Falling back to expectimax");
    return expectimaxFallback.findBestMove(board);
  }

  private Direction askClaude(Board board) {
    String boardStr = formatBoard(board);
    String prompt =
        "You are playing 2048. The board is a 4x4 grid. Tiles slide in the chosen direction, "
            + "and adjacent equal tiles merge. The goal is to reach 2048.\n\n"
            + "Current board (null = empty):\n"
            + boardStr
            + "\n\nWhat is the single best move? "
            + "Respond with a JSON object containing a \"direction\" field "
            + "with one of: UP, DOWN, LEFT, RIGHT.";

    StructuredMessage<MoveResponse> message =
        client
            .messages()
            .create(
                MessageCreateParams.builder()
                    .model(Model.CLAUDE_HAIKU_4_5)
                    .maxTokens(64)
                    .addUserMessage(prompt)
                    .outputConfig(MoveResponse.class, JsonSchemaLocalValidation.YES)
                    .build());

    MoveResponse parsed =
        message.content().stream()
            .filter(block -> block.text().isPresent())
            .map(block -> block.text().get().text())
            .findFirst()
            .orElse(null);

    if (parsed != null && parsed.direction() != null) {
      Direction dir = parseDirection(parsed.direction());
      if (dir != null) {
        return dir;
      }
    }

    LOG.warn("Claude returned no usable output, falling back to expectimax");
    return null;
  }

  private String formatBoard(Board board) {
    StringBuilder sb = new StringBuilder();
    for (int row = 0; row < Board.SIZE; row++) {
      for (int col = 0; col < Board.SIZE; col++) {
        Integer val = board.get(row, col);
        sb.append(val == null ? "null" : val);
        if (col < Board.SIZE - 1) sb.append('\t');
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  private Direction parseDirection(String text) {
    String upper = text.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
    for (Direction dir : Direction.values()) {
      if (upper.contains(dir.name())) {
        return dir;
      }
    }
    LOG.warn("Could not parse direction from Claude response: '{}'", text);
    return null;
  }
}
