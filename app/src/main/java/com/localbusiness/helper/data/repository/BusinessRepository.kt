package com.localbusiness.helper.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.localbusiness.helper.data.local.AppDatabase
import com.localbusiness.helper.data.local.entity.Customer
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.local.entity.OrderStatus
import com.localbusiness.helper.data.local.entity.PaymentStatus
import com.localbusiness.helper.data.remote.NetworkClient
import com.localbusiness.helper.utils.DateUtils
import kotlinx.coroutines.flow.Flow

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class BusinessRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val customerDao = db.customerDao()
    private val orderDao = db.orderDao()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    // ─── Customer Operations ───────────────────────────────────────────────

    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    suspend fun saveCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    fun getCustomerCount(): LiveData<Int> = customerDao.getCustomerCount()

    // ─── Order Operations ──────────────────────────────────────────────────

    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()

    fun getOrdersByCustomer(customerId: Long): Flow<List<Order>> =
        orderDao.getOrdersByCustomer(customerId)

    fun searchOrders(query: String): Flow<List<Order>> = orderDao.searchOrders(query)

    suspend fun getOrderById(id: Long): Order? = orderDao.getOrderById(id)

    suspend fun saveOrder(order: Order): Long = orderDao.insertOrder(order)

    suspend fun updateOrder(order: Order) = orderDao.updateOrder(order)

    suspend fun deleteOrder(order: Order) = orderDao.deleteOrder(order)

    fun getTodayFollowUps(): Flow<List<Order>> {
        val (start, end) = DateUtils.todayRange()
        return orderDao.getTodayFollowUps(start, end)
    }

    fun getTodayFollowUpsLive(): LiveData<List<Order>> {
        val (start, end) = DateUtils.todayRange()
        return orderDao.getTodayFollowUpsLive(start, end)
    }

    // ─── Dashboard Stats ───────────────────────────────────────────────────

    fun getTotalOrderCount(): LiveData<Int> = orderDao.getTotalOrderCount()
    fun getPendingOrderCount(): LiveData<Int> = orderDao.getPendingOrderCount()
    fun getTotalPendingAmount(): LiveData<Double?> = orderDao.getTotalPendingAmount()
    fun getUnpaidOrderCount(): LiveData<Int> = orderDao.getUnpaidOrderCount()
    fun getTotalCollected(): LiveData<Double?> = orderDao.getTotalCollected()

    // ─── Google Sheets Sync ────────────────────────────────────────────────

    suspend fun syncFromGoogleSheets(): Result<Int> {
        val spreadsheetId = prefs.getString("sheets_id", "") ?: ""
        val apiKey = prefs.getString("sheets_api_key", "") ?: ""
        val range = prefs.getString("sheets_range", "Sheet1!A2:I") ?: "Sheet1!A2:I"

        if (spreadsheetId.isEmpty() || apiKey.isEmpty()) {
            return Result.Error("Google Sheets not configured. Please set up in Settings.")
        }

        return try {
            val response = NetworkClient.sheetsApiService.getSheetValues(
                spreadsheetId, range, apiKey
            )

            if (!response.isSuccessful) {
                return Result.Error("API Error: ${response.code()} - ${response.message()}")
            }

            val values = response.body()?.values ?: return Result.Error("No data found in sheet")
            var syncCount = 0

            values.forEachIndexed { index, row ->
                if (row.size < 7) return@forEachIndexed

                val customerName = row.getOrElse(0) { "" }.trim()
                val phone = row.getOrElse(1) { "" }.trim()
                val product = row.getOrElse(2) { "" }.trim()
                val quantity = row.getOrElse(3) { "1" }.toIntOrNull() ?: 1
                val price = row.getOrElse(4) { "0" }.replace(",", "").toDoubleOrNull() ?: 0.0
                val orderDateStr = row.getOrElse(5) { "" }.trim()
                val deliveryDateStr = row.getOrElse(6) { "" }.trim()
                val paymentStatusStr = row.getOrElse(7) { "Unpaid" }.trim()
                val followUpDateStr = row.getOrElse(8) { "" }.trim()

                if (customerName.isEmpty()) return@forEachIndexed

                val syncId = "sheet_row_${index + 2}"

                // Upsert customer
                val existingCustomer = customerDao.getCustomerBySyncId(syncId)
                val customerId = if (existingCustomer != null) {
                    customerDao.updateCustomer(
                        existingCustomer.copy(name = customerName, phone = phone)
                    )
                    existingCustomer.id
                } else {
                    customerDao.insertCustomer(
                        Customer(
                            name = customerName,
                            phone = phone,
                            syncId = syncId
                        )
                    )
                }

                // Parse payment status
                val paymentStatus = when (paymentStatusStr.lowercase()) {
                    "paid" -> PaymentStatus.PAID
                    "partial" -> PaymentStatus.PARTIAL
                    else -> PaymentStatus.UNPAID
                }

                val orderDate = DateUtils.parseDate(orderDateStr)
                val deliveryDate = DateUtils.parseDate(deliveryDateStr)
                val followUpDate = DateUtils.parseDate(followUpDateStr)

                // Upsert order
                val existingOrder = orderDao.getOrderBySyncId(syncId)
                if (existingOrder != null) {
                    orderDao.updateOrder(
                        existingOrder.copy(
                            customerName = customerName,
                            product = product,
                            quantity = quantity,
                            price = price,
                            totalAmount = quantity * price,
                            orderDate = orderDate,
                            deliveryDate = deliveryDate,
                            paymentStatus = paymentStatus,
                            followUpDate = followUpDate,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    orderDao.insertOrder(
                        Order(
                            customerId = customerId,
                            customerName = customerName,
                            product = product,
                            quantity = quantity,
                            price = price,
                            totalAmount = quantity * price,
                            orderDate = orderDate,
                            deliveryDate = deliveryDate,
                            paymentStatus = paymentStatus,
                            followUpDate = followUpDate,
                            syncId = syncId
                        )
                    )
                }
                syncCount++
            }

            Result.Success(syncCount)
        } catch (e: Exception) {
            Result.Error("Sync failed: ${e.message}", e)
        }
    }
}
