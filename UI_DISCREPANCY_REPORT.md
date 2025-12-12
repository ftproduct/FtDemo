# UI Discrepancy Report

Comparing the developed implementation (Screen 1) with the Figma design (Screen 2), the following discrepancies have been identified.

## 1. Table Component (High Priority)
- **Checkbox Column**: The checkbox column currently has zero width or is collapsed, making it invisible or unusable.
- **Actions Column**: The design explicitly shows **two** action buttons in the rightmost column:
    1. A "More Options" button (`...`).
    2. A "View Details" button (Right Chevron `>`) in a circular container.
    *Current Implementation*: Only shows the "More Options" button.
- **Vehicle Info Column**: The design shows a small chevron/arrow icon (`>`) next to the Transporter Name (e.g., "Yonex Transporter >").
    *Current Implementation*: Missing this icon.
- **Trip Info Column**: The design includes a green verified/check icon next to the Trip ID.
    *Current Implementation*: Includes a check icon, but verify alignment and spacing matches the design's compact layout.

## 2. Page Header & Controls (High Priority)
- **Uneven Gaps**: The spacing between the DatePicker, Dropdowns, and other controls in the Page Header is inconsistent/uneven.
- **Direction Dropdown Width**: The "Direction" dropdown width does not match the width of other dropdowns or inputs in the header row.
- **View Toggle Icons**: The icons used in the Segmented Tab (List vs. Map view) do not match the design.
- **Menu Icon Distortion**: The menu icon in the App Header appears squished or distorted.

## 3. Quick Filters / Stats Bar (Medium Priority)
- **Alert Counts Styling**: In the Figma design, the counts for "Long Stoppage", "Route Deviation", and "Delayed" are styled in **Red** to indicate critical status.
    *Current Implementation*: The counts are displayed in neutral grey/black colors.
- **Filter Group Styling**: The "Delayed" filter group in Figma appears to have specific highlighting (red text/background) for the active or critical state.
    *Current Implementation*: Uses standard button styling.

## 4. General Iconography (Medium Priority)
- **Icon Mismatch**: There is a general mismatch of icons across the screen compared to the design. Specific attention is needed for:
    - Page Title Icon (should look like a signal tower, currently a Home icon).
    - App Header icons.
    - Segmented Control icons.

## Recommended Next Steps
1.  **Fix Checkbox Width**: Ensure the first column in the table has a fixed, visible width.
2.  **Fix Header Layout**: Standardize gaps and widths for all inputs/dropdowns in the Page Header.
3.  **Update Icons**: Replace the Menu icon, View Toggle icons, and Page Title icon with correct assets from the design system or icon library.
4.  **Update Actions Column**: Add the missing "View Details" (Chevron Right) button to the table rows.
5.  **Style Quick Filters**: Update the text color of the counts in the Quick Filter bar to **Red** for alert-related filters.
