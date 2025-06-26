package com.buupass.quickshuttle.data.repositories

import com.buupass.quickshuttle.data.models.booking.FetchPassengerParams
import com.buupass.quickshuttle.data.models.onboardingpassenger.ManifestTripListResponse
import com.buupass.quickshuttle.data.models.onboardingpassenger.ManifestTripsRequestParams
import com.buupass.quickshuttle.data.models.onboardingpassenger.OnboardPassengerRequest
import com.buupass.quickshuttle.data.models.onboardingpassenger.OnboardPassengerResponse
import com.buupass.quickshuttle.data.models.onboardingpassenger.PassengersToOnboardResponse
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.network.PassengerAPIService
import com.buupass.quickshuttle.data.network.safeApiCall
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher

class PassengerCheckInRepository(
    private val passengerAPIService: PassengerAPIService,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun fetchManifestTrips(params: ManifestTripsRequestParams): NetworkResult<ManifestTripListResponse>{
        return safeApiCall(dispatcher){
            val response = passengerAPIService.fetchManifestTrips(params)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<ManifestTripListResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }
        }

    }
    suspend fun fetchPassengerList(params: FetchPassengerParams): NetworkResult<PassengersToOnboardResponse> {
        return safeApiCall(dispatcher){
            val response = passengerAPIService.fetchPassengerList(params)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<PassengersToOnboardResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }

        }
    }

    suspend fun onboardPassenger(request: OnboardPassengerRequest): NetworkResult<OnboardPassengerResponse> {
        return safeApiCall(dispatcher){
            val response = passengerAPIService.onboardPassenger(request)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<OnboardPassengerResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }
        }

    }


}