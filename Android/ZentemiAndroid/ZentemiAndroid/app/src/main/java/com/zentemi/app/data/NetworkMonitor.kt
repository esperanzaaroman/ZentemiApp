package com.zentemi.app.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Conexión a internet en tiempo real (equivalente a NetworkMonitor.swift).
 * Se usa para sugerir crear cuenta solo con internet y para reintentar la
 * vinculación con Firestore cuando vuelve la red.
 */
object NetworkMonitor {

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var registered = false

    /** Se llama una vez al abrir la app (MainActivity). */
    fun init(context: Context) {
        if (registered) return
        val manager = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        registered = true

        _isOnline.value = manager.activeNetwork?.let { network ->
            manager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val connected = mutableSetOf<Network>()
        runCatching {
            manager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    synchronized(connected) { connected += network }
                    _isOnline.value = true
                }

                override fun onLost(network: Network) {
                    val empty = synchronized(connected) {
                        connected -= network
                        connected.isEmpty()
                    }
                    if (empty) _isOnline.value = false
                }
            })
        }
    }
}
