package com.buupass.quickshuttle.data.models.reprint

import com.buupass.quickshuttle.data.models.booking.Booking
import kotlinx.serialization.Serializable

@Serializable
data class FetchTicketResponse(
    val status: Boolean,
    val message: String,
    val data: Booking? = null
)

@Serializable
data class FetchTicketErrorResponse(
    val status: Boolean,
    val message: String,
    val data: Booking? = null
)