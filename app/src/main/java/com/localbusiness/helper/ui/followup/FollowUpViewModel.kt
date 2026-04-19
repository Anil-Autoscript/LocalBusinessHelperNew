package com.localbusiness.helper.ui.followup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.localbusiness.helper.data.repository.BusinessRepository

class FollowUpViewModel(private val repository: BusinessRepository) : ViewModel() {
    val todayFollowUps = repository.getTodayFollowUps().asLiveData()
    val allFollowUps = repository.getAllOrders().asLiveData()
}
