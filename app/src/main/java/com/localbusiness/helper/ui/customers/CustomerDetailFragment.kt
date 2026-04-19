package com.localbusiness.helper.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.localbusiness.helper.R
import com.localbusiness.helper.data.local.entity.Customer
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.databinding.FragmentCustomerDetailBinding
import com.localbusiness.helper.ui.orders.OrderAdapter
import com.localbusiness.helper.utils.DateUtils
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CustomerDetailFragment : Fragment(), OrderAdapter.OrderClickListener {

    private var _binding: FragmentCustomerDetailBinding? = null
    private val binding get() = _binding!!
    private var customerId: Long = 0L
    private lateinit var repo: BusinessRepository
    private lateinit var orderAdapter: OrderAdapter
    private val currency = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customerId = arguments?.getLong("customerId", 0L) ?: 0L
        repo = BusinessRepository(requireContext())
        orderAdapter = OrderAdapter(this)
        binding.rvOrders.adapter = orderAdapter
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val customer = repo.getCustomerById(customerId)
            customer?.let { bindCustomer(it) }
        }
        repo.getOrdersByCustomer(customerId).let { flow ->
            lifecycleScope.launch {
                flow.collect { orders ->
                    orderAdapter.submitList(orders)
                    val total = orders.sumOf { it.totalAmount }
                    val paid = orders.sumOf { it.paidAmount }
                    val pending = total - paid
                    binding.tvTotalOrders.text = orders.size.toString()
                    binding.tvTotalValue.text = currency.format(total)
                    binding.tvPendingAmount.text = currency.format(pending)
                    binding.emptyOrders.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvOrders.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun bindCustomer(customer: Customer) {
        requireActivity().title = customer.name
        val initials = customer.name.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
        binding.tvInitials.text = initials.ifEmpty { "?" }
        binding.tvName.text = customer.name
        binding.tvPhone.text = customer.phone
        binding.tvEmail.text = customer.email.ifEmpty { "—" }
        binding.tvAddress.text = customer.address.ifEmpty { "—" }
        binding.tvNotes.text = customer.notes.ifEmpty { "—" }
        binding.tvMemberSince.text = "Member since ${DateUtils.formatDate(customer.createdAt)}"

        binding.btnEdit.setOnClickListener {
            val bundle = Bundle().apply { putLong("customerId", customerId) }
            findNavController().navigate(R.id.addCustomerFragment, bundle)
        }
        binding.btnCall.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL,
                android.net.Uri.parse("tel:${customer.phone}"))
            startActivity(intent)
        }
    }

    override fun onOrderClick(order: Order) {
        val bundle = Bundle().apply { putLong("orderId", order.id) }
        findNavController().navigate(R.id.orderDetailFragment, bundle)
    }
    override fun onOrderEdit(order: Order) {
        val bundle = Bundle().apply { putLong("orderId", order.id) }
        findNavController().navigate(R.id.addOrderFragment, bundle)
    }
    override fun onOrderDelete(order: Order) {}

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
