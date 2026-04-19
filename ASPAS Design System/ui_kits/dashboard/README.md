# ASPAS Dashboard UI Kit

Interactive click-through prototype of the ASPAS web admin dashboard.

## Screens
| Screen | Description |
|---|---|
| Dashboard | KPI stat cards, low-stock alerts, recent sales |
| Inventory | Full parts table, search, filter by low-stock, Add Part modal |
| Orders | Purchase order list with status badges |
| Sales | Transaction history with revenue totals |
| Vendors | Vendor directory cards with contact info |
| Reports | Monthly revenue bar chart + JIT threshold summary |

## Usage
Open `index.html` in a browser. All navigation is client-side. Last visited screen persists via localStorage.

## Components (all inline in index.html)
- `Sidebar` — fixed 220px navy sidebar with active state indicator
- `TopBar` — page header with title, subtitle, action buttons
- `Btn` — primary / secondary / ghost button variants
- `Badge` / `OrderBadge` — status chips
- `Icon` — Lucide icon wrapper
- `AddPartModal` — form modal overlay
- `DashboardScreen`, `InventoryScreen`, `VendorsScreen`, `OrdersScreen`, `SalesScreen`, `ReportsScreen`

## Design notes
- Font: DM Sans (UI) + DM Mono (numbers, part codes)
- Accent: #F59E0B amber — used for active nav, primary buttons, low-stock bar chart
- Row hover: amber tint `#FFFBEB`
- No animations beyond 150ms CSS transitions
