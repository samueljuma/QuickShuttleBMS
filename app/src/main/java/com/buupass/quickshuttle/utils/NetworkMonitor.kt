package com.buupass.quickshuttle.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isConnected = MutableStateFlow(checkInitialConnection())
    val isConnected = _isConnected.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val networkCallback = object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            updateConnection()
        }
        override fun onLost(network: Network) {
            updateConnection()
        }
        override fun onCapabilitiesChanged( network: Network, networkCapabilities: NetworkCapabilities) {
            updateConnection()
        }
    }

    init {
        registerCallback()
    }

    private fun checkInitialConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun updateConnection() {
        coroutineScope.launch {
            _isConnected.emit(checkInitialConnection())
        }
    }

    fun registerCallback(){
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    fun unregisterCallback(){
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}