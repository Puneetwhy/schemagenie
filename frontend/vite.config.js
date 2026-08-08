import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Proxies /api/* to the Spring Boot backend during local dev, so the
// frontend can just call relative "/api/..." paths everywhere.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      "/api": {
        target: process.env.VITE_API_BASE_URL || "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
