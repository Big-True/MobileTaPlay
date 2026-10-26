import { describe, expect, it } from "vitest";
import { keyCodeForKey, installBridge } from "../src/index.js";
import { createDefaultMapping } from "@mobiletaplay/input-mapping";

describe("input-bridge", () => {
  it("maps known keys to keyCode", () => {
    expect(keyCodeForKey("ArrowUp")).toBe(38);
    expect(keyCodeForKey("Escape")).toBe(27);
    expect(keyCodeForKey("Space")).toBe(32);
    expect(keyCodeForKey("nope")).toBe(0);
  });

  it("installs and tears down without throwing", () => {
    const events: unknown[] = [];
    const h = installBridge({
      mapping: createDefaultMapping(),
      onSyntheticKey: (e) => events.push(e),
    });
    expect(h).toBeTruthy();
    h.emitControl("dpad_up", "down");
    h.emitControl("dpad_up", "up");
    expect(events).toHaveLength(2);
    h.destroy();
    h.emitControl("button_a", "down");
    expect(events).toHaveLength(2);
  });
});
