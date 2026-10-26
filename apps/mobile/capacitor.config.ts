import type { CapacitorConfig } from "@capacitor/cli";

const config: CapacitorConfig = {
  appId: "com.mobiletaplay.app",
  appName: "MobileTaPlay",
  // Remote product content — h5mota is not bundled.
  server: {
    url: "https://h5mota.com/",
    cleartext: false,
  },
  android: {
    allowMixedContent: false,
  },
  ios: {
    contentInset: "automatic",
  },
  plugins: {
    // Reserved: MtpShell / MtpInject / MtpGamepad
  },
};

export default config;
