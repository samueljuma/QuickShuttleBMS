package com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.models.parcel.FetchUserBookedParcelsRequestParams
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.data.repositories.ParcelRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShowParcelsViewModel(
    private val parcelRepository: ParcelRepository,
    private val authRepository: AuthRepository
): ViewModel(){

    private val _uiState = MutableStateFlow(ShowParcelsUiState())
    val uiState = _uiState.asStateFlow()

    private val _showParcelsEvent = MutableSharedFlow<ShowParcelsEvent>()
    val showParcelsEvent = _showParcelsEvent.asSharedFlow()


    fun fetchUserBookedParcels(date: String){
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Fetching parcels..."
            )
        }
        viewModelScope.launch {
            val params = FetchUserBookedParcelsRequestParams(
                user_id = authRepository.getUserDetails().id.toString(),
                date = date
            )

            val result = parcelRepository.fetchUserBookedParcels(params)
            when(result){
                is NetworkResult.Success -> {
                    val parcels = result.data.parcels
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            parcelList = result.data.parcels
                        )
                    }
                    if(parcels.isEmpty()){
                        _showParcelsEvent.emit(ShowParcelsEvent.ShowSuccessMessage("No parcels found"))
                    }else{
                        _showParcelsEvent.emit(ShowParcelsEvent.ShowSuccessMessage("Parcels fetched successfully"))
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            error = result.message
                        )
                    }
                    _showParcelsEvent.emit(ShowParcelsEvent.ShowErrorMessage("Error fetching parcels"))
                }
            }
        }
    }
}