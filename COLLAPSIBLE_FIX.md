# Collapsible Component Fix - Using Actual FT DS Components

## Issue
The Collapsible component was incorrectly wrapping components in Button elements instead of using the proper FT Design System composable API.

## Date
December 12, 2025

## Problem

### Before (Incorrect):
```tsx
<Collapsible>
  <CollapsibleTrigger asChild>
    <Button variant="default" style={{ width: '100%', justifyContent: 'space-between' }}>
      <CollapsibleHeader>
        <CollapsibleTitle>Click to expand</CollapsibleTitle>
      </CollapsibleHeader>
    </Button>
  </CollapsibleTrigger>
  <CollapsibleContent>
    <div style={{ padding: 'var(--spacing-x4)', border: '1px solid var(--color-border-primary)', borderRadius: 'var(--border-radius-md)', marginTop: 'var(--spacing-x2)' }}>
      <p>Hidden content revealed on click.</p>
    </div>
  </CollapsibleContent>
</Collapsible>
```

**Issues:**
- ❌ Unnecessarily wrapping in Button component
- ❌ Not using FT DS's built-in trigger styling
- ❌ Missing CollapsibleIcon for visual indicator
- ❌ Not showcasing different Collapsible types
- ❌ Custom styling instead of using FT DS variants

## Solution

### After (Correct):
```tsx
<Collapsible type="Primary" bg="Secondary">
  <CollapsibleTrigger>
    <CollapsibleHeader>
      <CollapsibleIcon />
      <CollapsibleTitle>Primary Collapsible</CollapsibleTitle>
    </CollapsibleHeader>
  </CollapsibleTrigger>
  <CollapsibleContent>
    <p style={{ padding: 'var(--spacing-x4)' }}>
      This is the content that appears when expanded.
    </p>
  </CollapsibleContent>
</Collapsible>
```

**Improvements:**
- ✅ Uses FT DS's native CollapsibleTrigger (no Button wrapper)
- ✅ Includes CollapsibleIcon for expand/collapse indicator
- ✅ Uses type prop for styling variants (Primary, Secondary, Tertiary)
- ✅ Uses bg prop for background variants
- ✅ Follows FT DS composable API pattern
- ✅ Minimal custom styling

## Complete Updated Showcase

The showcase now displays three Collapsible variants:

### 1. Primary Collapsible
```tsx
<Collapsible type="Primary" bg="Secondary">
  <CollapsibleTrigger>
    <CollapsibleHeader>
      <CollapsibleIcon />
      <CollapsibleTitle>Primary Collapsible</CollapsibleTitle>
    </CollapsibleHeader>
  </CollapsibleTrigger>
  <CollapsibleContent>
    <p style={{ padding: 'var(--spacing-x4)' }}>
      Content with Primary type and Secondary background.
    </p>
  </CollapsibleContent>
</Collapsible>
```

### 2. Secondary Collapsible with Badge
```tsx
<Collapsible type="Secondary" bg="Primary">
  <CollapsibleTrigger>
    <CollapsibleHeader>
      <CollapsibleIcon />
      <CollapsibleTitle>Secondary Collapsible</CollapsibleTitle>
      <CollapsibleExtra>
        <Badge variant="info">New</Badge>
      </CollapsibleExtra>
    </CollapsibleHeader>
  </CollapsibleTrigger>
  <CollapsibleContent>
    <p style={{ padding: 'var(--spacing-x4)' }}>
      Collapsible with badge using CollapsibleExtra.
    </p>
  </CollapsibleContent>
</Collapsible>
```

**Features:**
- Uses `CollapsibleExtra` component for additional header content
- Shows how to add badges or action buttons in the header
- Demonstrates Secondary type variant

### 3. Tertiary Collapsible with Nested Components
```tsx
<Collapsible type="Tertiary">
  <CollapsibleTrigger>
    <CollapsibleHeader>
      <CollapsibleIcon />
      <CollapsibleTitle>Tertiary Collapsible</CollapsibleTitle>
    </CollapsibleHeader>
  </CollapsibleTrigger>
  <CollapsibleContent>
    <div style={{ padding: 'var(--spacing-x4)' }}>
      <List>
        <ListItem>
          <ListItemContent>Item 1 inside collapsible</ListItemContent>
        </ListItem>
        <ListItem>
          <ListItemContent>Item 2 inside collapsible</ListItemContent>
        </ListItem>
      </List>
    </div>
  </CollapsibleContent>
</Collapsible>
```

**Features:**
- Shows Tertiary type variant
- Demonstrates nesting other FT DS components (List) inside
- Real-world usage pattern

## FT DS Collapsible API

### Composable Components

#### **Collapsible** (Root)
```typescript
<Collapsible 
  type?: "Primary" | "Secondary" | "Tertiary"
  bg?: "Primary" | "Secondary"
  defaultOpen?: boolean
  open?: boolean
  onOpenChange?: (open: boolean) => void
>
```

#### **CollapsibleTrigger**
- Clickable area that toggles the collapsible
- Renders as a button element
- No need to wrap in a separate Button component

#### **CollapsibleHeader**
- Container for header content
- Typically contains Icon, Title, and Extra

#### **CollapsibleIcon**
- Animated chevron/arrow icon
- Automatically rotates on expand/collapse
- Positioned on the left by default

#### **CollapsibleTitle**
- Main header text
- Semantic heading element

#### **CollapsibleExtra**
- Optional header content (right side)
- For badges, buttons, or other elements

#### **CollapsibleContent**
- Expandable content area
- Animated collapse/expand
- Hidden when closed

## Key Learnings

### ✅ Do's
1. **Use composable API** - It's the recommended pattern
2. **Include CollapsibleIcon** - Provides visual feedback
3. **Use type prop** - For styling variants (Primary, Secondary, Tertiary)
4. **Use bg prop** - For background variants
5. **Leverage CollapsibleExtra** - For badges, actions, or metadata

### ❌ Don'ts
1. **Don't wrap in Button** - CollapsibleTrigger IS the button
2. **Don't use asChild with Button** - Not needed for Collapsible
3. **Don't add custom styles** - Use FT DS props instead
4. **Don't use deprecated declarative API** - Use composable API

## Other Components Verified

### ✅ Correct Usage:

#### **Modal with Button Trigger**
```tsx
<Modal open={isOpen} onOpenChange={setIsOpen}>
  <ModalTrigger asChild>
    <Button variant="primary">Open Modal</Button>
  </ModalTrigger>
  <ModalContent>
    {/* ... */}
  </ModalContent>
</Modal>
```
**Note:** Modal correctly uses `asChild` because ModalTrigger can wrap any element.

#### **Drawer with Button Trigger**
```tsx
<Drawer open={isOpen} onOpenChange={setIsOpen}>
  <DrawerTrigger asChild>
    <Button variant="secondary">Open Drawer</Button>
  </DrawerTrigger>
  <DrawerContent>
    {/* ... */}
  </DrawerContent>
</Drawer>
```
**Note:** Drawer correctly uses `asChild` for custom trigger elements.

#### **Tooltip with Button Trigger**
```tsx
<Tooltip>
  <TooltipTrigger asChild>
    <Button>Hover Me</Button>
  </TooltipTrigger>
  <TooltipContent>
    <p>Helpful message</p>
  </TooltipContent>
</Tooltip>
```
**Note:** Tooltip correctly uses `asChild` to attach to any element.

## Understanding `asChild`

### When to Use `asChild`

The `asChild` prop is part of Radix UI's Slot pattern, used when:
- A trigger/wrapper component needs to merge props with a child element
- You want to use a custom element as the trigger
- The component is designed to wrap other interactive elements

### When NOT to Use `asChild`

Don't use `asChild` when:
- The component already IS the intended element (like CollapsibleTrigger)
- The component has its own built-in styling and behavior
- Wrapping would create redundant elements

## Summary

### Changes Made:
1. ✅ Removed Button wrapper from Collapsible
2. ✅ Added CollapsibleIcon for visual indicator
3. ✅ Added CollapsibleExtra import
4. ✅ Showcased three Collapsible variants (Primary, Secondary, Tertiary)
5. ✅ Demonstrated type and bg props
6. ✅ Showed CollapsibleExtra usage with Badge
7. ✅ Showed nested components (List) inside Collapsible

### Result:
- Proper FT DS component usage
- Clean, idiomatic code
- Showcases actual component capabilities
- No unnecessary wrappers
- Follows design system patterns

### Files Modified:
- `app/(routes)/components/page.tsx`
  - Fixed Collapsible implementation
  - Added CollapsibleIcon and CollapsibleExtra imports
  - Replaced single incorrect example with three correct examples

---

**Status:** ✅ Fixed and Verified
**Compilation:** ✅ No errors
**FT Design System Version:** 4.15.17
**Updated:** December 12, 2025

