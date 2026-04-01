import { describe, it, expect, vi } from "vitest";
import { renderHook } from "@testing-library/react";
import { useKeyboard } from "../hooks/useKeyboard";

describe("useKeyboard", () => {
  it("dispatches correct direction for arrow keys", () => {
    const onMove = vi.fn();
    renderHook(() => useKeyboard(onMove));

    window.dispatchEvent(
      new KeyboardEvent("keydown", { key: "ArrowUp", cancelable: true }),
    );
    window.dispatchEvent(
      new KeyboardEvent("keydown", { key: "ArrowDown", cancelable: true }),
    );
    window.dispatchEvent(
      new KeyboardEvent("keydown", { key: "ArrowLeft", cancelable: true }),
    );
    window.dispatchEvent(
      new KeyboardEvent("keydown", { key: "ArrowRight", cancelable: true }),
    );

    expect(onMove).toHaveBeenCalledTimes(4);
    expect(onMove).toHaveBeenNthCalledWith(1, "UP");
    expect(onMove).toHaveBeenNthCalledWith(2, "DOWN");
    expect(onMove).toHaveBeenNthCalledWith(3, "LEFT");
    expect(onMove).toHaveBeenNthCalledWith(4, "RIGHT");
  });

  it("dispatches correct direction for WASD keys", () => {
    const onMove = vi.fn();
    renderHook(() => useKeyboard(onMove));

    window.dispatchEvent(new KeyboardEvent("keydown", { key: "w" }));
    window.dispatchEvent(new KeyboardEvent("keydown", { key: "s" }));
    window.dispatchEvent(new KeyboardEvent("keydown", { key: "a" }));
    window.dispatchEvent(new KeyboardEvent("keydown", { key: "d" }));

    expect(onMove).toHaveBeenCalledTimes(4);
    expect(onMove).toHaveBeenNthCalledWith(1, "UP");
    expect(onMove).toHaveBeenNthCalledWith(2, "DOWN");
    expect(onMove).toHaveBeenNthCalledWith(3, "LEFT");
    expect(onMove).toHaveBeenNthCalledWith(4, "RIGHT");
  });

  it("ignores unrelated keys", () => {
    const onMove = vi.fn();
    renderHook(() => useKeyboard(onMove));

    window.dispatchEvent(new KeyboardEvent("keydown", { key: "Enter" }));
    window.dispatchEvent(new KeyboardEvent("keydown", { key: "x" }));
    window.dispatchEvent(new KeyboardEvent("keydown", { key: "Escape" }));

    expect(onMove).not.toHaveBeenCalled();
  });

  it("calls preventDefault for game keys", () => {
    const onMove = vi.fn();
    renderHook(() => useKeyboard(onMove));

    const event = new KeyboardEvent("keydown", {
      key: "ArrowUp",
      cancelable: true,
    });
    const preventSpy = vi.spyOn(event, "preventDefault");
    window.dispatchEvent(event);

    expect(preventSpy).toHaveBeenCalled();
  });
});
