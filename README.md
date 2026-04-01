# 2048

A full-stack implementation of the 2048 puzzle game with an AI solver that uses the expectimax algorithm to recommend optimal moves.

**[Play it live](https://iasons-2048-game.onrender.com)** — hosted on Render's free tier; the first request may take ~30s if the backend is cold. For the smoothest experience, [run locally](#running-locally) (takes 30 seconds to set up).

## Architecture

```
┌─────────────┐         ┌──────────────────┐
│   Frontend   │  POST   │     Backend      │
│  React/TS    │────────▶│  Micronaut/Java  │
│  (Static)    │◀────────│   (Docker)       │
└─────────────┘  JSON    └──────────────────┘
```

The application uses a **stateless client-server architecture**. The frontend holds the game state and sends the current board to the backend for move computation and AI suggestions. The backend has no session state — every request is self-contained.

### Why this architecture?

- **Testability**: Game logic lives in the backend where it can be unit-tested and integration-tested without a browser. 25 tests cover the engine, AI, board model, and HTTP endpoints.
- **Separation of concerns**: The frontend is purely presentational — it renders the board, handles input, and animates tiles. All game rules are enforced server-side.
- **Stateless API**: Each request contains the full board state, making the backend horizontally scalable and trivially restartable.

## Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| Backend | Java 21 + Micronaut 4.x | Records for immutable DTOs, switch expressions, fast startup (~400ms) |
| Frontend | React 19 + TypeScript + Vite | Type safety, fast HMR, lightweight build |
| AI | Expectimax algorithm | Correct model for probabilistic tile spawns (vs minimax which assumes adversarial play) |
| Deploy | Docker (backend) + Static site (frontend) | Containerized backend, static frontend hosted separately |

## API

Three stateless POST endpoints:

| Endpoint | Input | Output |
|----------|-------|--------|
| `/api/game/new` | — | Board with two random `2` tiles |
| `/api/game/move` | `{board, direction, score}` | New board, score, game state, spawned cell, tile movements |
| `/api/game/ai-suggest` | `{board}` | Recommended direction |

The move endpoint returns **tile movement data** (`fromRow`, `fromCol`, `toRow`, `toCol`, `merged`) so the frontend can animate tiles sliding to their exact destinations without guessing.

## Design Decisions

### All directions reduce to LEFT

Rather than implementing four separate merge algorithms, the engine transforms the board so every move becomes a left-slide:

- **LEFT**: no transform
- **RIGHT**: reverse each row → slide left → reverse back
- **UP**: transpose → slide left → transpose back
- **DOWN**: transpose + reverse → slide left → reverse + transpose back

This means `slideAndMergeRow()` is written once and tested once. The transformation approach is a well-known technique for 2048 implementations.

### AI Providers

The game supports two AI providers behind a common `AiProvider` interface, selected via the `AI_PROVIDER` environment variable:

| Provider | When active | Strengths |
|----------|------------|-----------|
| **Expectimax** (default) | `AI_PROVIDER` unset or `expectimax` | Offline, fast (<200ms), consistently reaches 2048 |
| **Claude** | `AI_PROVIDER=claude` + `ANTHROPIC_API_KEY` set | Demonstrates LLM integration; uses Claude Haiku for speed/cost |

Expectimax is the better player — it's purpose-built for 2048's probabilistic tile spawns. Claude is included as an alternative to satisfy the spec's "AI model" / "remote AI server" interpretation and to demonstrate API integration. The provider is injected via Micronaut's `@Requires` conditional wiring, so no code path changes when switching.

### Expectimax over Minimax

The tile spawn (90% chance of `2`, 10% chance of `4`) is random, not adversarial. Minimax assumes a worst-case opponent and would play too conservatively. Expectimax correctly computes the **expected value** across all possible spawns, producing better move recommendations.

**Search depth**: 3 plies when ≤6 empty cells, 2 plies otherwise. This keeps response time under 200ms while still finding good moves. The AI consistently reaches 2048.

**Heuristics** (board evaluation function):
- **Empty cells** (weight 2.7): More empty cells = more flexibility
- **Monotonicity** (weight 1.0): Tiles should increase/decrease along rows and columns
- **Smoothness** (weight 0.1): Adjacent tiles should have similar values
- **Corner bonus** (weight 1.0): Highest tile in a corner is a strong position

### Server-provided tile movements

Early iterations used frontend heuristics to guess which tile moved where by comparing before/after board snapshots. This produced incorrect animations when multiple tiles had the same value. The fix was to have the backend track exact tile movements during `slideAndMergeRow()` and return them in the response. The frontend simply animates each tile from its `from` position to its `to` position using CSS transitions.

## Running Locally

### Prerequisites

- Java 21 (e.g. Eclipse Temurin)
- Node.js 18+

### Backend

```bash
cd backend
./gradlew run        # starts on :8080
./gradlew test       # runs 25 tests
```

### Frontend

```bash
cd frontend
npm install
npm run dev          # starts on :5173, proxies /api to :8080
```

Open http://localhost:5173 and play with arrow keys, WASD, or swipe on mobile.

### Controls

- **Arrow keys / WASD**: Move tiles
- **Touch/swipe**: Move tiles (mobile)
- **New Game**: Reset the board
- **AI Hint**: Ask the AI for the best move
- **Auto Play**: Let the AI play continuously

## Testing

```
Backend: 25 tests across 4 test classes (./gradlew test)

  BoardTest (8)         — equals, hashCode, copy, emptyCells, toArray
  GameEngineTest (9)    — all 4 directions, merge rules (spec examples), win/lose detection
  ExpectimaxSolverTest (3) — returns valid direction, null on no moves, reasonable suggestions
  GameControllerTest (5) — integration tests hitting real HTTP endpoints

Frontend: 10 tests across 2 test files (npm test)

  gameApi.test.ts (4)      — API calls, request payloads, error handling
  components.test.tsx (6)  — ScoreBoard, GameOverlay (win/lose/playing), GameControls (hint, auto-play)
```

The `GameEngineTest` tests use the exact board examples from the requirements spec to verify correctness.

## Deployment

The backend is containerized via Docker and the frontend is a static build (`npm run build` → `dist/`). Both can be deployed to any hosting provider.

### Backend

```bash
cd backend
docker build -t game-2048-backend .
docker run -p 8080:8080 game-2048-backend
```

### Frontend

Set `VITE_API_URL` to the backend URL before building:

```bash
cd frontend
VITE_API_URL=https://your-backend-url npm run build
# Serve the dist/ directory with any static file server
```

## Assumptions

- The initial board spawns **exactly 2 tiles of value `2`** at random positions, matching the original 2048 behavior. The spec's "random number of `2`s" is interpreted conservatively to ensure a playable starting board.
- After reaching 2048, the player can choose to **keep playing** or start a new game (the spec marks 2048 as a win condition but doesn't explicitly say the game must stop).
- The default AI (expectimax) uses no external services or credentials — it runs entirely in the backend JVM. An optional Claude provider is available when `AI_PROVIDER=claude` and `ANTHROPIC_API_KEY` are set, but no credentials are included in the repository.
