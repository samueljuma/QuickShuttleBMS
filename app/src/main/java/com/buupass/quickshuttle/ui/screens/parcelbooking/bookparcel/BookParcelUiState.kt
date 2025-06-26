package com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel

import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.parcel.ParcelRoute
import com.buupass.quickshuttle.domain.parcel.ParcelItemDomain
import com.buupass.quickshuttle.domain.parcel.ParcelReceiptDetails
import com.buupass.quickshuttle.domain.parcel.ParcelStickerDetails
import com.buupass.quickshuttle.domain.parcel.PayeePhoneNumber
import com.buupass.quickshuttle.domain.parcel.SenderReceiver
import com.buupass.quickshuttle.domain.parcel.TotalCost
import com.buupass.quickshuttle.utils.PaymentMethod

data class BookParcelUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val parcelRoutes: List<ParcelRoute>? = null,
    val selectedParcelRoute: ParcelRoute? = null,
    val selectedPickupPoint: City = City(),
    val selectedDropOffPoint: City = City(),
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.Cash,
    val parcelItems: List<ParcelItemDomain> = emptyList(),
    val senderDetails: SenderReceiver = SenderReceiver(),
    val receiverDetails: SenderReceiver = SenderReceiver(),
    val newParcelItem: ParcelItemDomain = ParcelItemDomain(),
    val showAddParcelItemDialog: Boolean = false,
    val payeePhoneNumber: PayeePhoneNumber = PayeePhoneNumber(),
    val totalCost: TotalCost = TotalCost(),
    val parcelReceiptDetails: ParcelReceiptDetails? = null,
    val parcelStickerDetails: ParcelStickerDetails? = null,
    val printingInProgress: Boolean = false
)
