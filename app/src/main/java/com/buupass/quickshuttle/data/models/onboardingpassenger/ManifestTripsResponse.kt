package com.buupass.quickshuttle.data.models.onboardingpassenger

import kotlinx.serialization.Serializable


typealias ManifestTripListResponse = List<ManifestTrip>

@Serializable
data class ManifestTrip(
    val trip_schedule_id: Int,
    val trip_id: Int,
    val trip_name: String,
    val trip_series: String,
    val bus_type: String,
    val schedule_id: Int,
    val schedule_starting_time: String,
    val schedule_ending_time: String,
    val total_tickets: Int,
    val url: String
)


data class ManifestTripsRequestParams(
    val pickup_point: String,
    val drop_off_point: String,
    val travel_date: String
)