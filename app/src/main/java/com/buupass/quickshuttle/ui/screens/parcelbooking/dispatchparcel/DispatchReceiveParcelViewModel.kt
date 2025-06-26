package com.buupass.quickshuttle.ui.screens.parcelbooking.dispatchparcel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.parcel.DispatchParcelRequest
import com.buupass.quickshuttle.data.models.parcel.FetchParcelsFromFleetRequestParams
import com.buupass.quickshuttle.data.models.parcel.Fleet
import com.buupass.quickshuttle.data.models.parcel.FleetParams
import com.buupass.quickshuttle.data.models.parcel.QueuedData
import com.buupass.quickshuttle.data.models.parcel.ReceiveParcelRequest
import com.buupass.quickshuttle.data.models.parcel.ReceivedData
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.data.repositories.BookingRepository
import com.buupass.quickshuttle.data.repositories.ParcelRepository
import com.buupass.quickshuttle.utils.NetworkErrors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DispatchReceiveParcelViewModel(
    private val parcelRepository: ParcelRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(DispatchReceiveParcelUiState())
    val uiState = _uiState.asStateFlow()

    private val _dispatchReceiveParcelEvent = MutableSharedFlow<DispatchReceiveParcelEvent>()
    val dispatchReceiveParcelEvent = _dispatchReceiveParcelEvent.asSharedFlow()

    init {
        fetchDestinations()
    }


    private fun fetchDestinations() {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Fetching Cities"
            )
        }
        viewModelScope.launch {
            val result = bookingRepository.getCities()
            when (result) {
                is NetworkResult.Success -> {
                    val cities = result.data.cities
                    _uiState.update {
                        it.copy(
                            cityList = cities,
                            cityFrom = cities[0],
                            cityTo = cities[1],
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = null
                        )
                    }
                    _dispatchReceiveParcelEvent.emit(DispatchReceiveParcelEvent.ShowSuccessMessage("Cities fetched successfully"))
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message
                        )
                    }
                    _dispatchReceiveParcelEvent.emit(DispatchReceiveParcelEvent.ShowErrorMessage("Error fetching cities"))
                }
            }
        }
    }

    fun updateDestination(city: City, isPickUp: Boolean) {
        if (isPickUp) {
            _uiState.update {
                it.copy(cityFrom = city)
            }
        } else {
            _uiState.update {
                it.copy(cityTo = city)
            }
        }
    }

    fun fetchParcelFleet(){
        val fleetParams = FleetParams(
            start_point = uiState.value.cityFrom?.id.toString(),
            end_point = uiState.value.cityTo?.id.toString(),
        )

        Log.d("DispatchReceiveParcelViewModel", "Fleet Params: $fleetParams")
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Fetching Fleets"
            )
        }

        viewModelScope.launch {
            val result = parcelRepository.fetchParcelFleets(fleetParams)

            when (result) {
                is NetworkResult.Success -> {
                    val fleets = result.data.fleet
                    Log.d("DispatchReceiveParcelViewModel", "Fleets: $fleets")
                    _uiState.update {
                        it.copy(
                            fleetList = fleets,
                            isLoading = false,
                            loadingMessage = ""
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message
                        )
                    }
                    _dispatchReceiveParcelEvent.emit(DispatchReceiveParcelEvent.ShowErrorMessage("Error fetching fleets"))

                }
            }

        }
    }

    fun fetchParcelsForDispatchOrReceipt(fleet: Fleet, isDispatch: Boolean){
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Fetching Parcels...",
                selectedFleet = fleet // Set Selected Fleet
            )
        }
        viewModelScope.launch {
            val params = FetchParcelsFromFleetRequestParams(
                fleet_id = fleet.id.toString(),
                start_point = uiState.value.cityFrom?.id.toString(),
                end_point = uiState.value.cityTo?.id.toString()
            )

            val result =
                if(isDispatch)parcelRepository.fetchParcelsForDispatch(params)
                else parcelRepository.fetchParcelsForReceipt(params)

            when(result){
                is NetworkResult.Success -> {
                    val parcels = result.data.data
                    _uiState.update {
                        it.copy(
                            parcelsToProcess = parcels,
                            isLoading = false
                        )

                    }

                    if(parcels.isEmpty()){
                        _dispatchReceiveParcelEvent.emit(DispatchReceiveParcelEvent.ShowErrorMessage("No Parcels Found"))
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message
                        )
                    }
                    _dispatchReceiveParcelEvent.emit(DispatchReceiveParcelEvent.ShowErrorMessage("Error fetching parcels"))
                }
            }
        }

    }

    fun updateParcelToBeAdded(parcelCode: String){
        _uiState.update {
            it.copy(
                parcelCodeToBeAdded = parcelCode
            )
        }
    }

    fun addParcelToParcelsForProcessing(parcelCode: String, isDispatch: Boolean){
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Getting Parcel by code: $parcelCode...",
            )
        }
        viewModelScope.launch {
            val params = FetchParcelsFromFleetRequestParams(
                fleet_id = "1",
                start_point = uiState.value.cityFrom?.id.toString(),
                end_point = uiState.value.cityTo?.id.toString()
            )

            val result = if(isDispatch)parcelRepository.fetchParcelsForDispatch(params)
            else parcelRepository.fetchParcelsForReceipt(params)

            when(result){
                is NetworkResult.Success -> {
                    val parcels = result.data.data

                    if (parcels.isEmpty()) {
                        _dispatchReceiveParcelEvent.emit(
                            DispatchReceiveParcelEvent.ShowErrorMessage("No Parcels Found with code: $parcelCode")
                        )
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }

                    if (parcels.contains(parcelCode)) {
                        _uiState.update { state ->
                            val currentList = state.parcelsToProcess.orEmpty()
                            if (currentList.contains(parcelCode)) {
                                _dispatchReceiveParcelEvent.emit(
                                    DispatchReceiveParcelEvent.ShowErrorMessage("Parcel $parcelCode is in list")
                                )
                                return@update state.copy(
                                    isLoading = false
                                )
                            }else{
                                val updatedList = currentList + parcelCode
                                state.copy(
                                    isLoading = false,
                                    parcelsToProcess = updatedList
                                )
                            }
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                        _dispatchReceiveParcelEvent.emit(
                            DispatchReceiveParcelEvent.ShowErrorMessage("Parcel code $parcelCode not found.")
                        )
                    }
                    updateParcelToBeAdded("")
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message
                        )
                    }
                    updateParcelToBeAdded("")
                    _dispatchReceiveParcelEvent.emit(DispatchReceiveParcelEvent.ShowErrorMessage("Error fetching $parcelCode"))
                }
            }
        }

    }



    fun dispatchParcels(){
        viewModelScope.launch {
            if(_uiState.value.parcelsToProcess.isNullOrEmpty()){
                _dispatchReceiveParcelEvent.emit(
                    DispatchReceiveParcelEvent.ShowErrorMessage("No parcels to dispatch")
                )
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Dispatching Parcels..."
                )
            }
            val request = DispatchParcelRequest(
                queued_data = listOf(
                    QueuedData(
                        fleet_id = _uiState.value.selectedFleet?.id ?: -1,
                        parcel_codes = _uiState.value.parcelsToProcess!!,
                        queued_by = authRepository.getUserDetails().id.toString()
                    )
                )
            )

            val result = parcelRepository.dispatchParcels(request)
            when(result){
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            successMessage = result.data.message,
                            parcelsToProcess = emptyList()
                        )
                    }
                    _dispatchReceiveParcelEvent.emit(
                        DispatchReceiveParcelEvent.ShowSuccessMessageDialog(result.data.message)
                    )
                }
                is NetworkResult.Error -> {
                    val error = if(result.message == NetworkErrors.INTERNAL_SERVER_ERROR)
                        "Parcel(s) may have already been received" else result.message
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = error
                        )
                    }
                    Log.d("DispatchReceiveParcelViewModel", "Dispatch Error: ${result.message}")
                    _dispatchReceiveParcelEvent.emit(
                        DispatchReceiveParcelEvent.ShowErrorMessageDialog("Dispatch Failed")
                    )
                }
            }
        }

    }

    fun receiveParcels(){
        viewModelScope.launch {
            if(_uiState.value.parcelsToProcess.isNullOrEmpty()){
                _dispatchReceiveParcelEvent.emit(
                    DispatchReceiveParcelEvent.ShowErrorMessage("No parcels to Receive")
                )
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Processing Parcel Receipt..."
                )
            }
            val request = ReceiveParcelRequest(
                received_data = listOf(
                    ReceivedData(
                        fleet_id = _uiState.value.selectedFleet?.id ?: -1,
                        parcel_codes = _uiState.value.parcelsToProcess!!,
                        received_by = authRepository.getUserDetails().id.toString(),
                        destination = _uiState.value.cityTo?.id ?: -1
                    )
                )
            )

            val result = parcelRepository.receiveParcels(request)
            when(result){
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            successMessage = result.data.message,
                            parcelsToProcess = emptyList()
                        )
                    }
                    _dispatchReceiveParcelEvent.emit(
                        DispatchReceiveParcelEvent.ShowSuccessMessageDialog(result.data.message)
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message)
                    }
                    Log.d("DispatchReceiveParcelViewModel", "Receive Error: ${result.message}")
                    _dispatchReceiveParcelEvent.emit(
                        DispatchReceiveParcelEvent.ShowErrorMessageDialog("Receive Parcels Failed")
                    )
                }
            }
        }

    }

    fun removeParcelFromList(parcelCode: String){
        val updatedList = _uiState.value.parcelsToProcess?.toMutableList()
        updatedList?.remove(parcelCode)
        _uiState.update {
            it.copy(
                parcelsToProcess = updatedList
            )
        }
    }

    fun clearParcelList(){
        _uiState.update {
            it.copy(
                parcelsToProcess = emptyList()
            )
        }
    }

}