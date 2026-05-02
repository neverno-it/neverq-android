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

## Cart checkout verification
- Files changed: `app/src/main/java/com/neverno/neverq/customer/cart/CartViewModel.kt`
- What was done: Verified checkout calls `api.checkout(CheckoutRequest(paymentMode = "cod"))` and stores the returned `orderId` in `placedOrderId` so CartScreen navigates to order detail.
- Issues found: None.

## Login and Google auth visibility
- Files changed: `app/src/main/java/com/neverno/neverq/auth/LoginScreen.kt`, `app/src/main/java/com/neverno/neverq/auth/AuthViewModel.kt`
- What was done: Added a visible customer `Continue with Google` action that opens the live web Google login route and made password-login routing tolerate case/role aliases for customer, kitchen, POS, and admin users.
- Issues found: The Android Firebase config has no OAuth client and the API does not expose a mobile Google token endpoint, so native in-app Google token login still needs backend/config work.
