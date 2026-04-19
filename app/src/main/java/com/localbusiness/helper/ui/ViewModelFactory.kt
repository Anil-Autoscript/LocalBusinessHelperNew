package com.localbusiness.helper.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.ui.customers.CustomersViewModel
import com.localbusiness.helper.ui.dashboard.DashboardViewModel
import com.localbusiness.helper.ui.followup.FollowUpViewModel
import com.localbusiness.helper.ui.invoices.InvoicesViewModel
import com.localbusiness.helper.ui.orders.OrdersViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val repository by lazy { BusinessRepository(context) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(CustomersViewModel::class.java) ->
                CustomersViewModel(repository) as T
            modelClass.isAssignableFrom(OrdersViewModel::class.java) ->
                OrdersViewModel(repository) as T
            modelClass.isAssignableFrom(InvoicesViewModel::class.java) ->
                InvoicesViewModel(repository) as T
            modelClass.isAssignableFrom(FollowUpViewModel::class.java) ->
                FollowUpViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
