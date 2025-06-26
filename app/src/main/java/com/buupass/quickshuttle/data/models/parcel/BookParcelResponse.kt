package com.buupass.quickshuttle.data.models.parcel

import kotlinx.serialization.Serializable

@Serializable
data class BookParcelResponse(
    val status: Boolean,
    val message: String,
    val data: Data
)

@Serializable
data class Data(
    val parcel_code: String,
    val waybill: String,
    val sender_name: String,
    val sender_phone_number: String,
    val sender_national_id: String? = null,
    val receiver_name: String,
    val receiver_phone_number: String,
    val total_amount: Double,
    val payment_type: String,
    val payment_status: Boolean,
    val parcel_items: List<ParcelItemDto>,
    val receipt_parcel_items: List<ReceiptParcelItem>
)

@Serializable
data class ParcelItemDto(
    val parcel_item_code: String,
    val content: String,
    val waybill: String,
    val quantity: Int,
    val weight: String
)

@Serializable
data class ReceiptParcelItem(
    val parcel_item_code: String,
    val waybill: String,
    val content: String,
    val is_dispatched: Boolean,
    val is_received: Boolean,
    val is_issued: Boolean,
    val quantity: Int,
    val slug: String
)
