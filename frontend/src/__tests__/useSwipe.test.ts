import { describe, it, expect, vi } from "vitest";
import { renderHook } from "@testing-library/react";
import { useSwipe } from "../hooks/useSwipe";

function simulateSwipe(startX: number, startY: number, endX: number, endY: number) {
  const touchStart = new TouchEvent("touchstart", {
    touches: [{ clientX: startX, clientY: startY } as Touch],
  });
  const touchEnd = new TouchEvent("touchend", {
    changedTouches: [{ clientX: endX, clientY: endY } as Touch],
    cancelable: true,
  });
  window.dispatchEvent(touchStart);
  window.dispatchEvent(touchEnd);
}

describe("useSwipe", () => {
  it("dispatches RIGHT on horizontal swipe right", () => {
    const onMove = vi.fn();
    renderHook(() => useSwipe(onMove));
    simulateSwipe(100, 200, 200, 200);
    expect(onMove).toHaveBeenCalledWith("RIGHT");
  });

  it("dispatches LEFT on horizontal swipe left", () => {
    const onMove = vi.fn();
    renderHook(() => useSwipe(onMove));
    simulateSwipe(200, 200, 100, 200);
    expect(onMove).toHaveBeenCalledWith("LEFT");
  });

  it("dispatches DOWN on vertical swipe down", () => {
    const onMove = vi.fn();
    renderHook(() => useSwipe(onMove));
    simulateSwipe(200, 100, 200, 200);
    expect(onMove).toHaveBeenCalledWith("DOWN");
  });

  it("dispatches UP on vertical swipe up", () => {
    const onMove = vi.fn();
    renderHook(() => useSwipe(onMove));
    simulateSwipe(200, 200, 200, 100);
    expect(onMove).toHaveBeenCalledWith("UP");
  });

  it("ignores swipes below threshold", () => {
    const onMove = vi.fn();
    renderHook(() => useSwipe(onMove));
    simulateSwipe(200, 200, 210, 205);
    expect(onMove).not.toHaveBeenCalled();
  });
});
