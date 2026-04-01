const API_BASE = import.meta.env.VITE_API_URL || '';

export type Board = (number | null)[][];
export type Direction = 'UP' | 'DOWN' | 'LEFT' | 'RIGHT';
export type GameStatus = 'PLAYING' | 'WON' | 'LOST';

export interface TileMove {
  fromRow: number;
  fromCol: number;
  toRow: number;
  toCol: number;
  merged: boolean;
}

export interface NewGameResponse {
  board: Board;
  score: number;
}

export interface MoveResponse {
  board: Board;
  score: number;
  gameState: GameStatus;
  spawnedCell: [number, number] | null;
  moves: TileMove[];
}

export interface AiResponse {
  recommendedDirection: Direction;
}

async function post<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`API error: ${res.status}`);
  return res.json();
}

export function newGame(): Promise<NewGameResponse> {
  return post('/api/game/new');
}

export function move(board: Board, direction: Direction, score: number): Promise<MoveResponse> {
  return post('/api/game/move', { board, direction, score });
}

export function aiSuggest(board: Board): Promise<AiResponse> {
  return post('/api/game/ai-suggest', { board });
}
