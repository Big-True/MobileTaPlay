/**
 * MobileTaPlay Input Bridge — injectable IIFE source logic.
 *
 * Contract: only synthesize keyboard/touch events that mota-js already
 * understands (actions.js onkeyDown/onkeyUp/ondown/onup). Never bypass route.
 */

import type { Mapping } from "@mobiletaplay/input-mapping";

export interface SyntheticKeyEvent {
  type: "down" | "up";
  /** KeyboardEvent.key */
  key: string;
  /** KeyboardEvent.keyCode (legacy, used by mota-js) */
  keyCode: number;
}

export interface BridgeOptions {
  mapping: Mapping;
  onSyntheticKey: (e: SyntheticKeyEvent) => void;
}

export interface BridgeHandle {
  destroy(): void;
  setMapping(mapping: Mapping): void;
  /** Simulate a mapped control press/release (used by virtual pad / native gamepad). */
  emitControl(control: string, type: "down" | "up"): void;
}

/** Minimal key name → keyCode table (aligned with mota-js / DOM). */
const KEY_TO_CODE: Record<string, number> = {
  ArrowLeft: 37,
  ArrowUp: 38,
  ArrowRight: 39,
  ArrowDown: 40,
  Enter: 13,
  Space: 32,
  Escape: 27,
  Backspace: 8,
  t: 84,
  q: 81,
  x: 88,
  g: 71,
  s: 83,
  d: 68,
  a: 65,
  w: 87,
  b: 66,
  z: 90,
};

export function keyCodeForKey(key: string): number {
  return KEY_TO_CODE[key] ?? 0;
}

export function installBridge(opts: BridgeOptions): BridgeHandle {
  let mapping = opts.mapping;
  let destroyed = false;

  const press = (key: string, type: "down" | "up") => {
    if (destroyed) return;
    const keyCode = keyCodeForKey(key);
    if (!keyCode) return;
    opts.onSyntheticKey({ type, key, keyCode });
  };

  return {
    destroy() {
      destroyed = true;
    },
    setMapping(next: Mapping) {
      mapping = next;
    },
    emitControl(control: string, type: "down" | "up") {
      const binding = mapping.bindings[control as keyof Mapping["bindings"]];
      if (binding) press(binding.key, type);
    },
  };
}

// Re-export for injected bundle consumers
export type { Mapping };
