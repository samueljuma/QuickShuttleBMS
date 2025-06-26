package com.buupass.quickshuttle.data.models.parcel

import kotlinx.serialization.Serializable

@Serializable
data class FetchUserBookedParcelsRequestParams(
    val user_id: String,
    val date: String
)

@Serializable
data class UserBookedParcelsResponse(
    val status: Boolean,
    val message: String,
    val parcels: List<ParcelData>
)

@Serializable
data class ParcelData(
    val parcel_code: String,
    val waybill: String,
    val total_amount: Double,
    val sender_name: String,
    val sender_phone_number: String,
    val receiver_name: String,
    val receiver_phone_number: String,
    val receiver_national_id: String,
    val parcel_items: List<ParcelItemDetails>
)

@Serializable
data class ParcelItemDetails(
    val parcel_item_code: String,
    val waybill: String,
    val content: String,
    val quantity: Int
)
