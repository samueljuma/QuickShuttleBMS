package com.buupass.quickshuttle.data.models.onboardingpassenger
import kotlinx.serialization.Serializable

@Serializable
data class PassengersToOnboardResponse(
    val status: Boolean,
    val message: String,
    val passengers: List<PassengerToOnboard>
)


@Serializable
data class PassengerToOnboard(
    val booking_id: String,
    val passenger_id: String,
    val pnr_number: String,
    val route_name: String,
    val pickup: String,
    val dropoff: String,
    val name: String,
    val residence: String,
    val phone_number: String,
    val id_number: String,
    val trip: Int,
    val onboarded: Boolean = false,
    val schedule: Int,
    val seat_number: String
)

