package com.localbusiness.helper.ui.followup

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.localbusiness.helper.R
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.databinding.FragmentFollowUpBinding
import com.localbusiness.helper.ui.ViewModelFactory
import com.localbusiness.helper.ui.orders.OrderAdapter
import com.localbusiness.helper.utils.DateUtils

class FollowUpFragment : Fragment(), OrderAdapter.OrderClickListener {

    private var _binding: FragmentFollowUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FollowUpViewModel by viewModels { ViewModelFactory(requireContext()) }
    private lateinit var todayAdapter: OrderAdapter
    private lateinit var upcomingAdapter: OrderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFollowUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        todayAdapter = OrderAdapter(this)
        upcomingAdapter = OrderAdapter(this)
        binding.rvTodayFollowUps.adapter = todayAdapter
        binding.rvUpcoming.adapter = upcomingAdapter

        viewModel.todayFollowUps.observe(viewLifecycleOwner) { orders ->
            todayAdapter.submitList(orders)
            binding.tvTodayCount.text = "Today (${orders.size})"
            binding.emptyToday.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.allFollowUps.observe(viewLifecycleOwner) { orders ->
            val now = System.currentTimeMillis()
            val upcoming = orders.filter { it.followUpDate > now }
                .sortedBy { it.followUpDate }
            upcomingAdapter.submitList(upcoming)
            binding.tvUpcomingCount.text = "Upcoming (${upcoming.size})"
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
