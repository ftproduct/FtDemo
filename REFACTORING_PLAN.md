# MyJourneys.tsx Refactoring Plan

## Current Issues

### 1. **File Size** (1,476 lines)
- Too large for a single component
- Hard to navigate and maintain
- Violates Single Responsibility Principle

### 2. **Inline Styles Everywhere**
- Hundreds of inline style objects
- Repeated style patterns
- Hard to maintain consistency
- Poor performance (style objects recreated on every render)

### 3. **Duplicate Code**
- `getTripIcon` function defined twice (lines 551 and 819)
- Similar rendering patterns repeated across table columns
- Repeated icon configurations

### 4. **Mixed Concerns**
- Business logic (filtering, calculations)
- Presentation logic (rendering)
- Styling (inline styles and `<style>` blocks)
- State management
All in one file!

### 5. **Complex State Management**
- 11 separate `useState` hooks
- Could benefit from `useReducer` or context
- State updates scattered throughout

### 6. **Large Embedded CSS**
- 84 lines of CSS in `<style>` block
- Should be extracted to CSS module or styled component

### 7. **Table Column Definitions**
- 350+ lines just for column definitions
- Each column has complex inline JSX
- Hard to read and modify

## Proposed Refactoring Strategy

### Phase 1: Extract Utilities & Constants
**Priority: High**
```
src/
  components/
    pages/
      MyJourneys/
        index.tsx              (Main component - 300 lines max)
        constants.ts           (Tab configs, filter configs)
        utils.ts               (Helper functions)
        hooks/
          useJourneyFilters.ts (Filter logic)
          useJourneyTabs.ts    (Tab overflow logic)
        styles.module.css      (Extract inline styles)
```

**Benefits:**
- Reduce main file to ~300 lines
- Reusable utilities
- Easier testing

### Phase 2: Extract Table Components
**Priority: High**
```
src/
  components/
    pages/
      MyJourneys/
        components/
          JourneysTable/
            index.tsx
            columns.tsx        (Column definitions)
            cells/
              SelectCell.tsx
              FeedIdCell.tsx
              LocationCell.tsx
              VehicleCell.tsx
              TripCell.tsx
              StatusCell.tsx
              SLACell.tsx
              AlertCell.tsx
              ActionsCell.tsx
            JourneysTable.module.css
```

**Benefits:**
- Each cell component ~50 lines
- Easier to test individual cells
- Cleaner separation of concerns
- Reusable cell components

### Phase 3: Extract Card View
**Priority: Medium**
```
src/
  components/
    pages/
      MyJourneys/
        components/
          JourneyCard/
            index.tsx
            JourneyCard.module.css
```

**Benefits:**
- Separate mobile/card view logic
- Easier to maintain
- Better code organization

### Phase 4: Create Shared UI Components
**Priority: Medium**
```
src/
  components/
    shared/
      StatusBadge/
        index.tsx            (Reusable status badges)
      TripIcon/
        index.tsx            (SIM, GPS, Fastag icons)
      LocationInfo/
        index.tsx            (From/To location display)
```

**Benefits:**
- Reusable across other pages
- Consistent UI patterns
- Single source of truth

### Phase 5: CSS Modules / Styled Components
**Priority: Low (but important)**

Replace inline styles with:
```css
/* JourneysTable.module.css */
.tableWrapper {
  overflow-x: auto;
  width: 100%;
  position: relative;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-primary);
  background-color: var(--bg-primary);
}

.selectCell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  width: 100%;
  padding: 0 12px;
}
```

**Benefits:**
- Better performance
- Easier to maintain
- Standard CSS tooling
- Type safety with CSS modules

## Recommended Implementation Order

### Week 1: Quick Wins
1. ✅ Extract constants (tabs, filters)
2. ✅ Extract utility functions
3. ✅ Remove duplicate `getTripIcon`
4. ✅ Create `useJourneyFilters` hook

**Result:** Main file reduced from 1,476 to ~800 lines

### Week 2: Table Refactor
1. ✅ Create table cell components
2. ✅ Extract column definitions
3. ✅ Create `JourneysTable` component

**Result:** Main file reduced to ~400 lines

### Week 3: Final Polish
1. ✅ Extract card view component
2. ✅ Create CSS modules
3. ✅ Replace inline styles
4. ✅ Add unit tests

**Result:** Clean, maintainable codebase

## Quick Wins (Do These First!)

### 1. Extract Duplicate Function
```typescript
// src/components/pages/MyJourneys/utils/getTripIcon.tsx
import { Icon } from 'ft-design-system';

export const getTripIcon = (type: string) => {
  if (type === 'SIM') {
    return <Icon name="sim" style={{ width: '16px', height: '16px', color: 'var(--positive)' }} />;
  }
  if (type === 'GPS') {
    return <Icon name="gps" style={{ width: '20px', height: '20px', color: 'var(--neutral)' }} />;
  }
  if (type === 'Fastag') {
    return <div style={{ width: '16px', height: '16px', borderRadius: '2px', background: '#722ed1', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', color: 'white', fontWeight: 600 }}>F</div>;
  }
  return null;
};
```

### 2. Extract Tab Configuration
```typescript
// src/components/pages/MyJourneys/constants.ts
export const TAB_CONFIG = [
  { label: 'Planned', status: 'planned', icon: 'calendar' },
  { label: 'En Route to Loading', status: 'en_route_to_loading', icon: 'truck' },
  { label: 'At Loading', status: 'at_loading', icon: 'bundle' },
  { label: 'In Transit', status: 'in_transit', icon: 'location' },
  { label: 'At Unloading', status: 'at_unloading', icon: 'bundle' },
  { label: 'In Return', status: 'in_return', icon: 'refresh' },
  { label: 'Delivered', status: 'delivered', icon: 'check-fill' }
];
```

### 3. Extract Filters Hook
```typescript
// src/components/pages/MyJourneys/hooks/useJourneyFilters.ts
export const useJourneyFilters = (journeys: Journey[], activeFilters: Set<string>) => {
  return useMemo(() => {
    if (activeFilters.size === 0) return journeys;
    
    return journeys.filter((journey) => {
      for (const filterKey of activeFilters) {
        const [filterId, optionId] = filterKey.split(':');
        // Filter logic here...
      }
    });
  }, [journeys, activeFilters]);
};
```

## Metrics After Refactoring

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| File Size | 1,476 lines | ~250 lines | 83% reduction |
| Component Count | 1 | ~15 | Better separation |
| Inline Styles | ~200 | 0 | 100% removed |
| Duplicate Code | Yes | No | ✅ |
| Testability | Low | High | ✅ |
| Maintainability | Low | High | ✅ |

## Next Steps

**Would you like me to:**
1. Start with Quick Wins (extract utilities, constants, hooks)?
2. Go directly to table cell extraction?
3. Create a full example of one refactored component?

Let me know which approach you prefer, and I'll help you implement it step by step!
