/** Shared shell constants for MobileTaPlay (Capacitor). */

export const APP_NAME = "MobileTaPlay";
export const APP_ID = "com.mobiletaplay.app";

/** Production entry — remote site, never bundled as game content. */
export const DEFAULT_ENTRY_URL = "https://h5mota.com/";

/** Hosts the WebView may navigate to. Extend only after review. */
export const ALLOWED_HOSTS = [
  "h5mota.com",
  "www.h5mota.com",
  "static.mota.press",
  "mota.press",
] as const;

export interface ShellSettings {
  entryUrl: string;
  virtualPadEnabled: boolean;
  gamepadEnabled: boolean;
}

export function defaultSettings(): ShellSettings {
  return {
    entryUrl: DEFAULT_ENTRY_URL,
    virtualPadEnabled: true,
    gamepadEnabled: true,
  };
}

export function isAllowedHost(url: string): boolean {
  try {
    const u = new URL(url);
    return (ALLOWED_HOSTS as readonly string[]).some(
      (h) => u.hostname === h || u.hostname.endsWith(`.${h}`),
    );
  } catch {
    return false;
  }
}
