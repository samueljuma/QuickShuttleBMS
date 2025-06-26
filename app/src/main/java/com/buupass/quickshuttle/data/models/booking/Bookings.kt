package com.buupass.quickshuttle.data.models.booking

import kotlinx.serialization.Serializable

@Serializable
data class BookingsResponse(
    val status: Boolean,
    val message: String,
    val data: BookingData
)

@Serializable
data class BookingData(
    val bookings: List<Booking>
)

@Serializable
data class Booking(
    val booking_id: String,
    val booked_by: String = "" ,
    val reporting_time: String= "",
    val departure_time: String= "",
    val travel_date: String,
    val trip: String,
    val route: String,
    val pickup_location: String,
    val drop_off_location: String,
    val total_amount: Double,
    val number_of_seats: Int,
    val booked_seats: String,
    val passengers: List<Passenger>
)

@Serializable
data class Passenger(
    val pnr_number: String,
    val name: String,
    val phone_number: String,
    val id_number: String,
    val residence: String,
    val seat_number: String,
    val seat_price: Double,
    val kode: Double = 0.0,
    val luggage: Double,
    val discounted_price: Double? = null,
)