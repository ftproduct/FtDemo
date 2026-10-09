# FT Demo Application

A complete demo application built with Next.js 14, TypeScript, and the FT Design System.

## Tech Stack

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript (strict mode)
- **Styling**: Tailwind CSS + FT Design System
- **State Management**: Zustand
- **Data Fetching**: React Query (@tanstack/react-query)
- **Validation**: Zod
- **UI Components**: ft-design-system npm package

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build

```bash
npm run build
npm start
```

## Project Structure

```
/app              → Next.js App Router pages and API routes
/components       → Shared React components
/hooks           → Custom React hooks
/lib             → Utilities, API client, state management
/types           → TypeScript type definitions
/mock-db         → Mock database JSON files
```

## Mock API

The application includes mock API routes in `/app/api` that simulate backend endpoints with realistic data and latency.

## FT Design System

All UI components use the `ft-design-system` npm package. Custom components should only be created when FT DS components are not available.

## Code Quality

- TypeScript strict mode enabled
- ESLint configured
- All API calls typed with Zod schemas
- Production-ready code standards

## Freight Tiger Driver Assistant (Android)

A separate, Hindi-first voice assistant app for truck drivers lives in
[`driver-assistant-android/`](driver-assistant-android/README.md). It is an independent Gradle project
(Kotlin, Jetpack Compose) and does not affect this web app's build.
