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

## Admin menu expansion
- Files changed: `app/src/main/java/com/neverno/neverq/admin/AdminShell.kt`, `app/src/main/java/com/neverno/neverq/admin/AdminViewModel.kt`, `app/src/main/java/com/neverno/neverq/admin/AdminCatalogScreen.kt`, `app/src/main/java/com/neverno/neverq/admin/AdminStaffScreen.kt`, `app/src/main/java/com/neverno/neverq/admin/AdminCouponsScreen.kt`
- What was done: Expanded admin bottom navigation from Dashboard/Orders to Dashboard, Orders, Catalog, Staff, and Coupons; added API-backed catalog, staff, and coupon list screens with brand styling.
- Issues found: Current API exposes admin listing endpoints for products, categories, staff, and coupons but not create/edit/delete endpoints in the Android client yet, so these screens are read-only for now.

## Customer menu submenu support
- Files changed: `app/src/main/java/com/neverno/neverq/customer/menu/CustomerHomeScreen.kt`
- What was done: Added parent category filtering and child submenu chips using the category `parentId` field, while keeping veg-only filtering available as its own chip row.
- Issues found: None.

## Google auth browser handoff removal
- Files changed: `app/src/main/java/com/neverno/neverq/auth/LoginScreen.kt`, `app/src/main/java/com/neverno/neverq/MainActivity.kt`
- What was done: Removed the external browser handoff from the customer Google button, added an in-app explanation dialog, moved password sign-in above Google, made the login form scroll/keyboard safe, and normalized saved role routing on app startup.
- Issues found: The live Google OAuth flow is web-session based and redirects to the website; native Android Google login still requires an Android OAuth client plus a NeverQ API endpoint that returns app access/refresh tokens.

## Customer login fix
- Files changed: `app/src/main/java/com/neverno/neverq/core/models/ApiModels.kt`, `app/src/main/java/com/neverno/neverq/auth/AuthRepository.kt`, `app/src/main/java/com/neverno/neverq/auth/AuthViewModel.kt`, `app/src/main/java/com/neverno/neverq/auth/LoginScreen.kt`, `app/src/main/java/com/neverno/neverq/core/network/AuthInterceptor.kt`
- What was done: Customer login now sends `user_type = "customer"`, login response parsing tolerates missing/null customer role fields, routing falls back to the selected login mode, API error details are surfaced, and auth/refresh interceptors no longer attach stale bearer tokens or refresh around login calls.
- Issues found: Could not verify with real customer credentials locally, but invalid-login API checks confirmed the backend accepts the `user_type` field.

## Customer portal parity foundation
- Files changed: `app/build.gradle.kts`, `gradle/libs.versions.toml`, `app/src/main/java/com/neverno/neverq/auth/AuthRepository.kt`, `app/src/main/java/com/neverno/neverq/auth/AuthViewModel.kt`, `app/src/main/java/com/neverno/neverq/auth/LoginScreen.kt`, `app/src/main/java/com/neverno/neverq/core/models/ApiModels.kt`, `app/src/main/java/com/neverno/neverq/core/network/ApiService.kt`, `app/src/main/java/com/neverno/neverq/customer/CustomerShell.kt`, `app/src/main/java/com/neverno/neverq/customer/ProfileViewModel.kt`, `app/src/main/java/com/neverno/neverq/customer/NotificationsScreen.kt`, `app/src/main/java/com/neverno/neverq/customer/WalletScreen.kt`, `app/src/main/java/com/neverno/neverq/customer/menu/CustomerHomeScreen.kt`, `app/src/main/java/com/neverno/neverq/customer/menu/CustomerMenuViewModel.kt`, `app/src/main/java/com/neverno/neverq/customer/menu/ProductDetailScreen.kt`
- What was done: Added native Google account-picker plumbing, expanded customer menu models for banners/offerings/offers/featured products/cafes/recent orders, rebuilt the customer menu home to show portal-style sections, added product detail navigation, added notifications and wallet screens, and wired profile actions to real native destinations.
- Issues found: `app/google-services.json` has no OAuth client entries, so Google sign-in requires a `GOOGLE_WEB_CLIENT_ID` Gradle property or environment variable plus the matching backend `GOOGLE_APP_ALLOWED_CLIENT_IDS` value before it can authenticate live users.
