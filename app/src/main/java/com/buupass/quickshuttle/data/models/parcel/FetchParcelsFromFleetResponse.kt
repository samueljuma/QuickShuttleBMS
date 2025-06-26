package com.buupass.quickshuttle.data.models.parcel

import kotlinx.serialization.Serializable


@Serializable
data class FetchParcelsFromFleetResponse(
    val success: Boolean,
    val message: String,
    val fleet_id: String? = null,
    val `data`: List<String>
)

data class FetchParcelsFromFleetRequestParams(
    val fleet_id: String,
    val start_point: String,
    val end_point: String,
    val fleet_type: String = "parcel"
)

