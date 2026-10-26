import { describe, expect, it } from "vitest";
import { createDefaultMapping, validateMapping } from "../src/index.js";

describe("input-mapping", () => {
  it("creates a valid default mapping", () => {
    const m = createDefaultMapping();
    expect(validateMapping(m)).toEqual(m);
  });

  it("rejects invalid deadzone", () => {
    const m = createDefaultMapping();
    expect(() => validateMapping({ ...m, deadzone: 2 })).toThrow();
  });

  it("rejects missing binding", () => {
    const m = createDefaultMapping();
    const { button_a, ...rest } = m.bindings;
    expect(() => validateMapping({ ...m, bindings: rest })).toThrow();
  });
});
