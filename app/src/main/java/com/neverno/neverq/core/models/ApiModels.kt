package com.neverno.neverq.core.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── Auth ──────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String,
    @Json(name = "user_type") val userType: String? = null,
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val access: String,
    val refresh: String,
    @Json(name = "user_type") val userType: String = "",
    val role: String? = null,
    val name: String = "",
    val email: String = "",
    @Json(name = "company_id") val companyId: Int? = null,
    @Json(name = "auth_status") val authStatus: String? = null,
)

@JsonClass(generateAdapter = true)
data class TokenRefreshRequest(val refresh: String)

@JsonClass(generateAdapter = true)
data class GoogleLoginRequest(
    @Json(name = "id_token") val idToken: String,
    @Json(name = "customer_id") val customerId: Int? = null,
)

@JsonClass(generateAdapter = true)
data class TokenRefreshResponse(val access: String)

@JsonClass(generateAdapter = true)
data class FcmTokenRequest(val token: String, val platform: String = "android")

// ── Company ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CompanyInfo(
    val id: Int,
    val name: String,
    @Json(name = "logo_url") val logoUrl: String?,
    @Json(name = "store_status") val storeStatus: Boolean,
    @Json(name = "is_store_open") val isStoreOpen: Boolean,
    @Json(name = "ordering_status_message") val orderingStatusMessage: String,
    @Json(name = "order_window_label") val orderWindowLabel: String?,
    @Json(name = "fulfillment_mode") val fulfillmentMode: String,
)

// ── Menu ──────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class Category(
    val id: Int,
    val name: String,
    val slug: String,
    @Json(name = "parent_id") val parentId: Int?,
    @Json(name = "sort_order") val sortOrder: Int,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "icon_type") val iconType: Int = 0,
)

@JsonClass(generateAdapter = true)
data class Product(
    val id: Int,
    val name: String,
    val slug: String,
    val price: String,
    val description: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "category_id") val categoryId: Int,
    @Json(name = "category_name") val categoryName: String?,
    @Json(name = "is_veg") val isVeg: Boolean,
    val calories: Int?,
    @Json(name = "is_available") val isAvailable: Boolean,
    @Json(name = "display_price") val displayPrice: String = "",
    @Json(name = "discounted_price") val discountedPrice: String? = null,
    @Json(name = "max_qty") val maxQty: Int = -1,
    @Json(name = "food_label") val foodLabel: String = "",
    @Json(name = "has_offer") val hasOffer: Boolean = false,
    @Json(name = "offer_title") val offerTitle: String = "",
    @Json(name = "is_free_meal_eligible") val isFreeMealEligible: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class Banner(
    val id: Int,
    val name: String = "",
    @Json(name = "image_url") val imageUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class Offering(
    val id: Int,
    val name: String,
    val slug: String = "",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true,
)

@JsonClass(generateAdapter = true)
data class Offer(
    val id: Int,
    val title: String,
    @Json(name = "offer_type") val offerType: String,
    val value: String,
    @Json(name = "min_order_value") val minOrderValue: String? = null,
    @Json(name = "max_discount") val maxDiscount: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "badge_label") val badgeLabel: String = "Offer",
)

@JsonClass(generateAdapter = true)
data class Cafe(
    val id: Int,
    val name: String,
    @Json(name = "building_id") val buildingId: Int? = null,
    @Json(name = "building_name") val buildingName: String? = null,
)

@JsonClass(generateAdapter = true)
data class MenuResponse(
    val categories: List<Category>,
    val products: List<Product>,
    @Json(name = "featured_products") val featuredProducts: List<Product> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val offerings: List<Offering> = emptyList(),
    val offers: List<Offer> = emptyList(),
    @Json(name = "recent_orders") val recentOrders: List<OrderListItem> = emptyList(),
    val cafes: List<Cafe> = emptyList(),
    @Json(name = "selected_cafe_id") val selectedCafeId: Int? = null,
    @Json(name = "is_store_open") val isStoreOpen: Boolean,
    @Json(name = "ordering_status_message") val orderingStatusMessage: String,
    @Json(name = "store_name") val storeName: String = "",
    @Json(name = "order_window_label") val orderWindowLabel: String? = null,
)

@JsonClass(generateAdapter = true)
data class ProductDetailResponse(
    val product: Product,
    @Json(name = "similar_products") val similarProducts: List<Product> = emptyList(),
)

// ── Cart ──────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CartItem(
    val id: Int,
    val product: Product,
    val qty: Int,
    @Json(name = "line_total") val lineTotal: String,
)

@JsonClass(generateAdapter = true)
data class CartResponse(
    val items: List<CartItem>,
    val subtotal: String,
    @Json(name = "item_count") val itemCount: Int,
)

@JsonClass(generateAdapter = true)
data class AddToCartRequest(
    @Json(name = "product_id") val productId: Int,
    val qty: Int,
)

@JsonClass(generateAdapter = true)
data class RemoveCartItemRequest(@Json(name = "item_id") val itemId: Int)

// ── Coupon ────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CouponApplyRequest(val code: String, val subtotal: String)

@JsonClass(generateAdapter = true)
data class CouponApplyResponse(
    @Json(name = "coupon_id") val couponId: Int,
    val code: String,
    val discount: String,
    val description: String,
)

// ── Orders ────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class OrderListItem(
    val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "order_status") val orderStatus: Int,
    @Json(name = "status_label") val statusLabel: String,
    @Json(name = "status_color") val statusColor: String,
    @Json(name = "total_amount") val totalAmount: String,
    @Json(name = "payment_mode") val paymentMode: String,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "item_count") val itemCount: Int,
)

@JsonClass(generateAdapter = true)
data class OrderItemDetail(
    val id: Int,
    @Json(name = "product_name") val productName: String,
    val qty: Int,
    val price: String,
    @Json(name = "line_total") val lineTotal: String,
    @Json(name = "image_url") val imageUrl: String?,
)

@JsonClass(generateAdapter = true)
data class OrderDetail(
    val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "order_status") val orderStatus: Int,
    @Json(name = "status_label") val statusLabel: String,
    @Json(name = "status_color") val statusColor: String,
    val subtotal: String,
    @Json(name = "coupon_discount") val couponDiscount: String,
    @Json(name = "wallet_used") val walletUsed: String,
    @Json(name = "total_amount") val totalAmount: String,
    @Json(name = "payment_mode") val paymentMode: String,
    @Json(name = "payment_status") val paymentStatus: String,
    @Json(name = "order_type") val orderType: Int,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "display_customer_name") val displayCustomerName: String,
    val items: List<OrderItemDetail>,
)

@JsonClass(generateAdapter = true)
data class CheckoutRequest(
    @Json(name = "payment_mode") val paymentMode: String,
    @Json(name = "coupon_id") val couponId: Int? = null,
    @Json(name = "wallet_use") val walletUse: String = "0",
)

@JsonClass(generateAdapter = true)
data class CheckoutResponse(
    @Json(name = "order_id") val orderId: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "total_amount") val totalAmount: String,
)

// ── Customer Profile ─────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CustomerProfile(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    @Json(name = "company_id") val companyId: Int,
    @Json(name = "company_name") val companyName: String,
    @Json(name = "wallet_balance") val walletBalance: String,
    @Json(name = "royalty_points") val royaltyPoints: Int,
    @Json(name = "meal_benefit") val mealBenefit: String,
)

// ── Reviews ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ReviewRequest(
    @Json(name = "order_id") val orderId: Int,
    val rating: Float,
    val details: String,
)

// ── Notifications ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class Notification(
    val id: Int,
    @Json(name = "notif_type") val notifType: String,
    val title: String,
    val message: String?,
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "created_at") val createdAt: String?,
)

// ── Kitchen ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class KitchenOrder(
    val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "order_status") val orderStatus: Int,
    @Json(name = "status_label") val statusLabel: String,
    @Json(name = "total_amount") val totalAmount: String,
    @Json(name = "payment_mode") val paymentMode: String,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "display_customer_name") val displayCustomerName: String,
    @Json(name = "display_customer_phone") val displayCustomerPhone: String,
    val items: List<OrderItemDetail>,
)

@JsonClass(generateAdapter = true)
data class UpdateStatusRequest(@Json(name = "order_status") val orderStatus: Int)

@JsonClass(generateAdapter = true)
data class UpdateStatusResponse(
    @Json(name = "order_id") val orderId: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "order_status") val orderStatus: Int,
    @Json(name = "status_label") val statusLabel: String,
)

// ── POS ───────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class PosProduct(val id: String, val name: String, val price: String)

@JsonClass(generateAdapter = true)
data class PosProductsResponse(
    @Json(name = "pos_products") val posProducts: List<PosProduct>,
    @Json(name = "menu_products") val menuProducts: List<PosProduct>,
)

@JsonClass(generateAdapter = true)
data class PosOrderItemInput(
    @Json(name = "product_name") val productName: String,
    val price: String,
    val qty: Int,
)

@JsonClass(generateAdapter = true)
data class PosOrderRequest(
    @Json(name = "customer_name") val customerName: String = "Walk-in Customer",
    @Json(name = "customer_phone") val customerPhone: String = "",
    @Json(name = "customer_type") val customerType: String = "visitor",
    @Json(name = "payment_type") val paymentType: Int,
    val items: List<PosOrderItemInput>,
)

@JsonClass(generateAdapter = true)
data class PosOrderItem(
    val id: Int,
    @Json(name = "product_name") val productName: String,
    val price: String,
    val qty: Int,
    val amount: String,
)

@JsonClass(generateAdapter = true)
data class PosOrder(
    val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "customer_type") val customerType: String,
    @Json(name = "base_amount") val baseAmount: String,
    @Json(name = "card_fee_amount") val cardFeeAmount: String,
    @Json(name = "total_amount") val totalAmount: String,
    @Json(name = "payment_type") val paymentType: Int,
    @Json(name = "payment_label") val paymentLabel: String,
    @Json(name = "created_at") val createdAt: String?,
    val items: List<PosOrderItem>,
)

// ── Admin ─────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class DashboardStats(
    @Json(name = "today_web_orders") val todayWebOrders: Int,
    @Json(name = "today_web_revenue") val todayWebRevenue: String,
    @Json(name = "today_pos_orders") val todayPosOrders: Int,
    @Json(name = "today_pos_revenue") val todayPosRevenue: String,
    @Json(name = "total_web_orders") val totalWebOrders: Int,
    @Json(name = "total_web_revenue") val totalWebRevenue: String,
    @Json(name = "pending_orders") val pendingOrders: Int,
    @Json(name = "active_customers") val activeCustomers: Int,
)

@JsonClass(generateAdapter = true)
data class WeeklyRevenue(val date: String, val revenue: Double)

@JsonClass(generateAdapter = true)
data class DashboardResponse(
    val stats: DashboardStats,
    @Json(name = "weekly_revenue") val weeklyRevenue: List<WeeklyRevenue>,
)

@JsonClass(generateAdapter = true)
data class StaffUser(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    @Json(name = "role_label") val roleLabel: String,
    @Json(name = "company_id") val companyId: Int?,
    @Json(name = "company_name") val companyName: String?,
    @Json(name = "is_active") val isActive: Boolean,
)

@JsonClass(generateAdapter = true)
data class Coupon(
    val id: Int,
    val code: String,
    val description: String,
    @Json(name = "discount_type") val discountType: String,
    @Json(name = "discount_value") val discountValue: String,
    @Json(name = "min_order") val minOrder: String,
    @Json(name = "usage_limit") val usageLimit: Int,
    @Json(name = "used_count") val usedCount: Int,
    @Json(name = "valid_from") val validFrom: String?,
    @Json(name = "valid_to") val validTo: String?,
    @Json(name = "is_active") val isActive: Boolean,
)

// ── Generic ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class MessageResponse(val detail: String)
