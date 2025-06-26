package com.buupass.quickshuttle.data.repositories

import com.buupass.quickshuttle.data.models.payment.InitiateBookingRequest
import com.buupass.quickshuttle.data.models.payment.MpesaSTKPushResponse
import com.buupass.quickshuttle.data.models.payment.PaymentResponse
import com.buupass.quickshuttle.data.network.PassengerAPIService
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.network.safeApiCall
import com.buupass.quickshuttle.utils.PaymentErrors
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay

class PaymentRepository(
    private val passengerApiService: PassengerAPIService,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun initiateBooking(initiateBookingRequest: InitiateBookingRequest): NetworkResult<PaymentResponse> {
        return safeApiCall(dispatcher) {
            val response = passengerApiService.initiateBooking(initiateBookingRequest)
            when (response.status) {
                HttpStatusCode.OK -> {
                    when (initiateBookingRequest.payment_type) {
                        PaymentType.Mpesa.name -> {
                            val result = response.body<MpesaSTKPushResponse>()

                            val isPushSent = result.success ?: result.status ?: false

                            if (!isPushSent) {
                                return@safeApiCall NetworkResult.Error(result.message)
                            }

                            val bookingId = result.data?.booking_id
                                ?: return@safeApiCall NetworkResult.Error(
                                    PaymentErrors.BOOKING_NOT_FOUND_ERROR
                                )

                            val timeoutMillis = 5000L // Maximum polling time (5 seconds)
                            val interval = 2500L // Polling interval (2.5 seconds)
                            var timeElapsed = 0L

                            // Polling for payment status
                            while (timeElapsed < timeoutMillis) {
                                val paymentStatusResult =   confirmMpesaPayment(bookingId)
                                if (paymentStatusResult is NetworkResult.Success && paymentStatusResult.data.status) {
                                    return@safeApiCall NetworkResult.Success(
                                        paymentStatusResult.data
                                    )
                                }

                                delay(interval)
                                timeElapsed += interval
                            }

                            // If payment isn't completed within timeout
                            return@safeApiCall NetworkResult.Error(
                                message = PaymentErrors.PAYMENT_TIME_OUT_ERROR,
                                extra = mapOf("booking_id" to bookingId)
                            )
                        }

                        PaymentType.Cash.name -> {
                            val result = response.body<PaymentResponse>()
                            if (!result.status || result.data == null) {
                                return@safeApiCall NetworkResult.Error(result.message)
                            }
                            return@safeApiCall NetworkResult.Success(result)
                        }

                        else -> {
                            return@safeApiCall NetworkResult.Error("Wrong Payment Type")
                        }
                    }
                }

                else -> {
                    return@safeApiCall NetworkResult.Error("An error occurred")
                }
            }
        }
    }

    suspend fun confirmMpesaPayment(bookingId: String): NetworkResult<PaymentResponse> {
        return safeApiCall(dispatcher) {
            val response = passengerApiService.confirmMpesaPayment(bookingId)
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<PaymentResponse>()
                    if (!result.status || result.data == null) {
                        return@safeApiCall NetworkResult.Error(result.message)
                    }
                    return@safeApiCall NetworkResult.Success(result)
                }

                else -> {
                    return@safeApiCall NetworkResult.Error("Something is wrong")
                }
            }
        }
    }

    companion object {
        enum class PaymentType {
            Mpesa,
            Cash
        }
    }
}