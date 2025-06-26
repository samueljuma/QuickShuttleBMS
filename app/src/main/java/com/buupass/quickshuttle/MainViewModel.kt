package com.buupass.quickshuttle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.utils.NetworkMonitor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val networkMonitor: NetworkMonitor
): ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isConnected
        .map { it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(2000),
            initialValue = true
        )

    override fun onCleared() {
        super.onCleared()
        networkMonitor.unregisterCallback()
    }
}