package com.buupass.quickshuttle.data.repositories

import android.util.Log
import com.buupass.quickshuttle.data.models.auth.LoginResponse
import com.buupass.quickshuttle.data.models.auth.User
import com.buupass.quickshuttle.data.network.PassengerAPIService
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.session.SessionManager
import com.buupass.quickshuttle.domain.auth.UserDomain
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthRepository(
    private val passengerApiService: PassengerAPIService,
    private val dispatcher: CoroutineDispatcher,
    private val sessionManager: SessionManager

) {
    suspend fun login(user: User): NetworkResult<LoginResponse> {
        return withContext(dispatcher) {
            try {
                val response = passengerApiService.login(user)

                when (response.status) {
                    HttpStatusCode.OK -> {
                        // Handle successful response
                        val result = response.body<LoginResponse>()

                        // If the status is false, return an error
                        if (!result.status) {
                            return@withContext NetworkResult.Error(result.message)
                        }

                        // Proceed to save the user details and return success
                        val loggedInUser = result.data
                        if (loggedInUser != null) {
                            sessionManager.saveLoggedInUserDetails(user = loggedInUser)
                        }

                        NetworkResult.Success(result)
                    }
                    else -> {
                        NetworkResult.Error("Wrong Email or Password")
                    }
                }
            } catch (e: UnresolvedAddressException) {
                NetworkResult.Error("No Internet Connection")
            } catch (e: HttpRequestTimeoutException) {
                NetworkResult.Error("Request Timeout Error")
            } catch (e: ResponseException) {
                val error = when (e.response.status.value) {
                    503 -> "Service Temporarily Unavailable"
                    500 -> "Internal Server Error"
                    404 -> "Resource Not Found"
                    else -> "Unexpected error: ${e.response.status}"
                }
                NetworkResult.Error(error)
            } catch (e: java.nio.channels.UnresolvedAddressException) {
                NetworkResult.Error("Something is wrong, Check internet and try again")
            } catch (e: Exception) {
                when(e.message){
                    "Connection reset by peer" ->{
                        NetworkResult.Error("You have no internet connection.Please check your connection and try again")
                    }
                    else ->{
                        Log.d("API ERRORS", "safeApiCall: ${e.message}")
                        NetworkResult.Error("An error occurred")
                    }
                }
            }
        }
    }

    fun getAuthToken(): String {
        return sessionManager.getAuthToken()
    }

    fun getUserDetails(): UserDomain {
        return sessionManager.getUserDetails()
    }

    fun clearAllUserDetails() {
        sessionManager.clearAllUserDetails()
    }

    fun logoutUser() {
        clearAllUserDetails()
    }
}