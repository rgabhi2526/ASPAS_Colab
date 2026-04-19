# ASPAS Design System

**ASPAS** — Auto Parts Stock and Analysis System  
A backend management platform for auto parts inventory, vendor relationships, order processing, and sales analytics. Indian market context (₹ currency). Active development, v1.0.0.

---

## Sources

| Resource | Location |
|---|---|
| Backend codebase | https://github.com/rgabhi2526/ASPAS_Colab |
| Frontend | None (no frontend exists — this design system is the first UI definition) |

> No Figma files or design assets were provided. All visual decisions in this design system are original, derived from the product domain, data model, and user needs of an auto parts inventory manager.

---

## Products

**ASPAS Web Dashboard** — A single-page admin dashboard for store/warehouse staff and managers. Core surfaces:
- **Inventory** — Browse, search, add, update spare parts. Alert for below-threshold stock.
- **Orders** — Create and track purchase orders to vendors.
- **Sales** — Record and review sales transactions.
- **Vendors** — Manage vendor directory and relationships.
- **Reports** — Daily revenue, monthly trend graphs, JIT threshold summaries.

---

## CONTENT FUNDAMENTALS

### Voice & Tone
- **Direct and functional.** This is a tool used by warehouse staff and managers, not a consumer app.
- **No fluff.** Labels are concise: "Add Part", "Update Stock", "Generate Report".
- **Indian market English.** Standard business English with ₹ for currency. No casual slang.
- **Data-first.** Numbers, part numbers, and quantities are always prominent. Copy steps back.
- **No emoji.** The interface is professional and technical. Emoji are never used.

### Casing
- **Title Case** for navigation labels and page headings: "Spare Parts", "Vendor Directory"
- **Sentence case** for form labels and body copy: "Enter part number", "Stock updated successfully."
- **ALL CAPS** for status badges and category tags: `LOW STOCK`, `ACTIVE`, `MEDIUM`
- **SCREAMING_SNAKE_CASE** echoed from backend for part numbers: `SP-BRK-001`

### Specific Examples
- ✅ "25 units in stock — 10 below threshold" 
- ✅ "Reorder suggested: Timing Belt Kit (Rack #3)"
- ✅ "Order #4201 placed with Brembo SpA"
- ❌ "Uh oh! Looks like you're running low 😬"
- ❌ "Awesome! Stock updated! 🎉"

---

## VISUAL FOUNDATIONS

### Color
- **Base palette**: Deep navy (`#0F1C2E`) as primary surface/header. Warm white (`#F9FAFB`) as app background.
- **Accent**: Amber/orange (`#F59E0B`) — references automotive industry, warning signals, industrial equipment.
- **Semantic colors**: Green for in-stock/success, red for critical/error, amber for warnings/low-stock, slate for neutral states.
- **No gradients** in UI chrome. Gradients only used sparingly for stat card highlights.

### Typography
- **Primary font**: DM Sans (Google Fonts) — clean, readable sans-serif, good for data-dense UIs.
- **Monospace font**: DM Mono — used for part numbers, codes, quantities, API endpoints.
- **Display sizes** (h1–h2): 32–24px, weight 600. Used only on page headers.
- **Body**: 14px/1.6, weight 400. Standard for table rows and form labels.
- **Small/meta**: 12px, weight 500, often uppercase+tracked.

### Spacing & Layout
- Base unit: 4px. Scale: 4, 8, 12, 16, 24, 32, 48, 64.
- Sidebar: 240px fixed. Content area: fluid. Max content width: 1280px.
- Section padding: 24px. Card padding: 16–24px.

### Cards & Surfaces
- Cards: white background, 1px `#E5E7EB` border, 8px border-radius, subtle shadow (`0 1px 3px rgba(0,0,0,0.08)`).
- No heavy box shadows. Elevation communicated by border, not shadow depth.
- Tables: no outer border, row separator only, alternating row tint on hover.

### Backgrounds
- App bg: `#F9FAFB` (warm off-white)
- Sidebar: `#0F1C2E` (deep navy)
- Card/panel: `#FFFFFF`
- Table header: `#F3F4F6`
- No background images, textures, or illustrations.

### Animation
- Minimal. Transitions at 150ms ease for hover states. No bounces or spring animations.
- Sidebar transitions: 200ms ease. Toast notifications: fade in 150ms, fade out 300ms.
- No animated loaders except a subtle skeleton shimmer for table rows.

### Hover & Press States
- Buttons: darken background by 8% on hover, scale(0.98) on press.
- Table rows: light amber tint (`#FFFBEB`) on hover.
- Nav items: white text opacity 0.7 → 1.0 on hover; left border indicator on active.
- Icon buttons: background opacity 0 → 0.08 on hover.

### Corner Radii
- Buttons: 6px. Cards: 8px. Badges: 4px. Modal: 12px. Input: 6px.

### Borders
- Standard: 1px solid `#E5E7EB`
- Focus ring: 2px solid `#F59E0B` (amber), offset 2px
- Sidebar active indicator: 3px left border amber

### Shadows
- Card: `0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.04)`
- Dropdown/modal: `0 8px 24px rgba(0,0,0,0.12)`
- No inner shadows.

### Transparency & Blur
- Modals: backdrop `rgba(0,0,0,0.4)`, no blur.
- Tooltips: solid background, no blur.

### Imagery
- No photographs. No illustrations. Data is the content.
- Icons are the primary visual decoration.

---

## ICONOGRAPHY

- **Icon set**: Lucide Icons (CDN) — `https://unpkg.com/lucide@latest/dist/umd/lucide.min.js`
- **Style**: 20px stroke icons, stroke-width 1.5, no fill. Consistent with the minimal, clean UI.
- **Color**: Inherit from parent text color. Accent amber for call-to-action icon buttons.
- **No emoji** used as icons anywhere.
- **No custom SVG illustrations** — placeholders preferred if imagery needed.

### Icon usage by context
| Context | Icon |
|---|---|
| Inventory / Parts | `package` |
| Vendors | `building-2` |
| Orders | `clipboard-list` |
| Sales | `shopping-cart` |
| Reports | `bar-chart-2` |
| JIT / Threshold | `activity` |
| Low stock alert | `alert-triangle` |
| Settings | `settings` |
| Search | `search` |
| Add / Create | `plus` |
| Edit | `pencil` |
| Delete | `trash-2` |
| Rack / Location | `map-pin` |
| Price / Revenue | `indian-rupee` |

---

## FILES INDEX

```
/
├── README.md                   ← This file
├── SKILL.md                    ← Agent skill descriptor
├── colors_and_type.css         ← All CSS custom properties (colors + type)
├── assets/
│   └── logo.svg                ← ASPAS wordmark logo
├── preview/
│   ├── colors-primary.html     ← Primary color palette card
│   ├── colors-semantic.html    ← Semantic/state colors card
│   ├── type-scale.html         ← Typography scale card
│   ├── type-mono.html          ← Monospace / data type card
│   ├── spacing-tokens.html     ← Spacing scale card
│   ├── shadows-radii.html      ← Shadow + radius card
│   ├── components-buttons.html ← Button variants card
│   ├── components-badges.html  ← Badge/tag variants card
│   ├── components-inputs.html  ← Form input card
│   ├── components-table.html   ← Data table row card
│   └── components-statcard.html← Stat summary card
└── ui_kits/
    └── dashboard/
        ├── README.md           ← Dashboard kit notes
        └── index.html          ← Interactive dashboard prototype
```
