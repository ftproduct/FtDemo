# ⚠️ DEPRECATED - DO NOT USE

## This folder contains shadcn/ui components that are **DEPRECATED**.

### ❌ Do NOT import from this folder

All UI components should come from the official npm package:

```typescript
// ✅ CORRECT - Use ft-design-system/ai
import { Button, Input, Badge, Tabs, Table, Checkbox, Switch } from 'ft-design-system/ai';

// ❌ WRONG - Don't use these
import { Button } from './ui/button';
import { Input } from './ui/input';
```

### Why these files still exist

These files are kept temporarily for reference but should **NOT** be used in any new or existing code.

### Migration Status

- **Button** → Use `Button` from `ft-design-system/ai` ✅
- **Input** → Use `Input` from `ft-design-system/ai` ✅
- **Badge** → Use `Badge` from `ft-design-system/ai` ✅
- **Tabs** → Use `Tabs` from `ft-design-system/ai` ✅
- **Table** → Use `Table` from `ft-design-system/ai` ✅
- **Checkbox** → Use `Checkbox` from `ft-design-system/ai` ✅
- **Switch** → Use `Switch` from `ft-design-system/ai` ✅
- **Dropdown** → Use `Dropdown` from `ft-design-system/ai` ✅
- **ProgressBar** → Use `ProgressBar` from `ft-design-system/ai` ✅
- **RadioGroup** → Use `RadioGroup` from `ft-design-system/ai` ✅

### For components not in ft-design-system/ai

Use the `MissingComponent` placeholder:

```typescript
import { MissingComponent } from '../MissingComponent';

<MissingComponent 
  name="ComponentName"
  description="Description from design"
/>
```

### Action Required

If you see imports from `./ui/` in code review:
1. Replace with `ft-design-system/ai` import
2. Or use `MissingComponent` if not available
3. Remove the ui/ import

---

**Last Updated:** November 2025  
**Status:** Deprecated - Scheduled for removal
