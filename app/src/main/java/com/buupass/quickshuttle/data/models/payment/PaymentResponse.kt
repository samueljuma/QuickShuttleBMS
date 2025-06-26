package com.buupass.quickshuttle.data.models.payment

import kotlinx.serialization.Serializable

/**
 * --------------------------------------------------------------------------------------
 * PAYMENT RESPONSE FOR BOTH MPESA AND CASH
 * --------------------------------------------------------------------------------------
 */

@Serializable
data class PaymentResponse(
    val status: Boolean,
    val message: String,
    val data: PaymentData? = null
)

@Serializable
data class PaymentData(
    val booking_id: String? = null,
    val booked_by: String? = null,
    val reporting_time: String? = null,
    val departure_time: String? = null,
    val travel_date: String? = null,
    val trip: String? = null,
    val route: String? = null,
    val pickup_location: String? = null,
    val drop_off_location: String? = null,
    val total_amount: Double? = null,
    val number_of_seats: Int? = null,
    val booked_seats: String? = null,
    val passengers: List<Passenger>? = null
)

@Serializable
data class Passenger(
    val pnr_number: String? = null,
    val name: String? = null,
    val phone_number: String? = null,
    val id_number: String? = null,
    val residence: String? = null,
    val seat_number: String? = null,
    val seat_price: Double? = null,
    val discount: Double? = null,
    val discounted_price: Double? = null,
    val kode: Double? = null,
    val luggage: Double? = null
)

/**
 * --------------------------------------------------------------------------------------
 * M-PESA STK PUSH RESPONSE
 * --------------------------------------------------------------------------------------
 */

@Serializable
data class MpesaSTKPushResponse(
    val success: Boolean? = null, // Present when STK push is successful
    val status: Boolean? = null, // Present when STK push fails (e.g. seat already booked)
    val message: String,
    val data: MpesaPaymentData? = null
)

@Serializable
data class MpesaPaymentData(
    val transaction_id: String? = null,
    val phone: String? = null,
    val booking_id: String? = null
)