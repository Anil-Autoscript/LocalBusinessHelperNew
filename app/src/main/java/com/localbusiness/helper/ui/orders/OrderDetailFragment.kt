package com.localbusiness.helper.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.localbusiness.helper.R
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.local.entity.OrderStatus
import com.localbusiness.helper.data.local.entity.PaymentStatus
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.databinding.FragmentOrderDetailBinding
import com.localbusiness.helper.ui.ViewModelFactory
import com.localbusiness.helper.ui.customers.OperationState
import com.localbusiness.helper.utils.DateUtils
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private var orderId: Long = 0L
    private var currentOrder: Order? = null
    private val viewModel: OrdersViewModel by viewModels { ViewModelFactory(requireContext()) }
    private val currency = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderId = arguments?.getLong("orderId", 0L) ?: 0L
        loadOrder()
        setupObservers()
    }

    private fun loadOrder() {
        lifecycleScope.launch {
            val repo = BusinessRepository(requireContext())
            currentOrder = repo.getOrderById(orderId)
            currentOrder?.let { bindOrder(it) }
        }
    }

    private fun bindOrder(order: Order) {
        requireActivity().title = "Order #${order.id}"
        binding.tvCustomerName.text = order.customerName
        binding.tvProductSubtitle.text = order.product
        binding.tvProduct.text = order.product
        binding.tvQuantity.text = "Qty: ${order.quantity}"
        binding.tvPrice.text = "@ ${currency.format(order.price)}"
        binding.tvTotal.text = currency.format(order.totalAmount)
        binding.tvPaid.text = currency.format(order.paidAmount)
        binding.tvDue.text = currency.format(order.totalAmount - order.paidAmount)
        binding.tvOrderDate.text = DateUtils.formatDate(order.orderDate)
        binding.tvDeliveryDate.text = DateUtils.formatDate(order.deliveryDate)
        binding.tvFollowUpDate.text = if (order.followUpDate > 0L) DateUtils.formatDate(order.followUpDate) else "Not set"
        binding.tvNotes.text = order.notes.ifEmpty { "—" }

        val statusColor = when (order.status) {
            OrderStatus.PENDING -> R.color.status_pending
            OrderStatus.IN_PROGRESS -> R.color.status_in_progress
            OrderStatus.COMPLETED -> R.color.status_completed
            OrderStatus.DELIVERED -> R.color.status_delivered
            OrderStatus.CANCELLED -> R.color.status_cancelled
        }
        binding.chipStatus.setChipBackgroundColorResource(statusColor)
        binding.chipStatus.text = order.status.displayName

        val payColor = when (order.paymentStatus) {
            PaymentStatus.PAID -> R.color.payment_paid
            PaymentStatus.UNPAID -> R.color.payment_unpaid
            PaymentStatus.PARTIAL -> R.color.payment_partial
        }
        binding.chipPayment.setChipBackgroundColorResource(payColor)
        binding.chipPayment.text = order.paymentStatus.displayName

        binding.btnEdit.setOnClickListener {
            val bundle = Bundle().apply { putLong("orderId", orderId) }
            findNavController().navigate(R.id.addOrderFragment, bundle)
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Order")
                .setMessage("Delete this order for ${order.customerName}?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteOrder(order, requireContext())
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnUpdateStatus.setOnClickListener { showStatusUpdateDialog(order) }
        binding.btnMarkPaid.setOnClickListener { markAsPaid(order) }
    }

    private fun showStatusUpdateDialog(order: Order) {
        val statuses = OrderStatus.values().map { it.displayName }.toTypedArray()
        val current = order.status.ordinal
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Order Status")
            .setSingleChoiceItems(statuses, current) { dialog, which ->
                val newStatus = OrderStatus.values()[which]
                viewModel.updateOrder(
                    order.copy(status = newStatus, updatedAt = System.currentTimeMillis()),
                    requireContext()
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markAsPaid(order: Order) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Mark as Paid")
            .setMessage("Mark this order as fully paid (${currency.format(order.totalAmount)})?")
            .setPositiveButton("Mark Paid") { _, _ ->
                viewModel.updateOrder(
                    order.copy(
                        paymentStatus = PaymentStatus.PAID,
                        paidAmount = order.totalAmount,
                        updatedAt = System.currentTimeMillis()
                    ),
                    requireContext()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OperationState.Success -> {
                    if (state.message == "Order deleted") {
                        findNavController().navigateUp()
                    } else {
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                        loadOrder()
                    }
                    viewModel.resetState()
                }
                is OperationState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
