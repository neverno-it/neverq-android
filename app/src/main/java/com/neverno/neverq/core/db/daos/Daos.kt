package com.neverno.neverq.core.db.daos

import androidx.room.*
import com.neverno.neverq.core.db.entities.CachedKitchenOrder
import com.neverno.neverq.core.db.entities.CachedPosProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface KitchenOrderDao {
    @Query("SELECT * FROM cached_kitchen_orders ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<CachedKitchenOrder>>

    @Query("SELECT * FROM cached_kitchen_orders ORDER BY createdAt ASC")
    suspend fun getAll(): List<CachedKitchenOrder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<CachedKitchenOrder>)

    @Query("UPDATE cached_kitchen_orders SET orderStatus = :status, statusLabel = :label WHERE id = :id")
    suspend fun updateStatus(id: Int, status: Int, label: String)

    @Query("DELETE FROM cached_kitchen_orders WHERE orderStatus IN (5, 6)")
    suspend fun deleteCompleted()

    @Query("DELETE FROM cached_kitchen_orders")
    suspend fun clearAll()
}

@Dao
interface PosProductDao {
    @Query("SELECT * FROM cached_pos_products ORDER BY name ASC")
    fun observeAll(): Flow<List<CachedPosProduct>>

    @Query("SELECT * FROM cached_pos_products ORDER BY name ASC")
    suspend fun getAll(): List<CachedPosProduct>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<CachedPosProduct>)

    @Query("DELETE FROM cached_pos_products")
    suspend fun clearAll()
}
