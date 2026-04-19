package com.localbusiness.helper.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.localbusiness.helper.R
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.local.entity.OrderStatus
import com.localbusiness.helper.data.local.entity.PaymentStatus
import com.localbusiness.helper.databinding.ItemOrderBinding
import com.localbusiness.helper.utils.DateUtils
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(private val listener: OrderClickListener) :
    ListAdapter<Order, OrderAdapter.ViewHolder>(DIFF) {

    interface OrderClickListener {
        fun onOrderClick(order: Order)
        fun onOrderEdit(order: Order)
        fun onOrderDelete(order: Order)
    }

    private val currency = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    inner class ViewHolder(private val b: ItemOrderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(order: Order) {
            b.tvCustomerName.text = order.customerName
            b.tvProduct.text = "${order.product} × ${order.quantity}"
            b.tvAmount.text = currency.format(order.totalAmount)
            b.tvOrderDate.text = DateUtils.formatDate(order.orderDate)

            // Status chip
            b.chipStatus.text = order.status.displayName
            val statusColor = when (order.status) {
                OrderStatus.PENDING -> R.color.status_pending
                OrderStatus.IN_PROGRESS -> R.color.status_in_progress
                OrderStatus.COMPLETED -> R.color.status_completed
                OrderStatus.DELIVERED -> R.color.status_delivered
                OrderStatus.CANCELLED -> R.color.status_cancelled
            }
            b.chipStatus.setChipBackgroundColorResource(statusColor)

            // Payment chip
            b.chipPayment.text = order.paymentStatus.displayName
            val payColor = when (order.paymentStatus) {
                PaymentStatus.PAID -> R.color.payment_paid
                PaymentStatus.UNPAID -> R.color.payment_unpaid
                PaymentStatus.PARTIAL -> R.color.payment_partial
            }
            b.chipPayment.setChipBackgroundColorResource(payColor)

            b.root.setOnClickListener { listener.onOrderClick(order) }
            b.btnEdit.setOnClickListener { listener.onOrderEdit(order) }
            b.btnDelete.setOnClickListener { listener.onOrderDelete(order) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(a: Order, b: Order) = a.id == b.id
            override fun areContentsTheSame(a: Order, b: Order) = a == b
        }
    }
}
