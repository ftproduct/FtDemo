# FT Design System v4.15.20 - AI Rules

## Overview
- **Version:** 4.15.20
- **Components:** 124
- **Status:** ✅ All rules enforced project-wide

## IMPORTS

```tsx
// CSS (required in root layout)
import 'ft-design-system/styles';

// Components
import { Button, Input, Table, Badge } from 'ft-design-system';

// Provider (wrap your app)
import { FTProvider } from 'ft-design-system';
```

## FORBIDDEN PATTERNS ❌

Never generate these patterns:

| Pattern | Example | Why Forbidden |
|---------|---------|---------------|
| Arbitrary hex colors | `bg-[#434F64]`, `text-[#ff3532]` | Use semantic color classes |
| Arbitrary CSS vars | `bg-[var(--name)]`, `text-[var(--name)]` | Not type-safe |
| Dimension overrides | `h-[X]`, `w-[X]`, `rounded-[X]`, `p-[X]` | Use size props |
| CSS vars with underscore | `var(--some_token)` | Invalid token format |
| CSS vars with slash | `var(--some/token)` | Invalid token format |
| Hardcoded font sizes | `fontSize: '16px'` | Use rem-based classes |
| Inline style vars | `style={{ padding: 'var(--spacing-x4)' }}` | Use Tailwind classes |

## REQUIRED PATTERNS ✅

### Component Props

```tsx
// Always use size prop
<Button size="sm">Small</Button>
<Button size="md">Medium</Button>
<Button size="lg">Large</Button>

// Always use variant prop for styling
<Button variant="primary">Primary</Button>
<Button variant="secondary">Secondary</Button>
<Button variant="destructive">Delete</Button>
```

### Table API

```tsx
// Table rows MUST have 'id' field
const data = [
  { id: 1, name: 'John', email: 'john@example.com' },
  { id: 2, name: 'Jane', email: 'jane@example.com' },
];

// Table columns use 'title' NOT 'header'
const columns = [
  { key: 'name', title: 'Name' },      // ✅ Correct
  { key: 'email', title: 'Email' },    // ✅ Correct
  // { key: 'name', header: 'Name' },  // ❌ Wrong
];

<Table columns={columns} data={data} />
```

## COMPONENT API REFERENCE

### Button
```tsx
<Button 
  variant="primary|secondary|destructive|text|link|ghost|dashed"
  size="sm|md|lg"
  icon="icon-name"
  iconPosition="left|right|only"
  disabled={boolean}
>
  Label
</Button>
```

### Input
```tsx
<Input>
  <InputLabel>Label</InputLabel>
  <InputField 
    placeholder="Placeholder"
    size="sm|md|lg"
    error={boolean}
    helperText="Helper text"
    leadingIcon="search"
  />
</Input>
```

### Badge
```tsx
// IMPORTANT: Use 'danger' NOT 'error'
<Badge variant="primary|secondary|danger|success|warning|neutral">
  Label
</Badge>
```

### Modal
```tsx
<Modal open={isOpen} onOpenChange={setIsOpen}>
  <ModalTrigger asChild>
    <Button>Open Modal</Button>
  </ModalTrigger>
  <ModalContent>
    <ModalHeader>
      <ModalTitle>Title</ModalTitle>
      <ModalDescription>Description</ModalDescription>
    </ModalHeader>
    <ModalBody>Content</ModalBody>
    <ModalFooter>
      <Button variant="secondary" onClick={() => setIsOpen(false)}>Cancel</Button>
      <Button variant="primary" onClick={() => setIsOpen(false)}>Confirm</Button>
    </ModalFooter>
  </ModalContent>
</Modal>
```

## COLOR CLASSES

Use Tailwind utility classes:

| Color | Background | Text | Border |
|-------|------------|------|--------|
| Primary 700 | `bg-primary-700` | `text-primary-700` | `border-primary-700` |
| Neutral 100 | `bg-neutral-100` | `text-neutral-100` | `border-neutral-100` |
| Neutral 500 | `bg-neutral-500` | `text-neutral-500` | `border-neutral-500` |
| Critical | `bg-critical` | `text-critical` | `border-critical` |
| Positive | `bg-positive` | `text-positive` | `border-positive` |
| Warning 600 | `bg-warning-600` | `text-warning-600` | `border-warning-600` |

## TYPOGRAPHY CLASSES

Use rem-based classes:

| Class | Size |
|-------|------|
| `text-xs-rem` | 12px |
| `text-sm-rem` | 14px |
| `text-md-rem` | 16px |
| `text-lg-rem` | 20px |
| `text-xl-rem` | 24px |
| `text-xxl-rem` | 28px |

## SPACING (Tailwind)

Use standard Tailwind spacing:

| Class | Meaning |
|-------|---------|
| `p-1` | padding 4px |
| `p-2` | padding 8px |
| `p-3` | padding 12px |
| `p-4` | padding 16px |
| `p-5` | padding 20px |
| `p-6` | padding 24px |
| `gap-1` | gap 4px |
| `gap-2` | gap 8px |
| `gap-3` | gap 12px |
| `gap-4` | gap 16px |

## EXAMPLES

### ✅ Correct Usage

```tsx
// Button with proper props
<Button variant="primary" size="md">Save</Button>

// Input with proper structure
<Input>
  <InputLabel>Email</InputLabel>
  <InputField placeholder="Enter email" size="md" />
</Input>

// Table with proper columns and data
<Table 
  columns={[{ key: 'name', title: 'Name' }]} 
  data={[{ id: 1, name: 'John' }]} 
/>

// Badge with 'danger' variant
<Badge variant="danger">Error</Badge>

// Styling with Tailwind classes
<div className="p-4 bg-neutral-100 rounded-lg">
  <h1 className="text-xl-rem font-bold text-primary-700">Title</h1>
  <p className="text-sm-rem text-neutral-500">Description</p>
</div>
```

### ❌ Incorrect Usage

```tsx
// Don't use inline styles with CSS vars
<div style={{ padding: 'var(--spacing-x4)' }}>...</div>

// Don't use arbitrary values
<div className="bg-[#434F64] p-[16px]">...</div>

// Don't use 'error' variant for Badge
<Badge variant="error">Error</Badge>  // Use 'danger' instead

// Don't use 'header' in table columns
const columns = [{ key: 'name', header: 'Name' }];  // Use 'title'
```

---

**Last Updated:** December 12, 2025  
**FT Design System Version:** 4.15.20  
**Status:** ✅ Enforced Project-Wide
