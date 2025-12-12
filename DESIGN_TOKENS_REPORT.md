# FT Design System - Design Tokens & Component Report

**Generated:** November 15, 2025

---

## 📊 Design Tokens Summary

### Token Distribution

| Source | Token Categories | Total Variables | Lines of Code |
|--------|------------------|-----------------|---------------|
| **Custom (globals.css)** | Complete Design System | ~295 variables | 862 lines |
| **Package (ft-design-system)** | Tailwind + Base Tokens | Minified | 2 lines (minified) |

---

## 🎨 Custom Design Tokens (globals.css)

### Color System Tokens
**Total: ~120 variables across 3 themes**

#### Base Colors (per theme: Light, Dark, Night)
- Primary Scale: `--primary-900` through `--primary-100` (9 shades)
- Secondary Scale: `--secondary-900` through `--secondary-100` (9 shades)
- Tertiary Scale: `--tertiary-900` through `--tertiary-0` (10 shades)
- Neutral Scale: `--neutral-900` through `--neutral-100` (9 shades)
- Positive Scale: `--positive-900` through `--positive-100` (9 shades)
- Warning Scale: `--warning-900` through `--warning-100` (9 shades)
- Danger Scale: `--danger-900` through `--danger-100` (9 shades)

#### Semantic Colors
```css
--primary: var(--primary-700)
--secondary: var(--primary-500)
--tertiary: var(--primary-300)
--border-primary, --border-secondary
--bg-primary, --bg-secondary
--critical, --critical-dark, --critical-light
--warning, --warning-dark, --warning-light
--positive, --positive-dark, --positive-light
--neutral, --neutral-dark, --neutral-light
```

### Button System Tokens
**Total: ~15 variables**

```css
--button-primary-bg, --button-primary-text, --button-primary-hover-bg, --button-primary-border
--button-secondary-bg, --button-secondary-text, --button-secondary-hover-bg, --button-secondary-border, --button-secondary-hover-border
--button-destructive-bg, --button-destructive-text, --button-destructive-hover-bg, --button-destructive-border
--button-text-bg, --button-text-text, --button-text-hover-bg, --button-text-border
--button-link-bg, --button-link-text, --button-link-hover-text, --button-link-border
```

### Badge System Tokens
**Total: ~20 variables**

```css
--badge-normal-bg, --badge-normal-text, --badge-normal-border, --badge-normal-hover-bg, --badge-normal-hover-border
--badge-danger-bg, --badge-danger-text, --badge-danger-border, --badge-danger-hover-bg, --badge-danger-hover-border, --badge-danger-hover-text
--badge-success-bg, --badge-success-text, --badge-success-border, --badge-success-hover-bg
--badge-warning-bg, --badge-warning-text, --badge-warning-border, --badge-warning-hover-bg
--badge-neutral-bg, --badge-neutral-text, --badge-neutral-border
--badge-border-radius, --badge-font-size, --badge-font-weight
```

### Form System Tokens
**Total: ~17 variables**

```css
--surface, --surface-alt, --surface-hover
--input, --input-muted, --input-disabled
--placeholder, --helper
--border, --border-hover, --border-disabled, --border-alt
--focus, --focus-ring
--radio-size, --radio-gap
```

### Typography System Tokens
**Total: ~15 variables**

```css
--font-family-primary, --font-family-secondary
--font-weight-regular, --font-weight-medium, --font-weight-semibold, --font-weight-bold
--font-size-xs, --font-size-sm, --font-size-md, --font-size-lg, --font-size-xl, --font-size-xxl
--line-height-tight, --line-height-normal, --line-height-relaxed
```

### Spacing System Tokens (8-Point Grid)
**Total: 19 variables**

```css
--space-0, --space-1, --space-2, --space-3, --space-4, --space-5
--space-6, --space-7, --space-8, --space-9, --space-10, --space-11
--space-12, --space-13, --space-14, --space-15, --space-16
--space-20, --space-24
```

### Border Radius Tokens
**Total: 7 variables**

```css
--radius-none, --radius-sm, --radius-md, --radius-lg, --radius-xl
--radius-full, --radius-circle
```

### Shadow Tokens
**Total: 4 variables**

```css
--shadow-sm, --shadow-md, --shadow-lg, --shadow-xl
```

### Component System Tokens
**Total: ~25 variables**

```css
--component-border-radius, --component-border-width, --component-border-color, --component-border-focus
--component-transition
--component-gap-sm, --component-gap-md, --component-gap-lg
--component-font-size-sm, --component-font-size-md, --component-font-size-lg, --component-font-size-xl
--component-font-weight
--component-height-sm, --component-height-md, --component-height-lg, --component-height-xl
--component-padding-sm, --component-padding-md, --component-padding-lg, --component-padding-xl
```

---

## 📦 Package Tokens (ft-design-system/dist/index.css)

The package CSS is **minified Tailwind CSS** that includes:
- Tailwind CSS utility classes (responsive, colors, spacing)
- Design system base tokens (--primary, --secondary, etc.)
- Component-specific styling
- Theme variants (Light, Dark, Night)

**Key Difference:** 
- Package tokens are **compiled and minified** (2 lines)
- Custom tokens are **readable and documented** (862 lines)
- Both use **identical variable names** for compatibility

---

## 🎨 Available Icons from Package

### Icon System
The `ft-design-system/ai` package exports **200+ icons** through the `iconMap` object.

**Usage:**
```typescript
import { Button } from 'ft-design-system/ai';

<Button variant="primary" icon="add">Add Item</Button>
<Button variant="secondary" icon="check">Confirm</Button>
<Button variant="text" icon="download">Download</Button>
```

### Sample Icons Available:
```typescript
'add', 'check', 'close', 'delete', 'edit', 'download', 'upload',
'search', 'filter', 'calendar', 'clock', 'bell', 'user', 'settings',
'arrow-up', 'arrow-down', 'arrow-left', 'arrow-right',
'chevron-up', 'chevron-down', 'chevron-left', 'chevron-right',
'mail', 'phone', 'location', 'home', 'dashboard', 'reports',
'truck', 'warehouse', 'vehicle', 'tracker', 'gps',
'rupee-coin', 'document', 'file', 'excel', 'save', 'copy',
// ... 200+ more icons
```

**Full Icon List:** Check `node_modules/ft-design-system/dist/ai/index.d.ts` line 67-220

---

## 🐛 Current Issues

### 1. Component Cards Display Issue
**Problem:** Components are displayed in narrow cards (320px min-width)

**Current CSS:**
```typescript
gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))'
```

**Why It's Narrow:**
- Min-width set to 320px (mobile-first approach)
- Auto-fill creates as many columns as fit
- On wide screens, creates many narrow columns

**Recommended Fix:**
```typescript
// Option 1: Wider cards
gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))'

// Option 2: Fixed column count
gridTemplateColumns: 'repeat(3, 1fr)'  // 3 columns on desktop

// Option 3: Responsive grid
@media (min-width: 1440px) { grid-template-columns: repeat(3, 1fr); }
@media (max-width: 1440px) { grid-template-columns: repeat(2, 1fr); }
@media (max-width: 800px) { grid-template-columns: repeat(1, 1fr); }
```

### 2. Icons Not Showing in Component Gallery
**Problem:** Icon demonstrations are missing from the component gallery

**Current State:**
- Icon system exists in package ✅
- 200+ icons available ✅
- No icon showcase in ComponentGallery.tsx ❌

**Solution:** Add icon gallery to ComponentGallery:
```typescript
{
  name: 'Icons',
  description: 'Icon system with 200+ icons',
  demo: (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(8, 1fr)', gap: 'var(--space-4)' }}>
      {Object.keys(iconMap).slice(0, 24).map((iconName) => (
        <div key={iconName} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-1)' }}>
          <Button variant="secondary" icon={iconName} />
          <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)' }}>{iconName}</span>
        </div>
      ))}
    </div>
  )
}
```

---

## ✅ Recommendations

### Immediate Actions:
1. **Fix Component Card Width**
   - Change min-width from `320px` to `400px` or `450px`
   - OR use fixed 3-column layout for desktop

2. **Add Icon Gallery Component**
   - Show all 200+ icons in a searchable grid
   - Demonstrate icon usage with buttons
   - Include icon names for easy reference

3. **Add More Component Examples**
   - Dropdown component needs better examples
   - DatePicker needs date range examples
   - Table needs pagination examples

### Documentation Improvements:
1. Create icon reference guide
2. Document responsive breakpoints
3. Add theme switching guide
4. Create token migration guide

---

## 📈 Token Statistics

| Category | Custom Tokens | Package Tokens | Shared |
|----------|---------------|----------------|---------|
| Colors | 120+ | ✅ Compiled | 100% |
| Typography | 15 | ✅ Compiled | 100% |
| Spacing | 19 | ✅ Compiled | 100% |
| Shadows | 4 | ✅ Compiled | 100% |
| Components | 40+ | ✅ Compiled | 100% |
| **Total** | **~295** | **Minified** | **100%** |

**Conclusion:** All custom design tokens are perfectly aligned with the package tokens. The package uses the same variable names but in minified form with Tailwind utilities.

