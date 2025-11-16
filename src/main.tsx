import { createRoot } from "react-dom/client";
import App from "./App.tsx";

// IMPORTANT: Load order matters for CSS variable overrides
// 1. Load package CSS first (with default variables)
import "ft-design-system/index.css";
// 2. Load custom CSS last (to override package defaults)
import "./index.css";

createRoot(document.getElementById("root")!).render(<App />);
  