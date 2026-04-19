package com.localbusiness.helper.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.localbusiness.helper.R
import com.localbusiness.helper.data.local.entity.Customer
import com.localbusiness.helper.databinding.FragmentCustomersBinding
import com.localbusiness.helper.ui.ViewModelFactory

class CustomersFragment : Fragment(), CustomerAdapter.CustomerClickListener {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomersViewModel by viewModels { ViewModelFactory(requireContext()) }
    private lateinit var adapter: CustomerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupMenu()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter(this)
        binding.rvCustomers.adapter = adapter
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_search, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "Search customers..."
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = true
                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.setSearchQuery(newText ?: "")
                        return true
                    }
                })
            }
            override fun onMenuItemSelected(menuItem: MenuItem) = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupObservers() {
        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            adapter.submitList(customers)
            binding.emptyState.visibility = if (customers.isEmpty()) View.VISIBLE else View.GONE
            binding.rvCustomers.visibility = if (customers.isEmpty()) View.GONE else View.VISIBLE
        }
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OperationState.Success -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
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

    private fun setupClickListeners() {
        binding.fabAddCustomer.setOnClickListener {
            findNavController().navigate(R.id.addCustomerFragment)
        }
    }

    override fun onCustomerClick(customer: Customer) {
        val bundle = Bundle().apply { putLong("customerId", customer.id) }
        findNavController().navigate(R.id.customerDetailFragment, bundle)
    }

    override fun onCustomerEdit(customer: Customer) {
        val bundle = Bundle().apply { putLong("customerId", customer.id) }
        findNavController().navigate(R.id.addCustomerFragment, bundle)
    }

    override fun onCustomerDelete(customer: Customer) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Customer")
            .setMessage("Delete ${customer.name}? All orders will also be deleted.")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteCustomer(customer) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
