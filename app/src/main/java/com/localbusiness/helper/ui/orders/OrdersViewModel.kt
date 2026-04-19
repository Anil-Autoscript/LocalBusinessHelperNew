package com.localbusiness.helper.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.localbusiness.helper.data.local.entity.Order
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.ui.customers.OperationState
import com.localbusiness.helper.utils.NotificationReceiver
import kotlinx.coroutines.launch

class OrdersViewModel(private val repository: BusinessRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData("")

    val orders: LiveData<List<Order>> = _searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.getAllOrders().asLiveData()
        } else {
            repository.searchOrders(query).asLiveData()
        }
    }

    private val _operationState = MutableLiveData<OperationState>(OperationState.Idle)
    val operationState: LiveData<OperationState> = _operationState

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun saveOrder(order: Order, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val id = repository.saveOrder(order)
                if (order.followUpDate > 0L) {
                    NotificationReceiver.scheduleFollowUp(
                        context, id, order.customerName, order.product, order.followUpDate
                    )
                }
                _operationState.value = OperationState.Success("Order saved!")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed: ${e.message}")
            }
        }
    }

    fun updateOrder(order: Order, context: android.content.Context) {
        viewModelScope.launch {
            try {
                repository.updateOrder(order)
                NotificationReceiver.cancelFollowUp(context, order.id)
                if (order.followUpDate > 0L) {
                    NotificationReceiver.scheduleFollowUp(
                        context, order.id, order.customerName, order.product, order.followUpDate
                    )
                }
                _operationState.value = OperationState.Success("Order updated!")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed: ${e.message}")
            }
        }
    }

    fun deleteOrder(order: Order, context: android.content.Context) {
        viewModelScope.launch {
            try {
                NotificationReceiver.cancelFollowUp(context, order.id)
                repository.deleteOrder(order)
                _operationState.value = OperationState.Success("Order deleted")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed: ${e.message}")
            }
        }
    }

    fun resetState() { _operationState.value = OperationState.Idle }
}
