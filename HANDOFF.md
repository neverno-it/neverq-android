# NeverQ Android Codex Handoff

== COMPLETED IN THIS CODEX SESSION ==

- Login redesign and routing
  - `app/src/main/java/com/neverno/neverq/auth/LoginScreen.kt`
  - `app/src/main/java/com/neverno/neverq/auth/AuthViewModel.kt`
  - `app/src/main/java/com/neverno/neverq/auth/AuthRepository.kt`
  - `app/src/main/java/com/neverno/neverq/core/network/AuthInterceptor.kt`
  - `app/src/main/java/com/neverno/neverq/MainActivity.kt`
  - `app/src/main/java/com/neverno/neverq/navigation/NavGraph.kt`
  - Added customer/staff mode handling, tolerant role routing, stale-token-safe login calls, and visible customer Google entry point.

- Customer cart redesign and checkout verification
  - `app/src/main/java/com/neverno/neverq/customer/cart/CartScreen.kt`
  - `app/src/main/java/com/neverno/neverq/customer/cart/CartViewModel.kt`
  - Rebuilt cart with brand colors, item cards, quantity steppers, summary card, and COD checkout through `CheckoutRequest(paymentMode = "cod")`.

- Customer order screens
  - `app/src/main/java/com/neverno/neverq/customer/orders/OrderHistoryScreen.kt`
  - `app/src/main/java/com/neverno/neverq/customer/orders/OrderDetailScreen.kt`
  - Added branded order list cards, status badges, empty state, order detail status card, items, and payment summary.

- Customer profile, wallet, and notifications
  - `app/src/main/java/com/neverno/neverq/customer/CustomerShell.kt`
  - `app/src/main/java/com/neverno/neverq/customer/ProfileViewModel.kt`
  - `app/src/main/java/com/neverno/neverq/customer/WalletScreen.kt`
  - `app/src/main/java/com/neverno/neverq/customer/NotificationsScreen.kt`
  - Profile now reads actual profile/session data, and profile actions navigate to orders, wallet, notifications, and logout.

- Customer menu and portal parity foundation
  - `app/src/main/java/com/neverno/neverq/customer/menu/CustomerHomeScreen.kt`
  - `app/src/main/java/com/neverno/neverq/customer/menu/CustomerMenuViewModel.kt`
  - `app/src/main/java/com/neverno/neverq/customer/menu/ProductDetailScreen.kt`
  - `app/src/main/java/com/neverno/neverq/core/models/ApiModels.kt`
  - `app/src/main/java/com/neverno/neverq/core/network/ApiService.kt`
  - Added submenu/category chips, banners, offerings, offers, featured products, cafe card, product cards, and product detail navigation.

- Native Google sign-in plumbing
  - `app/build.gradle.kts`
  - `gradle/libs.versions.toml`
  - `app/src/main/java/com/neverno/neverq/auth/LoginScreen.kt`
  - `app/src/main/java/com/neverno/neverq/auth/AuthRepository.kt`
  - Added Google Play Services auth dependency and ID-token handoff to the backend API. This still needs Google OAuth configuration before live use.

- Admin navigation expansion
  - `app/src/main/java/com/neverno/neverq/admin/AdminShell.kt`
  - `app/src/main/java/com/neverno/neverq/admin/AdminViewModel.kt`
  - `app/src/main/java/com/neverno/neverq/admin/AdminCatalogScreen.kt`
  - `app/src/main/java/com/neverno/neverq/admin/AdminStaffScreen.kt`
  - `app/src/main/java/com/neverno/neverq/admin/AdminCouponsScreen.kt`
  - Expanded admin bottom navigation to Dashboard, Orders, Catalog, Staff, and Coupons with read-only API-backed screens.

== CURRENT STATE ==

- Current Android work was on branch `codex/customer-parity`; this handoff commit was then fast-forwarded into local `master` for `git push origin master`.
- The app built successfully with `.\gradlew assembleDebug --no-daemon` after the customer parity changes.
- Latest normal debug APK output path: `app/build/outputs/apk/debug/app-debug.apk`.
- A crash was seen on one installed APK where Android reported missing `TokenManager`. That installed APK was bad/corrupt. A fresh rollback APK was installed over ADB and launched without crashing.
- Emergency rollback APK created during recovery: `C:\Users\ThinkPad\Desktop\NeverQ-Android-rollback-login-only.apk`.
- The live VPS/web backend was not changed by this Android handoff work.
- The richer customer menu/product-detail/native Google pieces expect backend API additions from backend repo branch `codex/customer-parity-api`, commit `7a98cd9`. Without that backend deployed, existing menu/login still works, but product detail and native Google API calls can fail.
- Google sign-in is not live-ready until `GOOGLE_WEB_CLIENT_ID` is supplied to the Android build and the same client ID is allowed in backend `GOOGLE_APP_ALLOWED_CLIENT_IDS`.

== WHAT IS STILL NEEDED ==

1. Validate the current master APK on a clean device install, especially customer login, menu load, cart, checkout, orders, profile, wallet, and notifications.
2. Deploy or stage-test the backend customer parity API before enabling product detail and native Google sign-in in production.
3. Configure Google Cloud OAuth for Android/native sign-in: package name, SHA fingerprints, Android client, web client ID, `GOOGLE_WEB_CLIENT_ID`, and backend allowed client IDs.
4. Build native customer sign-up/registration flow, including company/building selection, OTP verification, duplicate-email handling, and Google sign-up completion.
5. Finish customer checkout parity: coupon UI, wallet usage, online payment modes, cafe selection persistence, offers/free-meal logic, and order confirmation.
6. Add full customer product/category/offering detail parity and cafe selector behavior matching the web portal.
7. Expand notifications: unread count, mark-read action, polling, push permission UX, and deep links.
8. Continue role parity after customer: admin/superadmin, cashier/POS, operations manager, kitchen, reports, and any dashboard-only web modules.
9. Add device/UI QA and release signing/versioning before shipping an APK to real users.

== IMPORTANT NOTES FOR NEXT DEVELOPER ==

- `app/google-services.json` is local/untracked. If building in a new worktree, copy it into `app/google-services.json` or the Google Services Gradle task will fail.
- The Android package is `com.neverno.neverq`.
- ADB was repaired by reinstalling Android SDK `platform-tools` to version `37.0.0`; the OPPO device appeared as `CPH2363`.
- Do not push or deploy backend parity code straight to the live VPS without a backup and `manage.py check`; the last known stable mobile backend menu fix was backend commit `d23e83d`.
- The customer parity Android code has defaults for new menu response fields, so basic menu parsing should tolerate the older live API, but product detail uses `GET /api/v1/customer/products/{id}/`, which requires the backend parity API.
- The emergency rollback APK forces start at login and was created only to recover the connected phone; it is not the normal app state for this repo.
- When using ADB text input, `@` may be entered incorrectly; manual entry on the device is safer for email/password testing.
- If the app crashes, capture logs with:
  `adb logcat -d -v time AndroidRuntime:E '*:S'`

== BUILD COMMAND ==

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
Set-Location "C:\Users\ThinkPad\Desktop\NeverQ-Android"
.\gradlew assembleDebug --no-daemon
```
