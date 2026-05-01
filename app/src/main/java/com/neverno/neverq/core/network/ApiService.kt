package com.neverno.neverq.core.network

import com.neverno.neverq.core.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/token/refresh/")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<TokenRefreshResponse>

    @POST("auth/logout/")
    suspend fun logout(@Body body: Map<String, String>): Response<MessageResponse>

    @POST("auth/fcm-token/")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): Response<MessageResponse>

    // ── Customer ──────────────────────────────────────────────────────────────
    @GET("customer/profile/")
    suspend fun getProfile(): Response<CustomerProfile>

    @GET("customer/store/")
    suspend fun getStoreInfo(): Response<CompanyInfo>

    @GET("customer/menu/")
    suspend fun getMenu(): Response<MenuResponse>

    @GET("customer/cart/")
    suspend fun getCart(): Response<CartResponse>

    @POST("customer/cart/")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "customer/cart/", hasBody = true)
    suspend fun removeFromCart(@Body request: RemoveCartItemRequest): Response<MessageResponse>

    @POST("customer/cart/clear/")
    suspend fun clearCart(): Response<MessageResponse>

    @POST("customer/coupon/apply/")
    suspend fun applyCoupon(@Body request: CouponApplyRequest): Response<CouponApplyResponse>

    @POST("customer/checkout/")
    suspend fun checkout(@Body request: CheckoutRequest): Response<CheckoutResponse>

    @GET("customer/orders/")
    suspend fun getOrders(): Response<List<OrderListItem>>

    @GET("customer/orders/{id}/")
    suspend fun getOrderDetail(@Path("id") id: Int): Response<OrderDetail>

    @POST("customer/reviews/")
    suspend fun submitReview(@Body request: ReviewRequest): Response<MessageResponse>

    @GET("customer/notifications/")
    suspend fun getNotifications(): Response<List<Notification>>

    // ── Kitchen ───────────────────────────────────────────────────────────────
    @GET("kitchen/orders/")
    suspend fun getKitchenOrders(): Response<List<KitchenOrder>>

    @PATCH("kitchen/orders/{id}/status/")
    suspend fun updateKitchenOrderStatus(
        @Path("id") id: Int,
        @Body request: UpdateStatusRequest,
    ): Response<UpdateStatusResponse>

    // ── POS ───────────────────────────────────────────────────────────────────
    @GET("pos/products/")
    suspend fun getPosProducts(): Response<PosProductsResponse>

    @POST("pos/orders/")
    suspend fun createPosOrder(@Body request: PosOrderRequest): Response<PosOrder>

    @GET("pos/orders/list/")
    suspend fun getPosOrders(): Response<List<PosOrder>>

    // ── Admin ─────────────────────────────────────────────────────────────────
    @GET("admin/dashboard/")
    suspend fun getDashboard(): Response<DashboardResponse>

    @GET("admin/orders/")
    suspend fun getAdminOrders(
        @Query("status") status: Int? = null,
        @Query("date") date: String? = null,
    ): Response<List<OrderListItem>>

    @GET("admin/orders/{id}/")
    suspend fun getAdminOrderDetail(@Path("id") id: Int): Response<OrderDetail>

    @PATCH("admin/orders/{id}/status/")
    suspend fun updateAdminOrderStatus(
        @Path("id") id: Int,
        @Body request: UpdateStatusRequest,
    ): Response<UpdateStatusResponse>

    @GET("admin/categories/")
    suspend fun getAdminCategories(): Response<List<Category>>

    @GET("admin/products/")
    suspend fun getAdminProducts(@Query("category") categoryId: Int? = null): Response<List<Product>>

    @PATCH("admin/products/{id}/toggle/")
    suspend fun toggleProduct(@Path("id") id: Int): Response<Map<String, Any>>

    @GET("admin/staff/")
    suspend fun getStaff(): Response<List<StaffUser>>

    @GET("admin/coupons/")
    suspend fun getCoupons(): Response<List<Coupon>>
}
