package com.localbusiness.helper.ui.customers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.localbusiness.helper.data.local.entity.Customer
import com.localbusiness.helper.databinding.ItemCustomerBinding

class CustomerAdapter(private val listener: CustomerClickListener) :
    ListAdapter<Customer, CustomerAdapter.ViewHolder>(DIFF_CALLBACK) {

    interface CustomerClickListener {
        fun onCustomerClick(customer: Customer)
        fun onCustomerEdit(customer: Customer)
        fun onCustomerDelete(customer: Customer)
    }

    inner class ViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: Customer) {
            binding.tvCustomerName.text = customer.name
            binding.tvCustomerPhone.text = customer.phone
            val initials = customer.name.split(" ")
                .take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
            binding.tvInitials.text = initials.ifEmpty { "?" }

            binding.root.setOnClickListener { listener.onCustomerClick(customer) }
            binding.btnEdit.setOnClickListener { listener.onCustomerEdit(customer) }
            binding.btnDelete.setOnClickListener { listener.onCustomerDelete(customer) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Customer>() {
            override fun areItemsTheSame(oldItem: Customer, newItem: Customer) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Customer, newItem: Customer) = oldItem == newItem
        }
    }
}
