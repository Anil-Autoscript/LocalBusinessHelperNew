package com.localbusiness.helper.ui.customers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.localbusiness.helper.data.local.entity.Customer
import com.localbusiness.helper.data.repository.BusinessRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CustomersViewModel(private val repository: BusinessRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    val customers: LiveData<List<Customer>> = _searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.getAllCustomers().asLiveData()
        } else {
            repository.searchCustomers(query).asLiveData()
        }
    }

    private val _operationState = MutableLiveData<OperationState>(OperationState.Idle)
    val operationState: LiveData<OperationState> = _operationState

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.saveCustomer(customer)
                _operationState.value = OperationState.Success("Customer saved!")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to save: ${e.message}")
            }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.updateCustomer(customer)
                _operationState.value = OperationState.Success("Customer updated!")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to update: ${e.message}")
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.deleteCustomer(customer)
                _operationState.value = OperationState.Success("Customer deleted")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error("Failed to delete: ${e.message}")
            }
        }
    }

    fun resetState() {
        _operationState.value = OperationState.Idle
    }
}

sealed class OperationState {
    object Idle : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}
