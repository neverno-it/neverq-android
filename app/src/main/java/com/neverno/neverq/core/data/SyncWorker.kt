package com.neverno.neverq.core.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.neverno.neverq.core.db.daos.KitchenOrderDao
import com.neverno.neverq.core.db.daos.PosProductDao
import com.neverno.neverq.core.db.entities.CachedKitchenOrder
import com.neverno.neverq.core.db.entities.CachedPosProduct
import com.neverno.neverq.core.models.KitchenOrder
import com.neverno.neverq.core.network.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: ApiService,
    private val kitchenDao: KitchenOrderDao,
    private val posDao: PosProductDao,
    private val moshi: Moshi,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userType = inputData.getString("user_type") ?: return Result.success()
            val role = inputData.getString("role") ?: return Result.success()

            if (role == "cafeman" || role == "admin" || role == "superadmin") {
                syncKitchenOrders()
            }
            if (role == "pos" || role == "admin" || role == "superadmin") {
                syncPosProducts()
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncKitchenOrders() {
        val response = api.getKitchenOrders()
        if (response.isSuccessful && response.body() != null) {
            val itemsType = Types.newParameterizedType(List::class.java, com.neverno.neverq.core.models.OrderItemDetail::class.java)
            val adapter = moshi.adapter<List<com.neverno.neverq.core.models.OrderItemDetail>>(itemsType)
            kitchenDao.insertAll(response.body()!!.map { order ->
                CachedKitchenOrder(
                    id = order.id,
                    orderNumber = order.orderNumber,
                    orderStatus = order.orderStatus,
                    statusLabel = order.statusLabel,
                    totalAmount = order.totalAmount,
                    paymentMode = order.paymentMode,
                    createdAt = order.createdAt,
                    displayCustomerName = order.displayCustomerName,
                    displayCustomerPhone = order.displayCustomerPhone,
                    itemsJson = adapter.toJson(order.items),
                )
            })
        }
    }

    private suspend fun syncPosProducts() {
        val response = api.getPosProducts()
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            val all = (body.posProducts + body.menuProducts).map {
                CachedPosProduct(it.id, it.name, it.price, "mixed")
            }
            posDao.clearAll()
            posDao.insertAll(all)
        }
    }

    companion object {
        private const val WORK_NAME = "neverq_staff_sync"

        fun schedule(context: Context, userType: String, role: String) {
            val data = workDataOf("user_type" to userType, "role" to role)
            val request = PeriodicWorkRequestBuilder<SyncWorker>(5, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
