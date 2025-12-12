/**
 * Main application store using Zustand
 */

import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

interface AppState {
  // Theme
  theme: 'light' | 'dark';
  setTheme: (theme: 'light' | 'dark') => void;

  // User
  user: {
    id: string;
    name: string;
    email: string;
    role: string;
  } | null;
  setUser: (user: AppState['user']) => void;

  // UI State
  sidebarOpen: boolean;
  setSidebarOpen: (open: boolean) => void;

  // Loading states
  isLoading: boolean;
  setLoading: (loading: boolean) => void;
}

export const useAppStore = create<AppState>()(
  devtools(
    (set) => ({
      // Theme
      theme: 'light',
      setTheme: (theme) => set({ theme }),

      // User
      user: null,
      setUser: (user) => set({ user }),

      // UI State
      sidebarOpen: false,
      setSidebarOpen: (sidebarOpen) => set({ sidebarOpen }),

      // Loading states
      isLoading: false,
      setLoading: (isLoading) => set({ isLoading }),
    }),
    {
      name: 'ft-demo-store',
    }
  )
);
