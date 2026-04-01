import { useState, useEffect, useCallback, useRef } from "react";
import * as api from "../api/gameApi";

const EMPTY_BOARD: api.Board = Array.from({ length: 4 }, () =>
  Array(4).fill(null),
);
const SWIPE_THRESHOLD = 30;
const AUTO_PLAY_DELAY = 300;

function loadBestScore(): number {
  const stored = localStorage.getItem("2048-best");
  return stored ? parseInt(stored, 10) : 0;
}

export function useGame() {
  const [board, setBoard] = useState<api.Board>(EMPTY_BOARD);
  const [score, setScore] = useState(0);
  const [bestScore, setBestScore] = useState(loadBestScore);
  const [gameStatus, setGameStatus] = useState<api.GameStatus>("PLAYING");
  const [isLoading, setIsLoading] = useState(false);
  const [spawnedCell, setSpawnedCell] = useState<[number, number] | null>(null);
  const [moves, setMoves] = useState<api.TileMove[]>([]);
  const [aiHint, setAiHint] = useState<api.Direction | null>(null);
  const [keepPlaying, setKeepPlaying] = useState(false);
  const [autoPlay, setAutoPlay] = useState(false);

  const boardRef = useRef(board);
  const scoreRef = useRef(score);
  const bestScoreRef = useRef(bestScore);
  const gameStatusRef = useRef(gameStatus);
  const keepPlayingRef = useRef(keepPlaying);
  const autoPlayRef = useRef(autoPlay);
  const busyRef = useRef(false);
  const moveQueueRef = useRef<api.Direction[]>([]);

  boardRef.current = board;
  scoreRef.current = score;
  bestScoreRef.current = bestScore;
  gameStatusRef.current = gameStatus;
  keepPlayingRef.current = keepPlaying;
  autoPlayRef.current = autoPlay;

  const startNewGame = useCallback(async () => {
    if (busyRef.current) return;
    busyRef.current = true;
    setIsLoading(true);
    setAiHint(null);
    setKeepPlaying(false);
    try {
      const res = await api.newGame();
      setBoard(res.board);
      setScore(res.score);
      setGameStatus("PLAYING");
      setSpawnedCell(null);
    } finally {
      setIsLoading(false);
      busyRef.current = false;
    }
  }, []);

  const continueAfterWin = useCallback(() => {
    setKeepPlaying(true);
    setGameStatus("PLAYING");
  }, []);

  const processMove = useCallback(async (direction: api.Direction) => {
    const res = await api.move(boardRef.current, direction, scoreRef.current);

    if (!res.spawnedCell) return;

    setMoves(res.moves ?? []);
    setBoard(res.board);
    setScore(res.score);
    setSpawnedCell(res.spawnedCell);

    if (keepPlayingRef.current) {
      setGameStatus(res.gameState === "LOST" ? "LOST" : "PLAYING");
    } else {
      setGameStatus(res.gameState);
    }

    const newBest = Math.max(bestScoreRef.current, res.score);
    if (newBest > bestScoreRef.current) {
      setBestScore(newBest);
      localStorage.setItem("2048-best", String(newBest));
    }
  }, []);

  const drainQueue = useCallback(async () => {
    if (busyRef.current) return;
    busyRef.current = true;
    setIsLoading(true);
    setAiHint(null);

    try {
      while (moveQueueRef.current.length > 0) {
        const status = gameStatusRef.current;
        if (status === "LOST" || (status === "WON" && !keepPlayingRef.current))
          break;

        const dir = moveQueueRef.current.shift()!;
        await processMove(dir);
      }
    } finally {
      moveQueueRef.current = [];
      setIsLoading(false);
      busyRef.current = false;
    }
  }, [processMove]);

  const doMove = useCallback(
    (direction: api.Direction) => {
      const status = gameStatusRef.current;
      if (status === "LOST") return;
      if (status === "WON" && !keepPlayingRef.current) return;

      moveQueueRef.current.push(direction);
      drainQueue();
    },
    [drainQueue],
  );

  const getAiHint = useCallback(async () => {
    if (busyRef.current || gameStatusRef.current === "LOST") return;
    busyRef.current = true;
    setIsLoading(true);

    try {
      const res = await api.aiSuggest(boardRef.current);
      setAiHint(res.recommendedDirection);
    } finally {
      setIsLoading(false);
      busyRef.current = false;
    }
  }, []);

  const toggleAutoPlay = useCallback(() => {
    setAutoPlay((prev) => !prev);
  }, []);

  // Auto-play loop: ask AI for best move, execute it, repeat
  useEffect(() => {
    if (!autoPlay) return;

    let cancelled = false;

    const step = async () => {
      if (cancelled || !autoPlayRef.current) return;

      const status = gameStatusRef.current;
      if (status === "LOST" || (status === "WON" && !keepPlayingRef.current)) {
        setAutoPlay(false);
        return;
      }

      if (busyRef.current) {
        // Retry shortly
        setTimeout(step, 50);
        return;
      }

      try {
        // Get AI suggestion
        const aiRes = await api.aiSuggest(boardRef.current);
        if (cancelled || !autoPlayRef.current) return;

        if (!aiRes.recommendedDirection) {
          setAutoPlay(false);
          return;
        }

        // Execute the move
        const status2 = gameStatusRef.current;
        if (
          status2 === "LOST" ||
          (status2 === "WON" && !keepPlayingRef.current)
        ) {
          setAutoPlay(false);
          return;
        }

        busyRef.current = true;
        setIsLoading(true);

        const res = await api.move(
          boardRef.current,
          aiRes.recommendedDirection,
          scoreRef.current,
        );
        if (cancelled || !autoPlayRef.current) {
          busyRef.current = false;
          setIsLoading(false);
          return;
        }

        if (res.spawnedCell) {
          setMoves(res.moves ?? []);
          setBoard(res.board);
          setScore(res.score);
          setSpawnedCell(res.spawnedCell);
          setAiHint(aiRes.recommendedDirection);

          if (keepPlayingRef.current) {
            setGameStatus(res.gameState === "LOST" ? "LOST" : "PLAYING");
          } else {
            setGameStatus(res.gameState);
          }

          const newBest = Math.max(bestScoreRef.current, res.score);
          if (newBest > bestScoreRef.current) {
            setBestScore(newBest);
            localStorage.setItem("2048-best", String(newBest));
          }
        }

        busyRef.current = false;
        setIsLoading(false);

        // Schedule next step
        if (!cancelled && autoPlayRef.current) {
          setTimeout(step, AUTO_PLAY_DELAY);
        }
      } catch {
        busyRef.current = false;
        setIsLoading(false);
        setAutoPlay(false);
      }
    };

    // Start the loop
    setTimeout(step, AUTO_PLAY_DELAY);

    return () => {
      cancelled = true;
    };
  }, [autoPlay]);

  // Keyboard input
  useEffect(() => {
    const keyMap: Record<string, api.Direction> = {
      ArrowUp: "UP",
      ArrowDown: "DOWN",
      ArrowLeft: "LEFT",
      ArrowRight: "RIGHT",
      w: "UP",
      s: "DOWN",
      a: "LEFT",
      d: "RIGHT",
    };

    const handler = (e: KeyboardEvent) => {
      const dir = keyMap[e.key];
      if (dir) {
        e.preventDefault();
        doMove(dir);
      }
    };

    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [doMove]);

  // Touch/swipe input
  useEffect(() => {
    let startX = 0;
    let startY = 0;

    const onTouchStart = (e: TouchEvent) => {
      startX = e.touches[0].clientX;
      startY = e.touches[0].clientY;
    };

    const onTouchEnd = (e: TouchEvent) => {
      const dx = e.changedTouches[0].clientX - startX;
      const dy = e.changedTouches[0].clientY - startY;
      const absDx = Math.abs(dx);
      const absDy = Math.abs(dy);

      if (Math.max(absDx, absDy) < SWIPE_THRESHOLD) return;

      let dir: api.Direction;
      if (absDx > absDy) {
        dir = dx > 0 ? "RIGHT" : "LEFT";
      } else {
        dir = dy > 0 ? "DOWN" : "UP";
      }

      e.preventDefault();
      doMove(dir);
    };

    window.addEventListener("touchstart", onTouchStart, { passive: true });
    window.addEventListener("touchend", onTouchEnd, { passive: false });
    return () => {
      window.removeEventListener("touchstart", onTouchStart);
      window.removeEventListener("touchend", onTouchEnd);
    };
  }, [doMove]);

  // Start game on mount
  useEffect(() => {
    let cancelled = false;
    api.newGame().then((res) => {
      if (!cancelled) {
        setBoard(res.board);
        setScore(res.score);
        setGameStatus("PLAYING");
        setSpawnedCell(null);
      }
    });
    return () => {
      cancelled = true;
    };
  }, []);

  return {
    board,
    score,
    bestScore,
    gameStatus,
    isLoading,
    spawnedCell,
    moves,
    aiHint,
    autoPlay,
    startNewGame,
    doMove,
    getAiHint,
    continueAfterWin,
    toggleAutoPlay,
  };
}
