package com.localbusiness.helper.ui.invoices

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.localbusiness.helper.R
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.local.entity.PaymentStatus
import com.localbusiness.helper.databinding.FragmentInvoicesBinding
import com.localbusiness.helper.ui.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class InvoicesFragment : Fragment() {

    private var _binding: FragmentInvoicesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InvoicesViewModel by viewModels { ViewModelFactory(requireContext()) }
    private lateinit var adapter: InvoiceAdapter
    private val currency = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInvoicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = InvoiceAdapter { order -> navigateToOrder(order) }
        binding.rvInvoices.adapter = adapter

        setupObservers()
        setupChipFilters()
    }

    private fun setupObservers() {
        viewModel.pendingAmount.observe(viewLifecycleOwner) {
            binding.tvPendingAmount.text = currency.format(it ?: 0.0)
        }
        viewModel.totalCollected.observe(viewLifecycleOwner) {
            binding.tvCollected.text = currency.format(it ?: 0.0)
        }
        viewModel.unpaidCount.observe(viewLifecycleOwner) {
            binding.tvUnpaidCount.text = "${it ?: 0} unpaid"
        }
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            val filtered = when (binding.chipGroup.checkedChipId) {
                R.id.chipUnpaid -> orders.filter { it.paymentStatus != PaymentStatus.PAID }
                R.id.chipPaid -> orders.filter { it.paymentStatus == PaymentStatus.PAID }
                else -> orders
            }
            adapter.submitList(filtered)
            binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupChipFilters() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, _ ->
            viewModel.orders.value?.let { orders ->
                val filtered = when (binding.chipGroup.checkedChipId) {
                    R.id.chipUnpaid -> orders.filter { it.paymentStatus != PaymentStatus.PAID }
                    R.id.chipPaid -> orders.filter { it.paymentStatus == PaymentStatus.PAID }
                    else -> orders
                }
                adapter.submitList(filtered)
            }
        }
    }

    private fun navigateToOrder(order: Order) {
        val bundle = Bundle().apply { putLong("orderId", order.id) }
        findNavController().navigate(R.id.orderDetailFragment, bundle)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
