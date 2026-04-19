package com.localbusiness.helper.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.localbusiness.helper.R
import com.localbusiness.helper.databinding.FragmentDashboardBinding
import com.localbusiness.helper.ui.ViewModelFactory
import com.localbusiness.helper.utils.DateUtils
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.totalOrders.observe(viewLifecycleOwner) { count ->
            binding.tvTotalOrders.text = count?.toString() ?: "0"
        }
        viewModel.pendingOrders.observe(viewLifecycleOwner) { count ->
            binding.tvPendingOrders.text = count?.toString() ?: "0"
        }
        viewModel.pendingAmount.observe(viewLifecycleOwner) { amount ->
            binding.tvPendingAmount.text = currencyFormat.format(amount ?: 0.0)
        }
        viewModel.todayFollowUps.observe(viewLifecycleOwner) { list ->
            val count = list?.size ?: 0
            binding.tvFollowUps.text = count.toString()
            binding.cardFollowUps.strokeColor =
                if (count > 0) requireContext().getColor(R.color.warning)
                else requireContext().getColor(R.color.surface_border)
        }
        viewModel.totalCollected.observe(viewLifecycleOwner) { amount ->
            binding.tvCollected.text = currencyFormat.format(amount ?: 0.0)
        }
        viewModel.syncState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SyncState.Loading -> {
                    binding.btnSync.isEnabled = false
                    binding.syncProgress.visibility = View.VISIBLE
                    binding.btnSync.text = "Syncing..."
                }
                is SyncState.Success -> {
                    binding.btnSync.isEnabled = true
                    binding.syncProgress.visibility = View.GONE
                    binding.btnSync.text = "Sync Now"
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                }
                is SyncState.Error -> {
                    binding.btnSync.isEnabled = true
                    binding.syncProgress.visibility = View.GONE
                    binding.btnSync.text = "Sync Now"
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Settings") {
                            findNavController().navigate(R.id.settingsFragment)
                        }.show()
                }
                else -> {
                    binding.btnSync.isEnabled = true
                    binding.syncProgress.visibility = View.GONE
                    binding.btnSync.text = "Sync Now"
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSync.setOnClickListener { viewModel.syncNow() }
        binding.cardTotalOrders.setOnClickListener {
            findNavController().navigate(R.id.ordersFragment)
        }
        binding.cardPendingOrders.setOnClickListener {
            findNavController().navigate(R.id.ordersFragment)
        }
        binding.cardPendingAmount.setOnClickListener {
            findNavController().navigate(R.id.invoicesFragment)
        }
        binding.cardFollowUps.setOnClickListener {
            findNavController().navigate(R.id.followUpFragment)
        }
        binding.fabAddOrder.setOnClickListener {
            findNavController().navigate(R.id.addOrderFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
