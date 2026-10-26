import { describe, expect, it } from "vitest";
import { isAllowedHost, defaultSettings, DEFAULT_ENTRY_URL } from "../src/index.js";

describe("shell-common", () => {
  it("allows h5mota hosts", () => {
    expect(isAllowedHost(DEFAULT_ENTRY_URL)).toBe(true);
    expect(isAllowedHost("https://static.mota.press/x")).toBe(true);
  });

  it("rejects unknown hosts", () => {
    expect(isAllowedHost("https://evil.example.com")).toBe(false);
    expect(isAllowedHost("not a url")).toBe(false);
  });

  it("has default settings", () => {
    expect(defaultSettings().entryUrl).toBe(DEFAULT_ENTRY_URL);
  });
});
