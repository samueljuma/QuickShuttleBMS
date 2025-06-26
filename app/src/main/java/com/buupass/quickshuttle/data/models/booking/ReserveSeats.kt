package com.buupass.quickshuttle.data.models.booking

import kotlinx.serialization.Serializable

@Serializable
data class ReserveSeatsRequest(
    val booking_channel: String = "android_bms",
    val customer_type: String = "normal",
    val drop_off_location: String?,
    val pickup_location: String?,
    val reservation_status: String = "1",
    val selected_seats: String?,
    val travel_date: String?,
    val trip_schedule_id: String?
)

@Serializable
data class ReserveSeatsResponse(
    val status: Boolean,
    val message: String
)