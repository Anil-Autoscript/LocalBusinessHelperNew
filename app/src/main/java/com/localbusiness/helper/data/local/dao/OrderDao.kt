package com.localbusiness.helper.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.local.entity.OrderStatus
import com.localbusiness.helper.data.local.entity.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderDate DESC")
    fun getOrdersByCustomer(customerId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersByStatus(status: String): Flow<List<Order>>

    @Query("""
        SELECT * FROM orders 
        WHERE customerName LIKE '%' || :query || '%' 
        OR product LIKE '%' || :query || '%'
        ORDER BY orderDate DESC
    """)
    fun searchOrders(query: String): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<Order>)

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    // Dashboard stats
    @Query("SELECT COUNT(*) FROM orders")
    fun getTotalOrderCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status NOT IN ('DELIVERED','CANCELLED')")
    fun getPendingOrderCount(): LiveData<Int>

    @Query("SELECT SUM(totalAmount - paidAmount) FROM orders WHERE paymentStatus != 'PAID'")
    fun getTotalPendingAmount(): LiveData<Double?>

    @Query("""
        SELECT * FROM orders 
        WHERE followUpDate >= :dayStart AND followUpDate <= :dayEnd 
        ORDER BY followUpDate ASC
    """)
    fun getTodayFollowUps(dayStart: Long, dayEnd: Long): Flow<List<Order>>

    @Query("""
        SELECT * FROM orders 
        WHERE followUpDate >= :dayStart AND followUpDate <= :dayEnd 
        ORDER BY followUpDate ASC
    """)
    fun getTodayFollowUpsLive(dayStart: Long, dayEnd: Long): LiveData<List<Order>>

    @Query("SELECT COUNT(*) FROM orders WHERE paymentStatus = 'UNPAID' OR paymentStatus = 'PARTIAL'")
    fun getUnpaidOrderCount(): LiveData<Int>

    @Query("SELECT * FROM orders WHERE syncId = :syncId LIMIT 1")
    suspend fun getOrderBySyncId(syncId: String): Order?

    @Query("SELECT SUM(paidAmount) FROM orders WHERE paymentStatus = 'PAID' OR paymentStatus = 'PARTIAL'")
    fun getTotalCollected(): LiveData<Double?>
}
