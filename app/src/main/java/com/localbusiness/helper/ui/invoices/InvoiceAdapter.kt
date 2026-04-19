package com.localbusiness.helper.ui.invoices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.local.entity.PaymentStatus
import com.localbusiness.helper.databinding.ItemInvoiceBinding
import com.localbusiness.helper.utils.DateUtils
import java.text.NumberFormat
import java.util.Locale

class InvoiceAdapter(private val onClick: (Order) -> Unit) :
    ListAdapter<Order, InvoiceAdapter.ViewHolder>(DIFF) {

    private val currency = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    inner class ViewHolder(private val b: ItemInvoiceBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(order: Order) {
            b.tvCustomerName.text = order.customerName
            b.tvProduct.text = order.product
            b.tvTotal.text = currency.format(order.totalAmount)
            b.tvPaid.text = "Paid: ${currency.format(order.paidAmount)}"
            val due = order.totalAmount - order.paidAmount
            b.tvDue.text = "Due: ${currency.format(due)}"
            b.tvDelivery.text = "Delivery: ${DateUtils.formatDate(order.deliveryDate)}"

            val (chipText, chipColor) = when (order.paymentStatus) {
                PaymentStatus.PAID -> "PAID" to com.localbusiness.helper.R.color.payment_paid
                PaymentStatus.UNPAID -> "UNPAID" to com.localbusiness.helper.R.color.payment_unpaid
                PaymentStatus.PARTIAL -> "PARTIAL" to com.localbusiness.helper.R.color.payment_partial
            }
            b.chipPayment.text = chipText
            b.chipPayment.setChipBackgroundColorResource(chipColor)

            b.root.setOnClickListener { onClick(order) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemInvoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(a: Order, b: Order) = a.id == b.id
            override fun areContentsTheSame(a: Order, b: Order) = a == b
        }
    }
}
