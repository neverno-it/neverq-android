# Codex Log

## Cart screen redesign
- Files changed: `app/src/main/java/com/neverno/neverq/customer/cart/CartScreen.kt`, `app/src/main/java/com/neverno/neverq/customer/cart/CartViewModel.kt`
- What was done: Rebuilt the cart UI with a CfNavy top app bar, CfSurface background, white rounded item cards, quantity stepper controls, summary card, and CfBlue COD-only Place Order button.
- Issues found: Existing cart screen text contained mojibake for rupee symbols, so display text now uses ASCII `Rs.` labels.

## Order history redesign
- Files changed: `app/src/main/java/com/neverno/neverq/customer/orders/OrderHistoryScreen.kt`
- What was done: Rebuilt order history with a CfNavy top app bar, CfSurface background, white order cards, order date/total details, status badges, chevron detail affordance, and icon empty state.
- Issues found: None.

## Order detail redesign
- Files changed: `app/src/main/java/com/neverno/neverq/customer/orders/OrderDetailScreen.kt`
- What was done: Rebuilt order detail with a CfNavy top app bar, colored status card, white items card, and payment summary card for subtotal, wallet used, coupon discount, and total.
- Issues found: None.

## Customer profile improvement
- Files changed: `app/src/main/java/com/neverno/neverq/customer/ProfileViewModel.kt`, `app/src/main/java/com/neverno/neverq/customer/CustomerShell.kt`
- What was done: Added a dedicated ProfileViewModel that reads stored user name, email, and company ID from TokenManager flows, enriches company name from the profile API when available, and preserves logout navigation.
- Issues found: TokenManager stores company ID but not company name, so the screen falls back to company ID if the profile API cannot provide a name.
