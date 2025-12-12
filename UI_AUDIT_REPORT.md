# Detailed UI Discrepancy Audit: Journeys Page

This report outlines the visual discrepancies identified between the current implementation of the "Journeys" page and the provided Figma design (`uploaded_image_1763707292515.png`).

## 1. Design Tokens & Colors

### Colors
*   **Primary Text:** The design uses a slightly darker, sharper grey for primary text (e.g., "My Journeys", Table Headers). Current implementation seems slightly lighter (`#434f64`).
*   **Secondary Text:** The secondary text (e.g., "MDC Labs ltd", "Ambala, Haryana") in the design has a cooler tone.
*   **"On Time" Green:** The green used for "On time" status in the design is a vibrant, standard success green (approx `#00C853` or `#22C55E`). The current implementation uses `var(--positive)` which might need adjustment to match exactly.
*   **"Delayed" Red:** The red text for "Delayed by..." is a clear alert red.
*   **Link Color:** The "View ID's" link in the design is a standard link blue (approx `#1890FF` or `#2563EB`).
*   **Badge Backgrounds:**
    *   **Alert Badges (Long Stoppage, etc.):** The design shows a very light red/pink background with red text.
    *   **Neutral Badges (+1P, +3D):** These have a light grey background in the design.

### Typography
*   **Page Title:** "My Journeys" is **Bold** (700) and larger (approx 24px/28px).
*   **Table Headers:** The design uses a **Semi-Bold** (600) weight for table headers. Current implementation looks Regular or Medium.
*   **Table Data:**
    *   **Feed Unique ID:** Monospace or distinct font for the ID number? (Hard to tell, but looks standard sans-serif).
    *   **"Delayed by..."**: This text is **Bold** in the design.
    *   **"On time"**: This text is **Bold** or **Semi-Bold**.
    *   **Alert Time ("1 hour ago"):** This text is smaller (approx 12px) and lighter weight.

## 2. Spacing & Padding (Layout)

### Page Layout
*   **Header Spacing:** There is a specific gap between the "My Journeys" title row and the Filter Bar row.
*   **Section Gaps:** The vertical spacing between:
    *   Header -> Tabs
    *   Tabs -> Quick Filters
    *   Quick Filters -> Table
    *   ...needs to be consistent (approx `20px` or `24px`).

### Component Spacing
*   **Filter Bar:** The gap between the Dropdowns, DatePicker, Search, and "Add Journey" button is consistent (approx `16px`).
*   **Quick Filters:** The horizontal gap between filter chips is approx `12px`.
*   **Table Cells:** The padding inside table cells (top/bottom) seems generous (approx `16px` or `20px`) to allow for the two-line content structure.

## 3. Elements & Components

### Header / Filter Bar
*   **"Add Journey" Button:**
    *   **Color:** Solid dark grey/black background (`#1F2937` or similar).
    *   **Icon:** Includes a "plus" icon.
    *   **Radius:** Rounded corners (approx `6px`).
*   **Search Bar:**
    *   **Icon:** Search icon is on the **left**.
    *   **Placeholder:** "Search My Journeys".
*   **Dropdowns & DatePicker:**
    *   **Height:** All input elements in this row (Dropdowns, DatePicker, Search, Button) MUST have the **same height** (approx `40px` or `36px`).
    *   **Borders:** Light grey borders.

### Tabs
*   **Active State:** The active tab ("In Transit") has a **dark bottom border** (underline) and potentially darker text.
*   **Badges:**
    *   The badges (e.g., "56") are **contained** within a small, rounded rectangle (or pill shape) with a light background and dark text.
    *   They are positioned next to the label.

### Quick Filters
*   **Styling:** These are "chip" style filters.
*   **"Delayed" / "Expiring":** These chips have specific styling:
    *   **Delayed:** Light red background, Red text/border.
    *   **Expiring:** Light orange/yellow background?
    *   **Normal:** White background, grey border.
*   **Counts:** The counts are bolded within the chips.

### Data Table
*   **Header:**
    *   Background is a light grey (`#F9FAFB` or similar).
    *   Text is aligned **Left**.
*   **Rows:**
    *   **Separators:** Light grey horizontal lines between rows.
    *   **Hover:** Likely a subtle hover effect (not visible in static design but standard).
*   **Columns:**
    *   **Select:** Checkbox + Star icon.
    *   **Feed Unique ID:** ID on top, "View ID's" link below.
    *   **From / To:** Location on top, Company Name below.
        *   **Badges (+1P, +3D):** These are small, grey pills next to the location.
    *   **Vehicle Info:** Vehicle Number on top, Transporter Name below.
    *   **Trip Info:**
        *   **SIM:** Shows a specific "Signal" icon + "SIM" text.
        *   **GPS:** Shows a MapPin icon? Or a specific GPS device icon.
        *   **Fastag:** Shows a specific "F" icon/logo.
        *   **Trip ID:** Shown below with a checkmark icon.
    *   **Status:**
        *   **Top:** Icon + Status Text (e.g., "On Road").
        *   **Bottom:** Icon + Location/Time.
    *   **SLA:**
        *   **Top:** Icon (Check/Clock) + Status ("On time"/"Delayed...").
        *   **Bottom:** ETA text.
    *   **Alerts:**
        *   **Badge:** "Long Stoppage" etc. in a red pill badge.
        *   **Time:** "1 hour ago" text next to or below the badge.
    *   **Actions:** Three-dot menu button + Right arrow button (circular).

## 4. Icons & Visuals

*   **Trip Type Icons:**
    *   **SIM:** Needs a specific "Signal bars" icon.
    *   **Fastag:** Needs a specific "F" square icon.
*   **Status Icons:**
    *   **MapPin:** Used for location.
    *   **CheckCircle:** Used for "On time" and verified Trip ID.
    *   **Clock:** Used for "Delayed".
*   **Action Icons:**
    *   **Three Dots:** Inside a circular border?
    *   **Right Arrow:** Inside a circular border.

## 5. Organisms & Alignment

*   **Table Alignment:**
    *   All text in the table headers and body cells should be **Left Aligned**.
    *   Vertical alignment in cells should be **Top** or **Center** (looks like Top with some padding).
*   **Content Direction:**
    *   Most cells follow a **Vertical Stack** pattern: Primary Info (Top) -> Secondary Info (Bottom).
    *   "From" and "To" cells have a **Horizontal** flow for the Location + Badge.

## 6. Summary of Critical Fixes Needed

1.  **Header Heights:** Fix the mismatched heights of Dropdowns, Search, and Button in the filter bar.
2.  **Table Header Alignment:** Ensure "Feed Unique ID", "From", "To", etc., are strictly left-aligned.
3.  **Typography:** Bold the "My Journeys" title. Bold the "Delayed" and "On time" status text.
4.  **Colors:** Update the "View ID's" link blue and "On time" green to match the vibrant design colors.
5.  **Icons:** Update the "Trip Info" icons to match the specific SIM/Fastag visuals.
6.  **Badges:** Style the Quick Filter chips (Delayed/Expiring) with their specific color themes.
