package com.neverno.neverq.core.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_kitchen_orders")
data class CachedKitchenOrder(
    @PrimaryKey val id: Int,
    val orderNumber: String,
    val orderStatus: Int,
    val statusLabel: String,
    val totalAmount: String,
    val paymentMode: String,
    val createdAt: String?,
    val displayCustomerName: String,
    val displayCustomerPhone: String,
    val itemsJson: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cached_pos_products")
data class CachedPosProduct(
    @PrimaryKey val id: String,
    val name: String,
    val price: String,
    val type: String,
    val cachedAt: Long = System.currentTimeMillis(),
)
