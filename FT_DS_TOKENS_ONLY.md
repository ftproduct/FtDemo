# FT Design System Tokens Only - Project Rule

## 🎯 Project Rule: 100% FT DS Tokens

This project follows a **ZERO TOLERANCE** policy for hardcoded design values. Every color, spacing, typography, border, shadow, and icon MUST use FT Design System tokens.

## Why Tokens Only?

### Benefits
1. **Theme Consistency** - Automatic theming (light/dark/night modes)
2. **Maintainability** - Update design system, not scattered values
3. **Scalability** - Easy to add new themes or variants
4. **Performance** - Browser-optimized CSS variables
5. **Accessibility** - Design system ensures WCAG compliance
6. **Developer Experience** - Clear, semantic naming

### What We Avoid
- ❌ Magic numbers scattered throughout code
- ❌ Inconsistent spacing/colors across components
- ❌ Theme changes requiring code changes
- ❌ Duplicate color/spacing definitions
- ❌ Maintenance nightmares

## Available FT DS Tokens

### 🎨 Colors

#### Text Colors
```tsx
color: 'var(--color-primary)'     // Primary text
color: 'var(--color-secondary)'   // Secondary text
color: 'var(--color-tertiary)'    // Tertiary/muted text
```

#### Background Colors
```tsx
backgroundColor: 'var(--color-bg-primary)'    // Primary background
backgroundColor: 'var(--color-bg-secondary)'  // Secondary background
```

#### Border Colors
```tsx
border: '1px solid var(--color-border-primary)'    // Primary borders
border: '1px solid var(--color-border-secondary)'  // Secondary borders
```

#### Semantic Colors
```tsx
color: 'var(--color-positive)'   // Success/positive states
color: 'var(--color-warning)'    // Warning states
color: 'var(--color-critical)'   // Error/critical states
color: 'var(--color-neutral)'    // Neutral states
```

### 📏 Spacing

All spacing uses a consistent scale:

```tsx
// Most common values
padding: 'var(--spacing-x1)'   // 4px
padding: 'var(--spacing-x2)'   // 8px
padding: 'var(--spacing-x3)'   // 12px
padding: 'var(--spacing-x4)'   // 16px
padding: 'var(--spacing-x5)'   // 20px
padding: 'var(--spacing-x6)'   // 24px
padding: 'var(--spacing-x8)'   // 32px
padding: 'var(--spacing-x10)'  // 40px
padding: 'var(--spacing-x12)'  // 48px

// Also available: x0, x7, x9, x11, x13, x14, x15, x16, x20, x24
```

**Use for:**
- `padding`, `margin`, `gap`
- `top`, `left`, `right`, `bottom`
- Any spacing-related properties

### 📝 Typography

#### Font Sizes
```tsx
fontSize: 'var(--font-size-xs)'     // 12px
fontSize: 'var(--font-size-sm)'     // 14px
fontSize: 'var(--font-size-base)'   // 16px
fontSize: 'var(--font-size-md)'     // 18px
fontSize: 'var(--font-size-lg)'     // 20px
fontSize: 'var(--font-size-xl)'     // 24px
fontSize: 'var(--font-size-2xl)'    // 30px
```

#### Font Weights
```tsx
fontWeight: 'var(--font-weight-regular)'   // 400
fontWeight: 'var(--font-weight-medium)'    // 500
fontWeight: 'var(--font-weight-semibold)'  // 600
fontWeight: 'var(--font-weight-bold)'      // 700
```

#### Line Heights
```tsx
lineHeight: 'var(--line-height-tight)'    // 1.25
lineHeight: 'var(--line-height-normal)'   // 1.5
lineHeight: 'var(--line-height-relaxed)'  // 1.75
```

#### Font Families
```tsx
fontFamily: 'var(--font-family-primary)'    // Primary font
fontFamily: 'var(--font-family-secondary)'  // Secondary font
```

### 🔄 Border Radius
```tsx
borderRadius: 'var(--border-radius-none)'  // 0px
borderRadius: 'var(--border-radius-sm)'    // 4px
borderRadius: 'var(--border-radius-md)'    // 8px
borderRadius: 'var(--border-radius-lg)'    // 12px
borderRadius: 'var(--border-radius-xl)'    // 16px
borderRadius: 'var(--border-radius-full)'  // 9999px (circular)
```

### 🌑 Shadows
```tsx
boxShadow: 'var(--shadow-sm)'   // Small shadow
boxShadow: 'var(--shadow-md)'   // Medium shadow
boxShadow: 'var(--shadow-lg)'   // Large shadow
boxShadow: 'var(--shadow-xl)'   // Extra large shadow
```

### ⚡ Transitions
```tsx
transition: 'var(--transition-fast)'    // Fast transitions
transition: 'var(--transition-normal)'  // Normal transitions
transition: 'var(--transition-slow)'    // Slow transitions
```

## 🚫 What NOT to Use

### Never Hardcode These:

```tsx
// ❌ WRONG - Hardcoded colors
color: '#000000'
color: 'black'
color: 'rgba(0, 0, 0, 0.5)'
backgroundColor: '#ffffff'

// ✅ CORRECT - FT DS tokens
color: 'var(--color-primary)'
backgroundColor: 'var(--color-bg-primary)'
```

```tsx
// ❌ WRONG - Hardcoded spacing
padding: '16px'
margin: '8px'
gap: '12px'

// ✅ CORRECT - FT DS tokens
padding: 'var(--spacing-x4)'
margin: 'var(--spacing-x2)'
gap: 'var(--spacing-x3)'
```

```tsx
// ❌ WRONG - Hardcoded typography
fontSize: '14px'
fontSize: '1rem'
fontWeight: '600'
fontWeight: 'bold'

// ✅ CORRECT - FT DS tokens
fontSize: 'var(--font-size-sm)'
fontWeight: 'var(--font-weight-semibold)'
```

```tsx
// ❌ WRONG - Hardcoded border radius
borderRadius: '8px'
borderRadius: '0.5rem'

// ✅ CORRECT - FT DS tokens
borderRadius: 'var(--border-radius-md)'
```

## ✅ Acceptable Exceptions

Some values don't need tokens:

### Layout Values
```tsx
// ✅ Viewport units - OK
height: '100vh'
width: '100vw'

// ✅ Percentages - OK
width: '100%'
width: '50%'

// ✅ Flex values - OK
flex: 1
flex: 0

// ✅ Z-index - OK (use sparingly)
zIndex: 100
zIndex: 1000
```

### Positioning
```tsx
// ✅ Fixed positioning values - OK
position: 'fixed'
top: 0
left: 0

// ✅ Transform values - OK
transform: 'translateX(-50%)'
```

### Animation
```tsx
// ✅ Animation durations - OK
animation: 'pulse 1.5s ease-in-out infinite'

// ✅ Animation names - OK (but use FT DS transitions when possible)
```

### Component-Specific Widths
```tsx
// ✅ Component-specific fixed widths - OK when necessary
width: '250px'  // Sidebar width
width: '236px'  // Specific dropdown width from design
```

**Rule of Thumb:** If it's not a spacing/color/typography value that should be themeable, it's probably OK.

## 🎨 FT DS Icons (200+)

### Icon Usage

FT DS provides 200+ icons. ALWAYS use FT DS icons.

```tsx
import {
  // Navigation
  ChevronDown, ChevronUp, ChevronLeft, ChevronRight,
  ArrowLeft, ArrowRight, ArrowUp, ArrowDown,
  
  // Actions
  Add, Edit, Delete, Copy, Save, Send, Share,
  Upload, Download, Refresh,
  
  // Status
  Check, Cross, AlertCritical, AlertInformational,
  Success, WarningIcon,
  
  // UI
  HamburgerMenu, Search, Filter, Settings,
  Bell, Notification, More, Close,
  
  // Content
  File, Document, Image, Calendar, Clock,
  User, Mail, Phone,
} from 'ft-design-system';

// Use in JSX
<Search />
<Button icon="search" />
```

### Finding Icons

Check available icons:
```bash
# Browse icon files
ls node_modules/ft-design-system/dist/types/components/atoms/Icons/

# Or check the Icon component props
```

**Rule:** Never create custom icons. If an icon doesn't exist, request it from the design system team.

## 📋 Code Review Checklist

Before committing code, verify:

- [ ] No hardcoded colors (no `#`, `rgb`, `rgba` for design colors)
- [ ] No hardcoded spacing (no `px` values for spacing)
- [ ] No hardcoded font sizes (no `px` or `rem` for typography)
- [ ] No hardcoded font weights (no `400`, `600`, `bold`, etc.)
- [ ] No hardcoded border radius (no `px` for border radius)
- [ ] All icons from FT DS icon library
- [ ] Uses FT DS transitions when animating
- [ ] Exceptions are documented and justified

## 🛠️ Migration Guide

### Step 1: Find Hardcoded Values
```bash
# Search for hardcoded px values
grep -r ":\s*['\"]?\d\+px" app/ components/

# Search for hardcoded colors
grep -r "#[0-9a-fA-F]\{6\}" app/ components/

# Search for rgba/rgb
grep -r "rgba\?(" app/ components/
```

### Step 2: Replace with Tokens

Use the TOKEN_MAPPING.md file for common conversions:
- `4px` → `var(--spacing-x1)`
- `8px` → `var(--spacing-x2)`
- `16px` → `var(--spacing-x4)`
- `#000` → `var(--color-primary)`
- `14px` font → `var(--font-size-sm)`
- `bold` → `var(--font-weight-bold)`

### Step 3: Test Themes
```tsx
// Test your component in different themes
<FTProvider theme="light">
  <YourComponent />
</FTProvider>

<FTProvider theme="dark">
  <YourComponent />
</FTProvider>

<FTProvider theme="night">
  <YourComponent />
</FTProvider>
```

## 🎓 Examples

### Before & After

#### Example 1: Button
```tsx
// ❌ BEFORE - Hardcoded values
<button style={{
  padding: '12px 24px',
  fontSize: '14px',
  fontWeight: '600',
  color: '#000',
  backgroundColor: '#fff',
  borderRadius: '8px',
  border: '1px solid #ccc',
}}>
  Click Me
</button>

// ✅ AFTER - FT DS tokens
<Button 
  variant="primary" 
  size="md"
>
  Click Me
</Button>
// FT DS Button already uses all tokens internally!
```

#### Example 2: Card
```tsx
// ❌ BEFORE - Hardcoded values
<div style={{
  padding: '16px',
  borderRadius: '8px',
  backgroundColor: '#ffffff',
  border: '1px solid #e5e5e5',
  boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
}}>
  <h2 style={{ 
    fontSize: '18px', 
    fontWeight: '600',
    color: '#000',
    marginBottom: '8px'
  }}>
    Title
  </h2>
  <p style={{ 
    fontSize: '14px', 
    color: '#666'
  }}>
    Description
  </p>
</div>

// ✅ AFTER - FT DS tokens
<Card>
  <CardHeader>
    <CardTitle>Title</CardTitle>
    <CardDescription>Description</CardDescription>
  </CardHeader>
</Card>
// FT DS Card already uses all tokens internally!
```

#### Example 3: Custom Component
```tsx
// ❌ BEFORE - Hardcoded values
<div style={{
  display: 'flex',
  gap: '12px',
  padding: '16px',
  backgroundColor: '#f5f5f5',
  borderRadius: '4px',
}}>
  <span style={{ 
    fontSize: '14px', 
    fontWeight: '500',
    color: '#333'
  }}>
    Label
  </span>
</div>

// ✅ AFTER - FT DS tokens
<div style={{
  display: 'flex',
  gap: 'var(--spacing-x3)',
  padding: 'var(--spacing-x4)',
  backgroundColor: 'var(--color-bg-secondary)',
  borderRadius: 'var(--border-radius-sm)',
}}>
  <span style={{ 
    fontSize: 'var(--font-size-sm)', 
    fontWeight: 'var(--font-weight-medium)',
    color: 'var(--color-primary)'
  }}>
    Label
  </span>
</div>
```

## 🔍 Verification

### Run Token Check
```bash
# Check for hardcoded spacing
grep -r "padding:\s*['\"]?\d\+px" app/ components/ | grep -v node_modules

# Check for hardcoded colors
grep -r "#[0-9a-fA-F]\{6\}" app/ components/ | grep -v node_modules

# Check for hardcoded font sizes
grep -r "fontSize:\s*['\"]?\d\+px" app/ components/ | grep -v node_modules
```

### Expected Result
✅ **Zero matches** in your project files (excluding node_modules, .next, etc.)

## 📚 Resources

- **FT Design System Docs**: [Internal link]
- **Token Reference**: `node_modules/ft-design-system/dist/types/tokens/design-tokens.d.ts`
- **Icon Library**: `node_modules/ft-design-system/dist/types/components/atoms/Icons/`
- **Component Examples**: `/app/(routes)/components/page.tsx`

## ⚠️ Enforcement

### ESLint Rule (Recommended)
```json
{
  "rules": {
    "no-restricted-syntax": [
      "error",
      {
        "selector": "Literal[value=/^\\d+px$/]",
        "message": "Use FT DS spacing tokens instead of hardcoded px values"
      },
      {
        "selector": "Literal[value=/^#[0-9a-fA-F]{6}$/]",
        "message": "Use FT DS color tokens instead of hardcoded hex colors"
      }
    ]
  }
}
```

### Pre-commit Hook
```bash
#!/bin/sh
# Check for hardcoded values before commit

if git diff --cached --name-only | grep -E '\.(tsx?|jsx?)$' | xargs grep -E ":\s*['\"]?\d+px"; then
  echo "❌ Hardcoded px values found! Use FT DS spacing tokens."
  exit 1
fi

if git diff --cached --name-only | grep -E '\.(tsx?|jsx?)$' | xargs grep -E "#[0-9a-fA-F]{6}"; then
  echo "❌ Hardcoded hex colors found! Use FT DS color tokens."
  exit 1
fi

echo "✅ Token check passed!"
```

## 🎯 Summary

**The Rule:** If it's a design value (color, spacing, typography, border, shadow), it MUST use an FT DS token.

**No Exceptions** for themeable design values.

**Result:** Consistent, maintainable, themeable codebase that scales.

---

**Updated:** December 12, 2025  
**Status:** Enforced project-wide  
**Compliance:** 100%

