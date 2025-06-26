package com.buupass.quickshuttle.data.network

import com.buupass.quickshuttle.data.models.auth.User
import com.buupass.quickshuttle.data.models.booking.FetchPassengerParams
import com.buupass.quickshuttle.data.models.booking.ReserveSeatsRequest
import com.buupass.quickshuttle.data.models.onboardingpassenger.ManifestTripsRequestParams
import com.buupass.quickshuttle.data.models.onboardingpassenger.OnboardPassengerRequest
import com.buupass.quickshuttle.data.models.payment.InitiateBookingRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

val apiHeaders: List<Pair<String, Any>> = listOf(
    "BMSAndroidVersion" to 2,
    "APIClient" to "android_bms"
)

class PassengerAPIService(private val mClient: HttpClient) {

    suspend fun login(user: User): HttpResponse {
        return mClient
            .post("accounts/login/") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
    }

    suspend fun getCities(): HttpResponse = mClient.get("destinations/")

    suspend fun getSchedules(pickUpPoint: Int, dropOffPoint: Int, travelDate: String): HttpResponse {
        return mClient.get("bms/schedules/") {
            url {
                parameters.append("pickup_point", "$pickUpPoint")
                parameters.append("drop_off_point", "$dropOffPoint")
                parameters.append("travel_date", travelDate)
            }
        }
    }

    suspend fun getAvailableSeats(
        pickUpPoint: Int,
        dropOffPoint: Int,
        travelDate: String,
        scheduleId: Int
    ): HttpResponse {
        return mClient.get("bms/schedule/") {
            url {
                parameters.append("pickup_point", "$pickUpPoint")
                parameters.append("drop_off_point", "$dropOffPoint")
                parameters.append("travel_date", travelDate)
                parameters.append("trip_schedule_id", "$scheduleId")
            }
        }
    }

    suspend fun initiateBooking(initiateBookingRequest: InitiateBookingRequest): HttpResponse {
        return mClient.post("bms/booking/") {
            contentType(ContentType.Application.Json)
            setBody(initiateBookingRequest)
        }
    }

    suspend fun confirmMpesaPayment(bookingId: String): HttpResponse {
        return mClient.get("confirm/booking/") {
            url {
                parameters.append("booking_id", bookingId)
            }
        }
    }

    suspend fun reserveSeats(reserveSeatsRequest: ReserveSeatsRequest): HttpResponse {
        return mClient.post("reserve/") {
            contentType(ContentType.Application.Json)
            setBody(reserveSeatsRequest)
        }
    }

    suspend fun getPastBookings(date: String, userId: Int): HttpResponse {
        return mClient.get("user/past-bookings/") {
            url {
                parameters.append("booking_date", date)
                parameters.append("user_id", "$userId")
            }
        }
    }

    suspend fun fetchTicketsForReprint(bookingId: String): HttpResponse {
        return mClient.get("bms/api_reprint") {
            url {
                parameters.append("booking_id", bookingId)
            }
        }
    }

    suspend fun fetchPassengerList(params: FetchPassengerParams): HttpResponse{
        return mClient.get("manifest/route/passengers") {
            url {
                parameters.append("manifest_date", params.manifest_date)
                parameters.append("schedule_id", params.schedule_id)
            }
        }
    }

    suspend fun onboardPassenger(request: OnboardPassengerRequest): HttpResponse{
        return mClient.post("onboarded/passenger/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun fetchManifestTrips(params: ManifestTripsRequestParams): HttpResponse{
        return mClient.get("manifest-trips") {
            url {
                parameters.append("drop_off_point", params.drop_off_point)
                parameters.append("pickup_point", params.pickup_point)
                parameters.append("travel_date", params.travel_date)
            }
        }
    }

}
