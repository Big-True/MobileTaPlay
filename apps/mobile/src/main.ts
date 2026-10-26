/**
 * MobileTaPlay mobile shell entry (settings UI / bootstrap).
 * Game content is the remote h5mota site — this file only runs shell surfaces
 * when not navigating to the remote server.url.
 */

import { defaultSettings, APP_NAME } from "@mobiletaplay/shell-common";
import { createDefaultMapping } from "@mobiletaplay/input-mapping";

const status = document.getElementById("status");

function bootstrap() {
  const settings = defaultSettings();
  const mapping = createDefaultMapping();
  if (status) {
    status.textContent = `${APP_NAME} · ${settings.entryUrl} · mapping v${mapping.version}`;
  }
  // M1: Capacitor server.url already points WebView at h5mota.
  // M2: inject input-bridge via native plugin here.
  console.info("[MobileTaPlay] shell bootstrap", { settings, mapping });
}

bootstrap();
