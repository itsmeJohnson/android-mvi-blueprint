package com.mvi.blueprint.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mvi.blueprint.data.database.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("""
        SELECT * FROM orders
        WHERE (:filter = 'ALL' OR status = :filter)
          AND (:query = '' OR customerName LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR productName LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun observeOrders(query: String, filter: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("UPDATE orders SET status = 'APPROVED' WHERE id IN (:orderIds)")
    suspend fun batchApproveOrders(orderIds: List<String>): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int

    @Query("DELETE FROM orders")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(orders: List<OrderEntity>) {
        clearAll()
        insertOrders(orders)
    }
}
