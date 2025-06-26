package com.buupass.quickshuttle.data.network

import android.util.Log
import com.buupass.quickshuttle.utils.NetworkErrors
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import java.nio.channels.UnresolvedAddressException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> NetworkResult<T>
): NetworkResult<T> {
    return withContext(dispatcher) {
        try {
            apiCall()
        } catch (e: HttpRequestTimeoutException) {
            NetworkResult.Error("Request Timeout Error")
        } catch (e: ResponseException) {
            val error = when (e.response.status.value) {
                503 -> "Service Temporarily Unavailable"
                500 -> NetworkErrors.INTERNAL_SERVER_ERROR
                404 -> "Resource Not Found"
                else -> "Unexpected error: ${e.response.status}"
            }
            NetworkResult.Error(error)
        } catch (e: UnresolvedAddressException) {
            NetworkResult.Error("Something is wrong, Check internet and try again")
        } catch (e: Exception) {
            when(e.message){
                "Connection reset by peer" ->{
                    NetworkResult.Error("You have no internet connection.Turn on Internet and press Ok to continue")
                }
                else ->{
                    Log.d("API ERRORS", "safeApiCall: ${e.message}")
                    NetworkResult.Error("An error occurred")
                }
            }

        }
    }
}