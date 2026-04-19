package com.localbusiness.helper.ui.dashboard

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.data.repository.Result
import com.localbusiness.helper.workers.SheetsSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(private val repository: BusinessRepository) : ViewModel() {

    val totalOrders = repository.getTotalOrderCount()
    val pendingOrders = repository.getPendingOrderCount()
    val pendingAmount = repository.getTotalPendingAmount()
    val todayFollowUps = repository.getTodayFollowUpsLive()
    val unpaidCount = repository.getUnpaidOrderCount()
    val totalCollected = repository.getTotalCollected()

    private val _syncState = MediatorLiveData<SyncState>()
    val syncState: MediatorLiveData<SyncState> = _syncState

    fun triggerManualSync(workManager: WorkManager) {
        _syncState.value = SyncState.Loading

        val request = OneTimeWorkRequestBuilder<SheetsSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueue(request)

        workManager.getWorkInfoByIdLiveData(request.id).observeForever { info ->
            when (info?.state) {
                WorkInfo.State.SUCCEEDED -> _syncState.value = SyncState.Success("Sync complete!")
                WorkInfo.State.FAILED -> _syncState.value = SyncState.Error("Sync failed. Check settings.")
                WorkInfo.State.CANCELLED -> _syncState.value = SyncState.Idle
                else -> {}
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            when (val result = withContext(Dispatchers.IO) { repository.syncFromGoogleSheets() }) {
                is Result.Success -> _syncState.value = SyncState.Success("Synced ${result.data} records")
                is Result.Error -> _syncState.value = SyncState.Error(result.message)
                else -> {}
            }
        }
    }
}

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}
