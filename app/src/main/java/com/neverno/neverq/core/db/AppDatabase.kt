package com.neverno.neverq.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neverno.neverq.core.db.daos.KitchenOrderDao
import com.neverno.neverq.core.db.daos.PosProductDao
import com.neverno.neverq.core.db.entities.CachedKitchenOrder
import com.neverno.neverq.core.db.entities.CachedPosProduct

@Database(
    entities = [CachedKitchenOrder::class, CachedPosProduct::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kitchenOrderDao(): KitchenOrderDao
    abstract fun posProductDao(): PosProductDao
}
