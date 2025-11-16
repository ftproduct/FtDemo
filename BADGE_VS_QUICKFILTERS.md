# Badge vs QuickFilters - When to Use Which

## 🎯 Quick Decision Guide

**Looking at your design, ask:**
- ❓ Can users click it to filter data? → **Use QuickFilters**
- ❓ Just showing status/label? → **Use Badge**

## 🔴 QuickFilters (Interactive)

### When to Use
- ✅ Filtering data/lists
- ✅ Shows counts with filters
- ✅ Users can click to toggle
- ✅ Can be selected/unselected
- ✅ Interactive filtering UI

### Visual Examples
```
19 Long Stoppage    ← Clickable, with count, red background
28 0-6 hrs          ← Clickable, with count, orange background
28 Expiring in 3 hrs ✓  ← Selected state with checkmark
E Way bill          ← Clickable, no count
```

### Code Example
```typescript
import { QuickFilters } from 'ft-design-system/ai';

<QuickFilters 
  filters={[
    { id: 'stoppage', label: 'Long Stoppage', count: 19, type: 'alert' },
    { id: '0-6hrs', label: '0-6 hrs', count: 28, type: 'warning' },
    { id: '6hrs', label: '6 hrs', count: 28, type: 'success' },
    { id: 'eway', label: 'E Way bill', type: 'normal' },
  ]}
  onFilterClick={(filterId) => handleFilterClick(filterId)}
  onFilterRemove={(filterId) => handleFilterRemove(filterId)}
/>
```

### Available Types
- `'alert'` - Red background (critical items)
- `'warning'` - Orange background (attention needed)
- `'success'` - Green background (good status)
- `'normal'` - Gray/default background
- `'neutral'` - Blue background (info)

## 🏷️ Badge (Display Only)

### When to Use
- ✅ Status labels in tables/cards
- ✅ Non-interactive indicators
- ✅ Simple status display
- ✅ No counting needed
- ✅ Pure visual indicator

### Visual Examples
```
Pending    ← Just a label, non-clickable
Completed  ← Status indicator
Warning    ← Alert indicator
```

### Code Example
```typescript
import { Badge } from 'ft-design-system/ai';

<Badge variant="normal">Pending</Badge>
<Badge variant="warning">Warning</Badge>
<Badge variant="danger">Critical</Badge>
<Badge variant="success">Completed</Badge>
```

### Available Variants
- `'normal'` - Default gray badge
- `'neutral'` - Blue badge (info)
- `'warning'` - Orange badge (warning)
- `'danger'` - Red badge (error/critical)
- `'success'` - Green badge (success)

## 📊 Side-by-Side Comparison

| Feature | QuickFilters | Badge |
|---------|-------------|-------|
| **Interactive** | ✅ Yes - Clickable | ❌ No - Display only |
| **Shows Counts** | ✅ Yes - Optional | ❌ No |
| **Selection State** | ✅ Yes - Can be selected | ❌ No |
| **Use Case** | Filtering data | Showing status |
| **Event Handlers** | `onFilterClick`, `onFilterRemove` | None |
| **In Your Design** | Top filter bar | Status in table cells |

## 🎨 Real-World Examples

### ✅ Use QuickFilters For:
```typescript
// Journey status filters (your screenshot)
<QuickFilters filters={[
  { id: 'stoppage', label: 'Long Stoppage', count: 19, type: 'alert' },
  { id: 'deviation', label: 'Route Deviation', count: 19, type: 'alert' },
  { id: 'delayed', label: 'Delayed', count: 51, type: 'normal' },
  { id: '0-6hrs', label: '0-6 hrs', count: 28, type: 'warning' },
]} />

// Payment status filters
<QuickFilters filters={[
  { id: 'pending', label: 'Pending', count: 45, type: 'warning' },
  { id: 'paid', label: 'Paid', count: 120, type: 'success' },
]} />
```

### ✅ Use Badge For:
```typescript
// Status in a table row
<TableCell>
  <Badge variant="warning">Delayed</Badge>
</TableCell>

// Status in a card
<Card>
  <h3>Journey #12345</h3>
  <Badge variant="success">Completed</Badge>
</Card>

// Priority indicator
<Badge variant="danger">Urgent</Badge>
```

## 🚨 Common Mistakes

### ❌ WRONG - Using Badge for Filters
```typescript
// Don't use badges for clickable filters
{filters.map(f => (
  <Badge variant="warning" onClick={...}>{f.label} {f.count}</Badge>
))}
```

### ✅ CORRECT - Use QuickFilters
```typescript
<QuickFilters 
  filters={filters}
  onFilterClick={handleClick}
/>
```

### ❌ WRONG - Using QuickFilters for Status Display
```typescript
// Don't use QuickFilters just to show status
<QuickFilters filters={[
  { id: 'status', label: 'Completed', type: 'success' }
]} />
```

### ✅ CORRECT - Use Badge
```typescript
<Badge variant="success">Completed</Badge>
```

## 📝 Implementation Checklist

When implementing filter UI from Figma:

- [ ] Are filters clickable/interactive? → Use **QuickFilters**
- [ ] Do filters show counts? → Use **QuickFilters**
- [ ] Can filters be selected/toggled? → Use **QuickFilters**
- [ ] Just showing status in table/card? → Use **Badge**
- [ ] No interaction needed? → Use **Badge**

## 💡 Pro Tips

1. **QuickFilters automatically handle:**
   - Hover states
   - Selected states (checkmark)
   - Count display
   - Click interactions

2. **Badge is simpler:**
   - Just pass variant and children
   - No event handlers
   - Pure display component

3. **Type Mapping:**
   - QuickFilters `type: 'alert'` = Badge `variant: 'danger'`
   - QuickFilters `type: 'warning'` = Badge `variant: 'warning'`
   - QuickFilters `type: 'success'` = Badge `variant: 'success'`
   - QuickFilters `type: 'normal'` = Badge `variant: 'normal'`
   - QuickFilters `type: 'neutral'` = Badge `variant: 'neutral'`

---

**Remember:** If your Figma design shows filters with counts like "19 Long Stoppage" or "28 0-6 hrs", that's **QuickFilters**, not Badge!

