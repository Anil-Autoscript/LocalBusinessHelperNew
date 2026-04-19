package com.localbusiness.helper.ui.invoices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.repository.BusinessRepository

class InvoicesViewModel(private val repository: BusinessRepository) : ViewModel() {

    private val _filter = MutableLiveData("ALL")

    val orders: LiveData<List<Order>> = _filter.switchMap { filter ->
        when (filter) {
            "UNPAID" -> repository.searchOrders("").asLiveData() // filtered below
            else -> repository.getAllOrders().asLiveData()
        }
    }

    val pendingAmount = repository.getTotalPendingAmount()
    val totalCollected = repository.getTotalCollected()
    val unpaidCount = repository.getUnpaidOrderCount()

    fun setFilter(filter: String) { _filter.value = filter }
}
