package com.localbusiness.helper.ui.orders

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.localbusiness.helper.data.local.entity.*
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.databinding.FragmentAddOrderBinding
import com.localbusiness.helper.ui.ViewModelFactory
import com.localbusiness.helper.ui.customers.OperationState
import com.localbusiness.helper.utils.DateUtils
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.Calendar

class AddOrderFragment : Fragment() {

    private var _binding: FragmentAddOrderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels { ViewModelFactory(requireContext()) }

    private var orderId: Long = 0L
    private var existingOrder: Order? = null
    private var selectedOrderDate: Long = System.currentTimeMillis()
    private var selectedDeliveryDate: Long = System.currentTimeMillis()
    private var selectedFollowUpDate: Long = 0L
    private var customers: List<com.localbusiness.helper.data.local.entity.Customer> = emptyList()
    private var selectedCustomerId: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderId = arguments?.getLong("orderId", 0L) ?: 0L

        setupSpinners()
        setupDatePickers()
        setupClickListeners()
        setupObservers()
        loadCustomers()

        if (orderId != 0L) {
            requireActivity().title = "Edit Order"
            loadOrder()
        } else {
            requireActivity().title = "Add Order"
            updateDateDisplay()
        }
    }

    private fun setupSpinners() {
        // Order status
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            OrderStatus.values().map { it.displayName }
        )
        binding.spinnerStatus.setAdapter(statusAdapter)
        binding.spinnerStatus.setText(OrderStatus.PENDING.displayName, false)

        // Payment status
        val payAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PaymentStatus.values().map { it.displayName }
        )
        binding.spinnerPayment.setAdapter(payAdapter)
        binding.spinnerPayment.setText(PaymentStatus.UNPAID.displayName, false)
    }

    private fun loadCustomers() {
        lifecycleScope.launch {
            val repo = BusinessRepository(requireContext())
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.getAllCustomers().collect { list ->
                    customers = list
                    val names = list.map { it.name }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        names
                    )
                    binding.spinnerCustomer.setAdapter(adapter)
                    binding.spinnerCustomer.setOnItemClickListener { _, _, pos, _ ->
                        selectedCustomerId = list[pos].id
                    }
                    existingOrder?.let { order ->
                        val idx = list.indexOfFirst { it.id == order.customerId }
                        if (idx >= 0) {
                            binding.spinnerCustomer.setText(list[idx].name, false)
                            selectedCustomerId = list[idx].id
                        }
                    }
                }
            }
        }
    }

    private fun loadOrder() {
        lifecycleScope.launch {
            val repo = BusinessRepository(requireContext())
            existingOrder = repo.getOrderById(orderId)
            existingOrder?.let { order ->
                binding.etProduct.setText(order.product)
                binding.etQuantity.setText(order.quantity.toString())
                binding.etPrice.setText(order.price.toString())
                binding.etNotes.setText(order.notes)
                binding.spinnerStatus.setText(order.status.displayName, false)
                binding.spinnerPayment.setText(order.paymentStatus.displayName, false)
                binding.etPaidAmount.setText(order.paidAmount.toString())
                selectedOrderDate = order.orderDate
                selectedDeliveryDate = order.deliveryDate
                selectedFollowUpDate = order.followUpDate
                updateDateDisplay()
            }
        }
    }

    private fun setupDatePickers() {
        binding.etOrderDate.setOnClickListener { showDatePicker { ts -> selectedOrderDate = ts; updateDateDisplay() } }
        binding.etDeliveryDate.setOnClickListener { showDatePicker { ts -> selectedDeliveryDate = ts; updateDateDisplay() } }
        binding.etFollowUpDate.setOnClickListener { showDatePicker { ts -> selectedFollowUpDate = ts; updateDateDisplay() } }
    }

    private fun showDatePicker(onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = LocalDate.of(year, month + 1, day)
                onSelected(DateUtils.localDateToLong(date))
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        binding.etOrderDate.setText(DateUtils.formatDate(selectedOrderDate).ifEmpty { "Select date" })
        binding.etDeliveryDate.setText(DateUtils.formatDate(selectedDeliveryDate).ifEmpty { "Select date" })
        binding.etFollowUpDate.setText(DateUtils.formatDate(selectedFollowUpDate).ifEmpty { "None" })
    }

    private fun setupObservers() {
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OperationState.Success -> findNavController().navigateUp()
                is OperationState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener { saveOrder() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }

    private fun saveOrder() {
        val product = binding.etProduct.text?.toString()?.trim() ?: ""
        val quantityStr = binding.etQuantity.text?.toString()?.trim() ?: ""
        val priceStr = binding.etPrice.text?.toString()?.trim() ?: ""
        val customerName = binding.spinnerCustomer.text?.toString()?.trim() ?: ""

        if (customerName.isEmpty()) { binding.tilCustomer.error = "Select a customer"; return }
        if (product.isEmpty()) { binding.tilProduct.error = "Product is required"; return }
        if (quantityStr.isEmpty()) { binding.tilQuantity.error = "Quantity required"; return }
        if (priceStr.isEmpty()) { binding.tilPrice.error = "Price required"; return }

        binding.tilCustomer.error = null
        binding.tilProduct.error = null
        binding.tilQuantity.error = null
        binding.tilPrice.error = null

        val quantity = quantityStr.toIntOrNull() ?: 1
        val price = priceStr.toDoubleOrNull() ?: 0.0
        val paidAmount = binding.etPaidAmount.text?.toString()?.toDoubleOrNull() ?: 0.0
        val statusName = binding.spinnerStatus.text?.toString() ?: OrderStatus.PENDING.displayName
        val paymentName = binding.spinnerPayment.text?.toString() ?: PaymentStatus.UNPAID.displayName
        val status = OrderStatus.values().firstOrNull { it.displayName == statusName } ?: OrderStatus.PENDING
        val payment = PaymentStatus.values().firstOrNull { it.displayName == paymentName } ?: PaymentStatus.UNPAID

        val order = Order(
            id = orderId,
            customerId = selectedCustomerId,
            customerName = customerName,
            product = product,
            quantity = quantity,
            price = price,
            totalAmount = quantity * price,
            orderDate = selectedOrderDate,
            deliveryDate = selectedDeliveryDate,
            followUpDate = selectedFollowUpDate,
            status = status,
            paymentStatus = payment,
            paidAmount = paidAmount,
            notes = binding.etNotes.text?.toString()?.trim() ?: "",
            syncId = existingOrder?.syncId ?: "",
            createdAt = existingOrder?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        if (orderId != 0L) viewModel.updateOrder(order, requireContext())
        else viewModel.saveOrder(order, requireContext())
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
