package com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.parcel.BookParcelRequest
import com.buupass.quickshuttle.data.models.parcel.ParcelRoute
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.data.repositories.ParcelRepository
import com.buupass.quickshuttle.domain.parcel.ParcelItemDomain
import com.buupass.quickshuttle.domain.parcel.ParcelReceiptDetails
import com.buupass.quickshuttle.domain.parcel.ParcelStickerDetails
import com.buupass.quickshuttle.domain.parcel.PayeePhoneNumber
import com.buupass.quickshuttle.domain.printer.PrinterManager
import com.buupass.quickshuttle.utils.PaymentMethod
import com.buupass.quickshuttle.utils.toBookParcelRequest
import com.buupass.quickshuttle.utils.toParcelReceiptDetails
import com.buupass.quickshuttle.utils.toParcelStickerDetails
import com.buupass.quickshuttle.utils.validateField
import com.buupass.quickshuttle.utils.validatePhone
import com.buupass.quickshuttle.utils.validateSenderID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.com.ctk.sdk.PosApiHelper

class BookParcelViewModel(
    private val parcelRepository: ParcelRepository,
    private val authRepository: AuthRepository,
    private val printerManager: PrinterManager
): ViewModel() {

    private val _uiState = MutableStateFlow(BookParcelUiState())
    val uiState: StateFlow<BookParcelUiState> = _uiState.asStateFlow()

    private val _bookParcelEvent = MutableSharedFlow<BookParcelEvent>()
    val bookParcelEvent: SharedFlow<BookParcelEvent> = _bookParcelEvent.asSharedFlow()

    init {
        fetchParcelRoutes()
    }

    private fun fetchParcelRoutes(){
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Fetching Parcel Routes"
            )
        }

        viewModelScope.launch {
            val result = parcelRepository.fetchParcelRoutes()
            when(result){
                is NetworkResult.Success -> {
                    val parcelRoutes = result.data.routes
                    _uiState.update {
                        it.copy(
                            parcelRoutes = parcelRoutes,
                            selectedParcelRoute = parcelRoutes[0],
                            selectedPickupPoint = parcelRoutes[0].pickup_points[0],
                            selectedDropOffPoint = parcelRoutes[0].dropoff_points[0],
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = "There was an error fetching parcel routes",
                        )
                    }
                    _bookParcelEvent.emit(BookParcelEvent.ShowErrorMessageToast)
                }
            }

        }
    }

    fun bookParcel(bookParcelRequest: BookParcelRequest){
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Booking Parcel ..."
            )
        }
        viewModelScope.launch {
            val result = parcelRepository.bookParcel(bookParcelRequest)
            when(result){
                is NetworkResult.Success -> {
                    val response = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            successMessage = "Parcel Booked Successfully",
                            parcelReceiptDetails = response.toParcelReceiptDetails(
                                state = _uiState.value,
                                user = authRepository.getUserDetails()
                            ),
                            parcelStickerDetails = response.toParcelStickerDetails(
                                state = _uiState.value
                            )
                        )
                    }
                    _bookParcelEvent.emit(BookParcelEvent.ShowSuccessMessageToast)
                    //Clear states that are no longer needed
                    val currentState = _uiState.value
                    val updatedState = BookParcelUiState(
                        parcelRoutes = currentState.parcelRoutes,
                        selectedParcelRoute = currentState.selectedParcelRoute,
                        selectedPickupPoint = currentState.selectedPickupPoint,
                        selectedDropOffPoint = currentState.selectedDropOffPoint,
                        parcelReceiptDetails = currentState.parcelReceiptDetails,
                        parcelStickerDetails = currentState.parcelStickerDetails
                    )
                    _uiState.update {
                        updatedState
                    }

                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message,
                        )
                    }
                    _bookParcelEvent.emit(BookParcelEvent.ShowErrorMessageToast)
                }
            }
        }
    }

    fun onParcelRouteSelected(parcelRoute: ParcelRoute) {
        _uiState.update {
            it.copy(
                selectedParcelRoute = parcelRoute,
                selectedPickupPoint = parcelRoute.pickup_points[0],
                selectedDropOffPoint = parcelRoute.dropoff_points[0]
            )
        }
    }

    fun onPointSelected(city: City, isPickUp: Boolean){
        when{
            isPickUp -> {
                _uiState.update {
                    it.copy(
                        selectedPickupPoint = city
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        selectedDropOffPoint = city
                    )
                }
            }
        }
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethod){
        _uiState.update {
            it.copy(
                selectedPaymentMethod = paymentMethod
            )
        }

        //Reset Payee Phone Number
        if(paymentMethod == PaymentMethod.Cash){
            _uiState.update {
                it.copy(
                    payeePhoneNumber = PayeePhoneNumber()
                )
            }
        }
    }

    fun updateSenderDetails(field: String, value: String){
        _uiState.update { state ->
            val senderDetails = state.senderDetails
            val updatedSenderDetails = when (field) {
                "name" -> senderDetails.copy(
                    name = value,
                    nameError = validateField(value)
                )
                "kraPin" -> senderDetails.copy(
                    kraPin = value,
                    kraPinError = validateField(value)
                )
                "idNumber" -> senderDetails.copy(
                    idNumber = value,
                    idNumberError = validateSenderID(value)
                )
                "phoneNumber" -> senderDetails.copy(
                    phoneNumber = value,
                    phoneNumberError = validatePhone(value)
                )
                else -> senderDetails
            }

            state.copy(senderDetails = updatedSenderDetails)
        }
    }

    fun updateReceiverDetails(field: String, value: String){
        _uiState.update { state ->
            val receiverDetails = state.receiverDetails
            val updatedReceiverDetails = when (field) {
                "name" -> receiverDetails.copy(
                    name = value,
                    nameError = validateField(value)
                )
                "idNumber" -> receiverDetails.copy(
                    idNumber = value,
                    idNumberError = validateSenderID(value)
                )
                "phoneNumber" -> receiverDetails.copy(
                    phoneNumber = value,
                    phoneNumberError = validatePhone(value)
                )
                else -> receiverDetails
            }
            state.copy(receiverDetails = updatedReceiverDetails)
        }
    }

    private fun validateSenderReceiverDetails(){
        _uiState.update { state ->
            val senderDetails = state.senderDetails
            val receiverDetails = state.receiverDetails
            val updatedSenderDetails = senderDetails.copy(
                nameError = validateField(senderDetails.name),
                kraPinError = validateField(senderDetails.kraPin),
                idNumberError = validateSenderID(senderDetails.idNumber),
                phoneNumberError = validatePhone(senderDetails.phoneNumber)
            )
            val updatedReceiverDetails = receiverDetails.copy(
                nameError = validateField(receiverDetails.name),
                idNumberError = validateSenderID(receiverDetails.idNumber),
                phoneNumberError = validatePhone(receiverDetails.phoneNumber)
            )
            state.copy(
                senderDetails = updatedSenderDetails,
                receiverDetails = updatedReceiverDetails
            )
        }
    }

    fun updateShowAddParcelItemDialog(show: Boolean){
        _uiState.update {
            it.copy(
                showAddParcelItemDialog = show,
                newParcelItem = ParcelItemDomain() // Reset New Item
            )
        }
    }

    fun updateNewParcelItemDetails(field: String, value: String){
        _uiState.update { state ->
            val newParcelItem = state.newParcelItem
            val updatedNewParcelItem = when (field) {
                "parcelName" -> newParcelItem.copy(
                    name = value,
                    nameError = validateField(value)
                )
                "parcelQuantity" -> newParcelItem.copy(
                    quantity = value,
                    quantityError = validateField(value)
                )
                "parcelWeight" -> newParcelItem.copy(
                    weight = value,
                    weightError = validateField(value)
                )
                else -> newParcelItem
            }

            state.copy(
                newParcelItem = updatedNewParcelItem
            )
        }
    }

    private fun validateNewParcelItemDetails(){
        _uiState.update { state ->
            val newParcelItem = state.newParcelItem
            val updatedNewParcelItem = newParcelItem.copy(
                nameError = validateField(newParcelItem.name),
                quantityError = validateField(newParcelItem.quantity),
                weightError = validateField(newParcelItem.weight),
            )
            state.copy(newParcelItem = updatedNewParcelItem)
        }
    }

    fun addParcelItem() {
        viewModelScope.launch {
            validateNewParcelItemDetails()
            val newParcelItem = _uiState.value.newParcelItem

            if (newParcelItem.isValid) {
                _uiState.update { state ->
                    val updatedParcelItems = state.parcelItems + newParcelItem
                    state.copy(
                        parcelItems = updatedParcelItems,
                        newParcelItem = ParcelItemDomain(), // Reset the form
                        showAddParcelItemDialog = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Please fill in all fields correctly"
                    )
                }
                _bookParcelEvent.emit(BookParcelEvent.ShowErrorMessageToast)
            }
        }
    }

    fun removeParcelItem(item: ParcelItemDomain) {
        _uiState.update { state ->
            val updatedParcelItems = state.parcelItems.toMutableList().apply {
                remove(item)
            }
            state.copy(parcelItems = updatedParcelItems)
        }
    }

    fun updateTotalCost(value: String){
        _uiState.update { state->
            val totalCost = state.totalCost
            val updatedTotalCost = totalCost.copy(
                value = value,
                error = validateField(value)
            )
            state.copy(totalCost = updatedTotalCost)
        }
    }

    fun updatePayeePhoneNumber(value: String){
        _uiState.update { state ->
            val payeePhoneNumber = state.payeePhoneNumber
            val updatedPayeePhoneNumber = payeePhoneNumber.copy(
                value = value,
                error = validatePhone(value)
            )
            state.copy(payeePhoneNumber = updatedPayeePhoneNumber)
        }
    }

    fun validatePaymentDetails() {
        _uiState.update { state ->
            when (state.selectedPaymentMethod) {
                PaymentMethod.Cash -> {
                    val updatedTotalCost = state.totalCost.copy(
                        error = validateField(state.totalCost.value)
                    )
                    state.copy(totalCost = updatedTotalCost)
                }

                PaymentMethod.Mpesa -> {
                    val updatedPayeePhoneNumber = state.payeePhoneNumber.copy(
                        error = validatePhone(state.payeePhoneNumber.value)
                    )
                    state.copy(payeePhoneNumber = updatedPayeePhoneNumber)
                }
            }
        }
    }




    fun bookParcel() {
        viewModelScope.launch {
            // Step 1: Validate inputs
            validateSenderReceiverDetails()
            validatePaymentDetails()

            val state = _uiState.value
            val parcelItems = state.parcelItems

            // Step 2: Check for missing/invalid fields
            if (!eachRequiredDetailIsValid()) {
                showError("Please fill out all fields correctly")
                return@launch
            }

            // Step 3: Check for empty parcel items
            if (parcelItems.isEmpty()) {
                showError("You have not added any parcel items")
                return@launch
            }

            // Step 4: Ready to proceed -> Make booking request
            val request = getParcelBookingRequest()

            bookParcel(request)
        }
    }


    private suspend fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
        _bookParcelEvent.emit(BookParcelEvent.ShowErrorMessageToast)
    }


    private fun eachRequiredDetailIsValid(): Boolean {
        val state = _uiState.value
        val commonValid = state.senderDetails.isValidSenderDetails &&
            state.receiverDetails.isValidReceiverDetails &&
            state.totalCost.isValid

        return when (state.selectedPaymentMethod) {
            PaymentMethod.Mpesa -> commonValid && state.payeePhoneNumber.isValid
            PaymentMethod.Cash -> commonValid
        }
    }

    private fun getParcelBookingRequest(): BookParcelRequest {
        return _uiState.value.toBookParcelRequest()
    }

    fun printParcelReceipt(
        bluetoothIsOn: Boolean,
        context: Context,
        posApiHelper: PosApiHelper,
        receiptDetails: ParcelReceiptDetails?
    ) {
        _uiState.update { it.copy(printingInProgress = true) }
        viewModelScope.launch {


            if (_uiState.value.parcelReceiptDetails == null) {
                _uiState.update { it.copy(errorMessage = "No parcel receipts to print") }
                _bookParcelEvent.emit(BookParcelEvent.ShowErrorMessageToast)
            }

            val posPrinterReady = try {
                posApiHelper.PrintCheckStatus() == 0
            } catch (e: Exception) {
                Log.e("POSCheck", "POS status check failed: ${e.localizedMessage}")
                false
            }

            if(posPrinterReady){
                try{
                    com.buupass.quickshuttle.utils.printParcelReceipt(
                        posApiHelper = posApiHelper,
                        parcelReceiptDetails = receiptDetails,
                        context = context
                    )
                    resetParcelReceiptDetails()
                }catch (e: Exception){
                    Log.e("POSPrint", "POS SDK print failed: ${e.localizedMessage}")
                    if (bluetoothIsOn) {
                        printReceipt()
                        resetParcelReceiptDetails()
                    }
                }
            }
            else{
                if (bluetoothIsOn) {
                    printReceipt()
                    resetParcelReceiptDetails()
                }else{
                    Toast.makeText(context, "Printer is not turned on", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun resetParcelReceiptDetails() {
        _uiState.update { it.copy(parcelReceiptDetails = null) }
    }
    fun resetParcelStickerDetails() {
        _uiState.update { it.copy(parcelStickerDetails = null) }
    }

    fun printParcelStickers(
        bluetoothIsOn: Boolean,
        context: Context,
        posApiHelper: PosApiHelper,
        stickerDetails: ParcelStickerDetails?
    ) {
        _uiState.update { it.copy(printingInProgress = true) }
        viewModelScope.launch {

            if (_uiState.value.parcelStickerDetails == null){
                _uiState.update { it.copy(errorMessage = "No parcel stickers to print") }
                _bookParcelEvent.emit(BookParcelEvent.ShowErrorMessageToast)
            }

            val posPrinterReady = try {
                posApiHelper.PrintCheckStatus() == 0
            } catch (e: Exception) {
                Log.e("POSCheck", "POS status check failed: ${e.localizedMessage}")
                false
            }

            if(posPrinterReady){
                try{
                    com.buupass.quickshuttle.utils.printParcelStickers(
                        posApiHelper = posApiHelper,
                        parcelStickerDetails = stickerDetails,
                        context = context
                    )
                    resetParcelStickerDetails()
                }catch (e: Exception){
                    Log.e("POSPrint", "POS SDK print failed: ${e.localizedMessage}")
                    if (bluetoothIsOn) {
                        printStickers()
                        resetParcelStickerDetails()
                    }
                }
            }
            else{
                if (bluetoothIsOn) {
                    printStickers()
                    resetParcelStickerDetails()
                }else{
                    Toast.makeText(context, "Printer is not turned on", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }

    suspend fun printStickers(){
        try {
            printerManager.printParcelStickers(_uiState.value.parcelStickerDetails)
            _uiState.update {
                it.copy(
                    parcelStickerDetails = null
                )
            }

        } catch (e: Exception) {
        _uiState.update { it.copy(errorMessage = "Error when printing") }
        Log.e("BookParcelViewModel", "Error when printing: ${e.message}")
        _bookParcelEvent.emit(BookParcelEvent.ShowErrorMessageToast)
    }finally {
        _uiState.update {
            it.copy(
                printingInProgress = false
            )
        }
    }
    }

    suspend fun printReceipt(){
        try {
            printerManager.printParcelReceipt(_uiState.value.parcelReceiptDetails) // suspend function
            _uiState.update {
                it.copy(
                    parcelReceiptDetails = null
                )
            }

        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Error when printing") }
            Log.e("BookParcelViewModel", "Error when printing: ${e.message}")
            _bookParcelEvent.emit(BookParcelEvent.ShowErrorMessageToast)
        }finally {
            _uiState.update {
                it.copy(
                    printingInProgress = false
                )
            }
        }
    }
}
