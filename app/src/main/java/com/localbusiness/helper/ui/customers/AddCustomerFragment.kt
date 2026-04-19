package com.localbusiness.helper.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.localbusiness.helper.data.local.entity.Customer
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.databinding.FragmentAddCustomerBinding
import com.localbusiness.helper.ui.ViewModelFactory
import kotlinx.coroutines.launch

class AddCustomerFragment : Fragment() {

    private var _binding: FragmentAddCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomersViewModel by viewModels { ViewModelFactory(requireContext()) }
    private var customerId: Long = 0L
    private var existingCustomer: Customer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customerId = arguments?.getLong("customerId", 0L) ?: 0L

        if (customerId != 0L) {
            requireActivity().title = "Edit Customer"
            loadCustomer()
        } else {
            requireActivity().title = "Add Customer"
        }

        setupObservers()
        setupClickListeners()
    }

    private fun loadCustomer() {
        lifecycleScope.launch {
            val repo = BusinessRepository(requireContext())
            existingCustomer = repo.getCustomerById(customerId)
            existingCustomer?.let { customer ->
                binding.etName.setText(customer.name)
                binding.etPhone.setText(customer.phone)
                binding.etEmail.setText(customer.email)
                binding.etAddress.setText(customer.address)
                binding.etNotes.setText(customer.notes)
            }
        }
    }

    private fun setupObservers() {
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OperationState.Success -> {
                    findNavController().navigateUp()
                }
                is OperationState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener { saveCustomer() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }

    private fun saveCustomer() {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return
        }
        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone is required"
            return
        }

        binding.tilName.error = null
        binding.tilPhone.error = null

        val customer = Customer(
            id = customerId,
            name = name,
            phone = phone,
            email = binding.etEmail.text?.toString()?.trim() ?: "",
            address = binding.etAddress.text?.toString()?.trim() ?: "",
            notes = binding.etNotes.text?.toString()?.trim() ?: "",
            syncId = existingCustomer?.syncId ?: "",
            createdAt = existingCustomer?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        if (customerId != 0L) {
            viewModel.updateCustomer(customer)
        } else {
            viewModel.saveCustomer(customer)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
