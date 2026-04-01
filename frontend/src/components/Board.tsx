import { useEffect, useRef, useState } from "react";
import type { Board as BoardType, TileMove } from "../api/gameApi";

interface BoardProps {
  board: BoardType;
  spawnedCell: [number, number] | null;
  moves: TileMove[];
}

interface TileData {
  id: number;
  value: number;
  row: number;
  col: number;
  isNew: boolean;
}

let nextTileId = 1;

function posStyle(row: number, col: number) {
  return {
    top: `calc(10px + ${row} * ((100% - 50px) / 4 + 10px))`,
    left: `calc(10px + ${col} * ((100% - 50px) / 4 + 10px))`,
  };
}

function key(r: number, c: number) {
  return `${r},${c}`;
}

export function Board({ board, spawnedCell, moves }: BoardProps) {
  const [tiles, setTiles] = useState<TileData[]>([]);
  const prevTilesRef = useRef<Map<string, TileData>>(new Map());

  useEffect(() => {
    const prevMap = prevTilesRef.current;
    const nextTiles: TileData[] = [];
    const nextMap = new Map<string, TileData>();

    if (moves.length > 0) {
      // We have server-provided movement data — use it for exact animation
      // 1. Build moved tiles: for each move, take the old tile ID and move it to new position
      const usedOldKeys = new Set<string>();
      const destinationIds = new Map<string, number>(); // destKey -> id of tile that slides there

      for (const mv of moves) {
        const fromKey = key(mv.fromRow, mv.fromCol);
        const toKey = key(mv.toRow, mv.toCol);
        const oldTile = prevMap.get(fromKey);

        if (oldTile && !usedOldKeys.has(fromKey)) {
          usedOldKeys.add(fromKey);
          if (!mv.merged) {
            // This tile slides to dest and stays (it's the primary tile)
            destinationIds.set(toKey, oldTile.id);
          }
          // For merged tiles, we create a ghost that slides and disappears
        }
      }

      // 2. Build final tile list from the new board
      for (let r = 0; r < 4; r++) {
        for (let c = 0; c < 4; c++) {
          const val = board[r][c];
          if (val == null) continue;

          const cellKey = key(r, c);
          const isSpawned =
            spawnedCell != null && spawnedCell[0] === r && spawnedCell[1] === c;

          let id: number;
          if (isSpawned) {
            id = nextTileId++;
          } else if (destinationIds.has(cellKey)) {
            id = destinationIds.get(cellKey)!;
          } else {
            // Merged result at this position — reuse any old tile that moved here
            const incoming = moves.find(
              (m) => m.toRow === r && m.toCol === c && !m.merged,
            );
            const oldKey = incoming
              ? key(incoming.fromRow, incoming.fromCol)
              : null;
            const oldTile = oldKey ? prevMap.get(oldKey) : null;
            id = oldTile?.id ?? nextTileId++;
          }

          const tile: TileData = {
            id,
            value: val,
            row: r,
            col: c,
            isNew: isSpawned,
          };
          nextTiles.push(tile);
          nextMap.set(cellKey, tile);
        }
      }
    } else {
      // No moves (initial board or no-op) — just render current board
      for (let r = 0; r < 4; r++) {
        for (let c = 0; c < 4; c++) {
          const val = board[r][c];
          if (val == null) continue;

          const cellKey = key(r, c);
          const existing = prevMap.get(cellKey);
          const id = existing?.id ?? nextTileId++;
          const tile: TileData = {
            id,
            value: val,
            row: r,
            col: c,
            isNew: false,
          };
          nextTiles.push(tile);
          nextMap.set(cellKey, tile);
        }
      }
    }

    prevTilesRef.current = nextMap;
    setTiles(nextTiles);
  }, [board, spawnedCell, moves]);

  return (
    <div className="board">
      {Array.from({ length: 16 }, (_, i) => (
        <div key={`bg-${i}`} className="cell" />
      ))}
      {tiles.map((tile) => (
        <div
          key={tile.id}
          className={`tile tile-${tile.value}${tile.isNew ? " tile-new" : ""}`}
          style={posStyle(tile.row, tile.col)}
        >
          <span
            className={`tile-value${tile.value >= 1024 ? " tile-small" : tile.value >= 128 ? " tile-medium" : ""}`}
          >
            {tile.value}
          </span>
        </div>
      ))}
    </div>
  );
}
