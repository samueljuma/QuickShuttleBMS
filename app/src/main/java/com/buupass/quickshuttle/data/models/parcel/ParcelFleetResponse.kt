package com.buupass.quickshuttle.data.models.parcel

import com.buupass.quickshuttle.utils.PARCEL_FLEET_TYPE
import kotlinx.serialization.Serializable

@Serializable
data class FleetParams(
    val start_point: String,
    val end_point: String,
    val fleet_type: String = PARCEL_FLEET_TYPE,
)


@Serializable
data class ParcelFleetResponse(
    val fleet: List<Fleet>
)

@Serializable
data class Fleet(
    val id: Int,
    val registration_number: String
)