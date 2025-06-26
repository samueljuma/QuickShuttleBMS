package com.buupass.quickshuttle.data.models.parcel

import kotlinx.serialization.Serializable

@Serializable
data class DispatchParcelRequest(
    val queued_data: List<QueuedData>
)


@Serializable
data class QueuedData(
    val fleet_id: Int,
    val parcel_codes: List<String>,
    val queued_by: String,
    val driver_name: String = ""
)

@Serializable
data class DispatchParcelsResponse(
    val success: Boolean,
    val message: String,
)


@Serializable
data class ReceiveParcelRequest(
    val received_data: List<ReceivedData>
)


@Serializable
data class ReceivedData(
    val fleet_id: Int,
    val parcel_codes: List<String>,
    val received_by: String,
    val destination: Int
)

@Serializable
data class ReceiveParcelsResponse(
    val success: Boolean,
    val message: String,
    val data: ReceiveParcelsData
)

@Serializable
data class ReceiveParcelsData(
    val misrouted: List<String?>,
    val not_dispatched: List<String?>
)


