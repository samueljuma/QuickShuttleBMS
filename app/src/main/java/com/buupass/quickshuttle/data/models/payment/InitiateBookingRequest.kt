package com.buupass.quickshuttle.data.models.payment

import kotlinx.serialization.Serializable

@Serializable
data class InitiateBookingRequest(
    val booking_channel: String = "android_bms",
    val customer_type: String,
    val travel_date: String,
    val pickup_location: String,
    val drop_off_location: String,
    val boarding_point: String,
    val drop_off_point: String,
    val trip_schedule_id: String,
    val selected_seats: String,
    val passenger_details: List<PassengerDetail>,
    val reservation_status: String,
    val payment_type: String? = null,
    val payee_phone_number: String = "",
    val total_fare: String,
    val currency_code: String
)

@Serializable
data class PassengerDetail(
    val id_number: String,
    val kode: String,
    val luggage_price: String,
    val name: String,
    val phone: String,
    val residence: String,
    val seat_number: String,
    val seat_price: String,
    val customer_type: String
)