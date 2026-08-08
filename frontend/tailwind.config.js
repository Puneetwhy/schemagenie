/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        accent: {
          DEFAULT: "#6366f1", // indigo-500, the app's single accent color
          dark: "#4f46e5",
          light: "#eef2ff",
        },
      },
    },
  },
  plugins: [],
};
