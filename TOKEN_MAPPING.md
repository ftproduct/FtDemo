# FT Design System Token Mapping

## Hardcoded Values → FT DS Tokens

### Spacing/Dimensions

| Hardcoded Value | FT DS Token | Notes |
|----------------|-------------|-------|
| `'4px'` | `var(--spacing-x1)` | |
| `'8px'` | `var(--spacing-x2)` | |
| `'12px'` | `var(--spacing-x3)` | |
| `'16px'` | `var(--spacing-x4)` | |
| `'20px'` | `var(--spacing-x5)` | |
| `'24px'` | `var(--spacing-x6)` | |
| `'32px'` | `var(--spacing-x8)` | |
| `'48px'` | `var(--spacing-x12)` | |
| `'100px'` | Use relative units or multiple tokens | |
| `'150px'` | Use relative units or multiple tokens | |
| `'180px'` | Use relative units or multiple tokens | |
| `'200px'` | Use relative units or multiple tokens | |
| `'250px'` | Use relative units or multiple tokens | Sidebar width - keep as is or use FT DS layout |
| `'400px'` | Use relative units | Min-height - use vh or flex |
| `'100vh'` | Keep as is | Viewport units acceptable |
| `'100%'` | Keep as is | Percentage units acceptable |
| `1` (as number) | Keep as is | Flex values acceptable |
| `100` (z-index) | Keep as is | Z-index values acceptable |

### Border

| Hardcoded Value | FT DS Token | Notes |
|----------------|-------------|-------|
| `'1px solid var(--color-border-primary)'` | Keep as is | Already using FT DS color token |
| Border radius `'4px'` | `var(--border-radius-sm)` | |
| Border radius `'8px'` | `var(--border-radius-md)` | |

### Animations/Transitions

| Hardcoded Value | FT DS Token | Notes |
|----------------|-------------|-------|
| `'0.2s ease'` | `var(--transition-normal)` | Check if FT DS has this |
| `'all 0.2s ease'` | Use FT DS transition token | |
| `'pulse 1.5s ease-in-out infinite'` | Keep as is | Animation name - acceptable |
| `'1.5s'` | Keep duration as is | |

### Typography

| Hardcoded Value | FT DS Token | Notes |
|----------------|-------------|-------|
| `'12px'` | `var(--font-size-xs)` | |
| `'14px'` | `var(--font-size-sm)` | |
| `'16px'` | `var(--font-size-base)` | |
| `'18px'` | `var(--font-size-md)` | |
| `'20px'` | `var(--font-size-lg)` | |
| `'24px'` | `var(--font-size-xl)` | |
| `'30px'` | `var(--font-size-2xl)` | |
| `'bold'` | `var(--font-weight-bold)` | |
| `'500'` | `var(--font-weight-medium)` | |
| `'600'` | `var(--font-weight-semibold)` | |

### Fallbacks for Non-Token Values

| Property | Approach |
|----------|----------|
| Large fixed widths (250px+) | Consider keeping or using flex/grid |
| Min-height | Use vh units or flex: 1 |
| Z-index | Keep numeric values (acceptable) |
| Percentages | Keep as is (acceptable) |
| Viewport units (vh, vw) | Keep as is (acceptable) |
| Flex values (0, 1, etc.) | Keep as is (acceptable) |
| Animation durations | Keep as is (acceptable) |
| Transform values | Keep as is (acceptable) |

## Files to Fix

### Priority 1 - Core Components
1. ✅ `components/layout/app-sidebar.tsx` - 250px, 100vh, 100, 0.2s ease
2. ✅ `components/layout/app-header.tsx` - Already good
3. ✅ `app/error.tsx` - 400px minHeight
4. ✅ `app/not-found.tsx` - 400px minHeight

### Priority 2 - Pages
5. `app/(routes)/components/page.tsx` - Many inline styles
6. `app/(routes)/my-journeys/page.tsx` - Many inline styles
7. `app/(routes)/dashboard/page.tsx` - 250px marginLeft
8. `app/(routes)/orders/page.tsx`
9. `app/(routes)/shipments/page.tsx`
10. `app/(routes)/assets/page.tsx`

### Priority 3 - UI Components
11. `components/ui/loading-skeleton.tsx` - 20px, 1.5s, 200px, 150px, 180px, 120px
12. `components/ui/missing-component.tsx`
13. `components/ui/error-boundary.tsx`

## Replacement Strategy

### Keep As Is (Acceptable)
- `100vh`, `100%`, `50%` - Viewport and percentage units
- `flex: 1`, `flex: 0` - Flex values
- `zIndex: 100` - Z-index values
- `minHeight: '400px'` - Can convert to var(--spacing-x100) if available, or keep
- `width: '250px'` - Sidebar width (consider keeping or using layout tokens)
- Animation durations and names

### Must Convert
- All explicit px values for spacing that have FT DS equivalents
- All border-radius values
- All typography sizes
- All font weights expressed as strings or numbers

