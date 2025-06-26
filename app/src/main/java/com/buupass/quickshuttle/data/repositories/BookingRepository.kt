package com.buupass.quickshuttle.data.repositories

import android.util.Log
import com.buupass.quickshuttle.data.models.booking.BookingsResponse
import com.buupass.quickshuttle.data.models.booking.CitiesResponse
import com.buupass.quickshuttle.data.models.booking.ReserveSeatsRequest
import com.buupass.quickshuttle.data.models.booking.ReserveSeatsResponse
import com.buupass.quickshuttle.data.models.booking.SchedulesResponse
import com.buupass.quickshuttle.data.models.booking.SeatAvailabilityResponse
import com.buupass.quickshuttle.data.models.reprint.FetchTicketResponse
import com.buupass.quickshuttle.data.network.PassengerAPIService
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.network.safeApiCall
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CoroutineDispatcher

class BookingRepository(
    private val passengerApiService: PassengerAPIService,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun getCities(): NetworkResult<CitiesResponse> {
        return safeApiCall(dispatcher) {
            val response = passengerApiService.getCities()
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<CitiesResponse>()
                    if (!result.status) {
                        return@safeApiCall NetworkResult.Error(result.message)
                    }
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error("An error occurred ${response.status}")
                }
            }
        }
    }

    suspend fun getSchedules(
        pickUpPoint: Int,
        dropOffPoint: Int,
        travelDate: String
    ): NetworkResult<SchedulesResponse> {
        return safeApiCall(dispatcher) {
            val response = passengerApiService.getSchedules(pickUpPoint, dropOffPoint, travelDate)
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<SchedulesResponse>()
                    if (!result.status) {
                        return@safeApiCall NetworkResult.Error(result.message)
                    }
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error("An error occurred")
                }
            }
        }
    }

    suspend fun getAvailableSeats(
        pickUpPoint: Int,
        dropOffPoint: Int,
        travelDate: String,
        scheduleId: Int
    ): NetworkResult<SeatAvailabilityResponse> {
        return safeApiCall(dispatcher) {
            val response = passengerApiService.getAvailableSeats(
                pickUpPoint,
                dropOffPoint,
                travelDate,
                scheduleId
            )
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<SeatAvailabilityResponse>()
                    if (!result.status) {
                        return@safeApiCall NetworkResult.Error(result.message)
                    }
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error("An error occurred")
                }
            }
        }
    }

    suspend fun reserveSeats(reserveSeatsRequest: ReserveSeatsRequest): NetworkResult<ReserveSeatsResponse> {
        return safeApiCall(dispatcher) {
            val response = passengerApiService.reserveSeats(reserveSeatsRequest)
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<ReserveSeatsResponse>()
                    if (!result.status) {
                        return@safeApiCall NetworkResult.Error(result.message)
                    }
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error("There was an Error!")
                }
            }
        }
    }

    suspend fun getPastBookings(date: String, userId: Int): NetworkResult<BookingsResponse> {
        return safeApiCall(dispatcher) {
            val response = passengerApiService.getPastBookings(date, userId)
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<BookingsResponse>()
                    if (!result.status) {
                        return@safeApiCall NetworkResult.Error(result.message)
                    }
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error("There was an Error!")
                }
            }
        }
    }

    suspend fun fetchTicketsForReprint(bookingId: String): NetworkResult<FetchTicketResponse> {
        return safeApiCall(dispatcher) {
            val response = passengerApiService.fetchTicketsForReprint(bookingId)
            when (response.status) {
                HttpStatusCode.OK -> {
                    try {
                        val result = response.body<FetchTicketResponse>()
                        if (!result.status) {
                            return@safeApiCall NetworkResult.Error(result.message)
                        }
                        NetworkResult.Success(result)
                    } catch (e: JsonConvertException) {
                        NetworkResult.Error("$bookingId does not exist")
                    }
                }
                else -> {
                    Log.d("BookingRepository", "fetchTicketsForReprint: ${response.status}")
                    NetworkResult.Error("There was an Error! ")
                }
            }
        }
    }
}