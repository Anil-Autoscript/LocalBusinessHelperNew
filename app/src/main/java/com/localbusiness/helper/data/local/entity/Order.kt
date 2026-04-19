package com.localbusiness.helper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val product: String,
    val quantity: Int,
    val price: Double,
    val totalAmount: Double = quantity * price,
    val orderDate: Long,
    val deliveryDate: Long,
    val followUpDate: Long = 0L,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncId: String = ""
)

enum class OrderStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

enum class PaymentStatus(val displayName: String) {
    PAID("Paid"),
    UNPAID("Unpaid"),
    PARTIAL("Partial")
}
