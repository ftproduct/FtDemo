# FT Design System Showcase Page

## Overview
A comprehensive showcase page has been created at `/components` that displays all FT Design System components, colors, tokens, and design elements.

**Current Version:** FT Design System v4.15.17 (Updated: December 12, 2025)

## Location
- **Route:** `/components`
- **File:** `app/(routes)/components/page.tsx`
- **URL:** [http://localhost:3001/components](http://localhost:3001/components)

## Features

### 1. **Components Tab**
Displays all major component categories with live examples:

#### Form Components
- **Buttons:** Primary, Secondary, Tertiary, Danger variants with different sizes
- **Text Input:** Standard input fields with labels
- **Textarea:** Multiline text input
- **Select Dropdown:** Dropdown selection with multiple options
- **Checkboxes:** Checkbox options with labels
- **Radio Groups:** Radio button selections
- **Toggle Switch:** On/off switches
- **Slider:** Range slider controls

#### Badges & Tags
- Multiple badge variants: Primary, Secondary, Success, Warning, Danger, Info

#### Alerts
- Alert components with different severities: Info, Success, Warning, Error
- Includes titles and descriptions

#### Cards
- Card layouts with headers, bodies, and footers
- Multiple card examples with different content types

#### Avatars
- Avatar components in different sizes: Small, Medium, Large
- Fallback support for initials
- **Avatar Group:** Display multiple avatars with overflow count (+2 more)

#### Progress Indicators
- Progress bars with different completion states
- Status variants: Default, Success, Warning
- Spinning loaders in multiple sizes

#### Skeleton Loaders
- Loading placeholders for content
- Text skeleton with multiple lines

#### Modals & Drawers
- Modal dialogs with headers, bodies, and footers
- Side drawer panels
- Interactive triggers

#### Tables
- Data tables with headers and rows
- Column support with different data types
- Action buttons in cells
- Badge status indicators

#### Timeline
- Event timelines with colored dots
- Content sections for each event
- Visual progression display

#### Steps
- Step-by-step progress indicators
- Current step highlighting
- Step titles and descriptions

#### Breadcrumbs
- Navigation breadcrumb trails
- Hierarchical page structure

#### Pagination
- Page navigation controls
- Configurable page sizes

#### Empty States
- No data placeholders
- Customizable messages

#### Result Pages
- Success, Error, and Warning result displays
- Status-specific messaging

#### Statistics (NEW in v4.15.17)
- Display numerical data with titles and values
- Perfect for dashboards and KPIs
- Composable `Statistic`, `StatisticTitle`, `StatisticValue`
- Examples: Total Revenue, Active Users, Growth Rate

#### Tooltips (NEW in v4.15.17)
- Contextual information on hover
- Multiple placement options
- Requires `TooltipProvider` wrapper
- Interactive examples with buttons

#### Collapsible (NEW in v4.15.17)
- Expandable content sections
- Toggle visibility with smooth animations
- Composable trigger and content components
- Great for FAQs and detailed information

#### Descriptions (NEW in v4.15.17)
- Key-value pair data display
- Clean, structured layout for details
- Perfect for user profiles, settings, invoices
- Supports rich content (badges, links, etc.)

#### Lists (NEW in v4.15.17)
- Structured list components
- Rich content support with `ListItemContent`
- Flexible styling for different use cases
- Examples with titles and descriptions

#### Navigation Enhancements (NEW in v4.15.17)
- **Anchor:** Jump to specific page sections
- **BackTop:** Scroll to top button for long pages

#### Version Information (NEW)
- Displays current FT Design System version
- Technology stack badges (React 18, TypeScript, Tailwind CSS)
- Clear version tracking for developers

### 2. **Colors Tab**
Comprehensive color palette showcase:

#### Color Groups
- **Primary Colors:** 9 shades (100-900)
- **Secondary Colors:** 9 shades (100-900)
- **Tertiary Colors:** 9 shades (100-900)
- **Neutral Colors:** 9 shades (100-900)
- **Positive Colors:** 9 shades (100-900)
- **Warning Colors:** 9 shades (100-900)
- **Danger Colors:** 9 shades (100-900)

Each color displays:
- Visual color swatch
- Shade number
- Hex color value

#### Semantic Colors
- Primary Text
- Secondary Text
- Tertiary Text
- Primary Background
- Secondary Background
- Border Primary
- Border Secondary
- Success
- Warning
- Error
- Info

All semantic colors show:
- Color preview
- CSS variable name
- Usage context

### 3. **Design Tokens Tab**
Design system tokens and values:

#### Border Radius
- None, Small, Medium, Large, XL, Full
- Visual examples with CSS variable names

#### Shadows
- None, Small, Medium, Large, XL
- Box shadow examples with depth

#### Z-Index
- Stacking order tokens for different UI layers
- Base, Dropdown, Sticky, Fixed, Modal Backdrop, Modal, Popover, Tooltip

#### Transitions
- Animation timing values
- Fast, Base, Slow, Slower durations

### 4. **Typography Tab**
Complete typography system:

#### Font Sizes
- XS, SM, Base, LG, XL, 2XL, 3XL, 4XL
- Visual examples with "The quick brown fox" sample text
- CSS variable names and pixel values

#### Font Weights
- Light (300)
- Normal (400)
- Medium (500)
- Semibold (600)
- Bold (700)
- Visual weight comparisons

#### Line Heights
- Tight (1.25)
- Normal (1.5)
- Relaxed (1.75)
- Loose (2)
- Visual spacing examples

#### Font Families
- Primary font stack
- Secondary font stack
- Monospace font stack

### 5. **Spacing Tab**
Spacing system and scale:

#### Spacing Scale
- x1 (4px) through x16 (64px)
- Visual size representations
- CSS variable names

#### Spacing in Practice
- Real-world layout examples
- Card with proper spacing
- Button groups with gaps

## Navigation

The page is accessible through:
1. **Sidebar Navigation** - "Components" link in the main sidebar
2. **Direct URL** - Navigate to `/components`
3. **From Dashboard** - Click "Components" in the navigation menu

## Design

The page follows the FT Design System guidelines:
- Consistent spacing using design tokens
- Proper color contrast and accessibility
- Responsive layout
- Clean, organized sections with cards
- Tab-based navigation for different categories
- Interactive components with live examples

## Technical Implementation

### Key Technologies
- **React 18** with hooks
- **Next.js 14** App Router
- **FT Design System v4.15.14**
- **TypeScript** for type safety
- **CSS Variables** from FT DS

### Component Structure
```
ComponentsShowcasePage (Main component)
├── Tabs Navigation
│   ├── Components Tab → ComponentsShowcase()
│   ├── Colors Tab → ColorsShowcase()
│   ├── Tokens Tab → TokensShowcase()
│   ├── Typography Tab → TypographyShowcase()
│   └── Spacing Tab → SpacingShowcase()
└── ShowcaseSection (Reusable section wrapper)
```

### Reusable Components
- `ShowcaseSection`: Wrapper component for consistent section styling
  - Takes title, description, and children props
  - Provides card-based layout

## Usage

To view the showcase:

1. Ensure the development server is running:
   ```bash
   npm run dev
   ```

2. Open your browser and navigate to:
   ```
   http://localhost:3001/components
   ```

3. Use the tabs to explore different sections:
   - **Components**: See all UI components in action
   - **Colors**: Browse the complete color palette
   - **Tokens**: View design tokens and values
   - **Typography**: Explore typography scale
   - **Spacing**: Understand spacing system

## Customization

The showcase is designed to be easily expandable:

1. **Add New Components**: Import from FT DS and add to appropriate section
2. **Add New Sections**: Create new `ShowcaseSection` components
3. **Add New Tabs**: Add new tab content and corresponding showcase function
4. **Modify Examples**: Edit component props to show different variations

## Benefits

This showcase page serves multiple purposes:

1. **Documentation**: Visual reference for all available components
2. **Development**: Quick access to component examples
3. **Design Review**: See all design tokens and colors in one place
4. **Testing**: Verify component implementations
5. **Onboarding**: Help new developers understand the design system
6. **QA**: Visual regression testing reference

## Next Steps

Potential enhancements:
- Add code snippets for each component example
- Include component API documentation
- Add search functionality
- Export design tokens as JSON
- Add theme switcher to preview dark mode
- Include accessibility notes for each component
- Add interactive component playground
- Include usage guidelines and best practices
