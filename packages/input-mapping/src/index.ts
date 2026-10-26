/**
 * MobileTaPlay input mapping types.
 * Keys are logical controls; values map to KeyboardEvent.keyCode-compatible keys
 * consumed by mota-js actions.js.
 */

export type ControlId =
  | "dpad_up"
  | "dpad_down"
  | "dpad_left"
  | "dpad_right"
  | "button_a"
  | "button_b"
  | "button_x"
  | "button_y"
  | "lb"
  | "rb"
  | "lt"
  | "rt"
  | "start"
  | "select";

export interface Binding {
  /** Keyboard key name or keyCode number string; refined in M2 with real devices. */
  key: string;
}

export interface Mapping {
  version: number;
  /** Analog stick deadzone, 0..1 */
  deadzone: number;
  bindings: Record<ControlId, Binding>;
}

export function createDefaultMapping(): Mapping {
  return {
    version: 1,
    deadzone: 0.35,
    bindings: {
      dpad_up: { key: "ArrowUp" },
      dpad_down: { key: "ArrowDown" },
      dpad_left: { key: "ArrowLeft" },
      dpad_right: { key: "ArrowRight" },
      // Confirm / soft-press — engine Space/Enter semantics (M2 calibrate)
      button_a: { key: "Space" },
      button_b: { key: "Escape" },
      button_x: { key: "t" },
      button_y: { key: "q" },
      lb: { key: "x" },
      rb: { key: "g" },
      lt: { key: "s" },
      rt: { key: "d" },
      start: { key: "Escape" },
      select: { key: "b" },
    },
  };
}

export function validateMapping(input: unknown): Mapping {
  if (!input || typeof input !== "object") {
    throw new Error("mapping must be an object");
  }
  const m = input as Partial<Mapping>;
  if (typeof m.version !== "number") throw new Error("mapping.version required");
  if (typeof m.deadzone !== "number" || m.deadzone < 0 || m.deadzone > 1) {
    throw new Error("mapping.deadzone must be 0..1");
  }
  if (!m.bindings || typeof m.bindings !== "object") {
    throw new Error("mapping.bindings required");
  }
  const defaults = createDefaultMapping();
  for (const id of Object.keys(defaults.bindings) as ControlId[]) {
    const b = m.bindings[id];
    if (!b || typeof b.key !== "string" || b.key.length === 0) {
      throw new Error(`mapping.bindings.${id} invalid`);
    }
  }
  return {
    version: m.version,
    deadzone: m.deadzone,
    bindings: m.bindings as Mapping["bindings"],
  };
}
