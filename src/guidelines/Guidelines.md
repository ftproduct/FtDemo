# Freight Tiger Design System Guidelines

## Overview
This design system is built for the Freight Tiger TMS (Transport Management System) POC, covering both FTL (Full Truckload) and PTL (Part Truckload) operations from planning to invoicing.

## Design Principles

### Responsive Design
- **Breakpoint-based rem sizing**:
  - Screen width > 1440px: `1rem = 16px`
  - Screen width ≤ 1440px: `1rem = 14px`
- All spacing and sizing should use rem units to scale proportionally

### Grid System
- **24-column grid** for precise, flexible layouts
- **Responsive gutters**:
  - 20px gutters when rem = 16px (screens > 1440px)
  - 16px gutters when rem = 14px (screens ≤ 1440px)
- Use `.grid-24` class for the 24-column grid container
- Use `.col-span-{n}` utilities for column spanning (e.g., `.col-span-8`, `.col-span-12`)

### Spacing Scale
- Based on 8px increments (0.5rem at base 16px)
- Use consistent spacing: 8px, 16px, 24px, 32px, 40px, 48px
- Utility classes: `.space-8`, `.space-16`, `.space-24`, `.space-32`, `.space-40`, `.space-48`

### Container
- Use `.container-custom` for page-level containers
- Max-width: 1440px
- Responsive horizontal padding matches gutter sizing

## Typography

### Font Family
- **Primary font**: Inter (Google Fonts)
- Import: `@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap');`

### Type Scale
Use CSS variables defined in `/styles/globals.css`:
- `--text-2xl`: 28px (h1)
- `--text-xl`: 24px (h2)
- `--text-lg`: 20px (h3)
- `--text-base`: 16px (body, buttons)
- `--text-sm`: 14px (labels, captions)

### Font Weights
- Regular: 400 (`--font-weight-regular`)
- Medium: 500 (`--font-weight-medium`)
- Semibold: 600 (`--font-weight-semibold`)

### Usage
- **Do NOT** use Tailwind font-size, font-weight, or line-height classes unless specifically requested
- Typography is automatically applied via HTML elements (h1, h2, h3, h4, p, span, label, button, input)
- Line-height is set to 1.5 across all elements

## Color System

### Semantic Colors
All colors are defined as CSS variables in `/styles/globals.css`. Use Tailwind color utilities (e.g., `bg-primary`, `text-accent`, `border-border`):

**Base Colors**
- `--background`: Default app background (rgb(248, 248, 249))
- `--foreground`: Default text color (rgb(67, 79, 100))
- `--card`: Card/container background (rgb(255, 255, 255))
- `--border`: Default border color (rgb(206, 209, 215))

**Interactive Colors**
- `--primary`: Primary buttons, links (rgb(67, 79, 100))
- `--secondary`: Secondary elements (rgb(255, 255, 255))
- `--accent`: Highlights, important actions (rgb(255, 193, 9))
- `--muted`: Disabled states (rgb(206, 209, 215))

**Status Colors**
- `--success`: Success states (rgb(0, 198, 56))
- `--warning`: Warning states (rgb(255, 108, 25))
- `--destructive`: Error/delete actions (rgb(255, 53, 51))

**Chart Colors**
- `--chart-1` through `--chart-5` for data visualization

### Foreground Pairings
Each color has a `-foreground` variant for text/elements on top of that color (e.g., `--primary-foreground`, `--accent-foreground`)

## Components

All components are imported from `/components/ui/` and follow shadcn/ui patterns.

### Key Components
- **Button**: Primary interactive element with variants (default, secondary, outline, destructive, ghost)
- **Card**: Container with header, content, and footer sections
- **Input**: Text input fields with label support
- **Select**: Dropdown selection
- **Table**: Data tables with headers and rows
- **Tabs**: Tabbed navigation for content organization
- **Dialog**: Modal overlays for important actions
- **Drawer**: Slide-in panels
- **Badge**: Status indicators and labels
- **Breadcrumb**: Navigation trails
- **Pagination**: Page navigation controls
- **Toast**: Temporary notifications (use `toast` from 'sonner@2.0.3')

### Component Import Pattern
```tsx
import { Button } from '../ui/button';
import { Card, CardHeader, CardTitle, CardContent } from '../ui/card';
```

## Border Radius

Use CSS variables for consistent rounded corners:
- `--radius`: 8px (default for containers, tooltips)
- `--radius-button`: 8px (specifically for buttons)
- `--radius-full`: 9999px (fully rounded elements)
- Tailwind utilities: `rounded-sm`, `rounded-md`, `rounded-lg`, `rounded-xl`

## Accessibility

- **Contrast**: Maintain minimum 4.5:1 contrast ratio for text
- **Focus states**: Always visible with `--ring` color
- **Keyboard navigation**: All interactive elements must be keyboard accessible
- **Labels**: All form inputs must have associated labels

## Layout Best Practices

1. **Use the 24-column grid** for complex layouts requiring precise control
2. **Avoid absolute positioning** unless absolutely necessary
3. **Prefer flexbox and grid** for responsive layouts
4. **Use semantic HTML** (header, nav, main, section, footer)
5. **Keep component files small** - split into separate files as needed

## File Structure

```
/components/ui/          # Atomic design system components
/components/pages/       # Page-level components
/styles/globals.css      # Design tokens and base styles
/guidelines/Guidelines.md # This file
```

## TMS-Specific Guidelines

### Journey Workflow Stages
The TMS covers these journey stages:
1. **Planning**: Route and load planning
2. **In Transit**: Active shipment tracking
3. **Completed**: Delivered shipments
4. **Invoiced**: Invoicing and billing

### Data Display
- Use **Tables** for list views (journeys, shipments, invoices)
- Use **Cards** for summary information and KPI tiles
- Use **Badges** for status indicators (Active, Pending, Completed, etc.)
- Use **Tabs** to organize content by journey stage

### Actions
- **Primary actions** (Create Journey, Submit) use primary buttons
- **Secondary actions** (Cancel, Edit) use secondary/outline buttons
- **Destructive actions** (Delete) use destructive variant
- Place primary actions on the right side of button groups

## Code Style

- Use TypeScript for all `.tsx` files
- Use functional components with hooks
- Prefer named exports for page components
- Use default exports only for `/App.tsx`
- Keep props interfaces clear and well-typed

---

**Last Updated**: October 18, 2025
