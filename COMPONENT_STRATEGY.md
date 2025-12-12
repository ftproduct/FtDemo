# Component Strategy - Single Source of Truth

## 🎯 Overview

This project uses **ONLY** components from the official npm package `ft-design-system/ai`. No custom components, no shadcn/ui duplicates.

## ✅ What Was Implemented

### 1. **MissingComponent Placeholder** ✨
- **Location:** `/src/components/MissingComponent.tsx`
- **Purpose:** Display placeholder for components not in the npm package
- **Design:** Matches design system aesthetics with dashed border, icon, and clear messaging

### 2. **Cursor Rules Created**
- **`designsystem.mdc`** - Always use ft-design-system/ai components
- **`missing-components.mdc`** - Use MissingComponent for unavailable components

### 3. **CSS Load Order Fixed**
- Package CSS loads first: `import "ft-design-system/index.css"`
- Custom CSS loads last: `import "./index.css"`
- Result: Custom CSS variables properly override package defaults

### 4. **Duplicate Components Removed**
- ❌ Deleted: `FTButton.tsx`, `FTInput.tsx`, `FTTab.tsx`
- ❌ Removed: Duplicate Tabs component from ComponentGallery
- ✅ Updated: `ui/` folder marked as deprecated with clear README

## 📦 Available Components (from npm)

All from `ft-design-system/ai`:

- **Forms:** Button, Input, Checkbox, RadioGroup, Switch, Dropdown
- **Display:** Badge, QuickFilters, Table, Tabs, ProgressBar, Card
- **Typography:** Text, SubText, Statistic
- **Layout:** DisplayBlock, NavigationMenu

### Key Distinction: Badge vs QuickFilters

- **Badge** - Non-interactive status display (e.g., "Pending", "Completed")
- **QuickFilters** - Interactive filter chips with counts (e.g., "19 Long Stoppage", "28 0-6 hrs")

**Use QuickFilters when:**
- Users need to filter/toggle data
- Showing counts with filters
- Interactive selection needed

**Use Badge when:**
- Just displaying status
- No interaction required
- Simple label indicators

## 🚫 When Component is Missing

**DO NOT build a custom component.** Instead:

```typescript
import { MissingComponent } from '../components/MissingComponent';

<MissingComponent 
  name="ComponentName"
  description="What the component should do"
/>
```

### Example Usage

```typescript
// ✅ CORRECT - Component not available
<MissingComponent 
  name="Breadcrumb"
  description="Component for showing navigation path"
/>

// ❌ WRONG - Don't build custom
const CustomBreadcrumb = () => { /* ... */ };

// ❌ WRONG - Don't use shadcn/ui
import { Breadcrumb } from './ui/breadcrumb';
```

## 📋 Component Checklist

Before using any component:

- [ ] Check if it exists in `ft-design-system/ai`
- [ ] If YES: Import and use from npm package
- [ ] If NO: Use `MissingComponent` placeholder
- [ ] NEVER import from `./ui/` folder
- [ ] NEVER build custom implementations

## 🎨 Styling Guidelines

### Use CSS Variables (Always)

```typescript
// ✅ CORRECT
<div style={{
  color: 'var(--primary)',
  backgroundColor: 'var(--bg-primary)',
  padding: 'var(--space-4)',
  borderRadius: 'var(--radius-md)'
}}>
```

### Never Use Hardcoded Values

```typescript
// ❌ WRONG
<div style={{
  color: '#434f64',
  backgroundColor: '#ffffff',
  padding: '16px',
  borderRadius: '8px'
}}>
```

## 🔍 Benefits

1. **Single Source of Truth:** One package, one design system
2. **No Duplicates:** Clean codebase without redundant code
3. **Clear Communication:** Designers see what's missing via placeholders
4. **Easy Updates:** Update one npm package, everything updates
5. **Consistent Styling:** All components use same CSS variables

## 📊 Current Status

### Removed Components ✅
- Custom FTButton (deleted)
- Custom FTInput (deleted)
- Custom FTTab (deleted)
- Duplicate Tabs component (removed from gallery)

### Deprecated ⚠️
- All components in `src/components/ui/` folder
- Do not use - kept for reference only

### Active Components ✅
- All imports from `ft-design-system/ai`
- MissingComponent placeholder
- Examples in ComponentGallery showing proper usage

## 🚀 Moving Forward

### When Adding New Features

1. **Check Figma Design:** Identify required components
2. **Verify Availability:** Is it in ft-design-system/ai?
3. **Use Appropriate Solution:**
   - Available? Use npm package
   - Not available? Use MissingComponent
4. **Document:** Add to ComponentGallery if new

### When Updating Designs

1. **Review Components:** Check all components used
2. **Validate Sources:** Ensure all from npm package
3. **Replace Placeholders:** When new components added to package
4. **Test Styling:** Verify CSS variables are working

## 📞 Getting Help

### Component Request Process

If you need a component not in the package:

1. **Document the need:** Use MissingComponent with description
2. **Share screenshot:** Show placeholder to design team
3. **Request addition:** Ask design system team to add to package
4. **Wait for update:** Don't build custom - use placeholder
5. **Replace placeholder:** Once available in npm package

## 🎯 Success Metrics

- ✅ Zero custom component implementations
- ✅ All components from single source (npm package)
- ✅ Clear placeholders for missing components
- ✅ Consistent styling across all components
- ✅ Easy to update (just update npm package version)

---

**Last Updated:** November 15, 2025  
**Package Version:** ft-design-system@4.11.1  
**Status:** Active - All guidelines enforced via Cursor rules

