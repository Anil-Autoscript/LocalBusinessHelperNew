package com.localbusiness.helper.ui.orders

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.localbusiness.helper.R
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.databinding.FragmentOrdersBinding
import com.localbusiness.helper.ui.ViewModelFactory
import com.localbusiness.helper.ui.customers.OperationState

class OrdersFragment : Fragment(), OrderAdapter.OrderClickListener {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels { ViewModelFactory(requireContext()) }
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = OrderAdapter(this)
        binding.rvOrders.adapter = adapter
        setupMenu()
        setupObservers()
        binding.fabAddOrder.setOnClickListener { findNavController().navigate(R.id.addOrderFragment) }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_search, menu)
                val sv = menu.findItem(R.id.action_search).actionView as SearchView
                sv.queryHint = "Search orders..."
                sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(q: String?) = true
                    override fun onQueryTextChange(t: String?): Boolean {
                        viewModel.setSearchQuery(t ?: ""); return true
                    }
                })
            }
            override fun onMenuItemSelected(item: MenuItem) = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupObservers() {
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            adapter.submitList(orders)
            binding.emptyState.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
            binding.rvOrders.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
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

    override fun onOrderClick(order: Order) {
        val bundle = Bundle().apply { putLong("orderId", order.id) }
        findNavController().navigate(R.id.orderDetailFragment, bundle)
    }

    override fun onOrderEdit(order: Order) {
        val bundle = Bundle().apply { putLong("orderId", order.id) }
        findNavController().navigate(R.id.addOrderFragment, bundle)
    }

    override fun onOrderDelete(order: Order) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Order")
            .setMessage("Delete order for ${order.customerName}?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteOrder(order, requireContext()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
