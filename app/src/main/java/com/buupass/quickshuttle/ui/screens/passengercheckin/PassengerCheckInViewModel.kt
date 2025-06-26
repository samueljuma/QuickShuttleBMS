package com.buupass.quickshuttle.ui.screens.passengercheckin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.booking.FetchPassengerParams
import com.buupass.quickshuttle.data.models.onboardingpassenger.ManifestTrip
import com.buupass.quickshuttle.data.models.onboardingpassenger.ManifestTripsRequestParams
import com.buupass.quickshuttle.data.models.onboardingpassenger.OnboardPassengerRequest
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.data.repositories.PassengerCheckInRepository
import com.buupass.quickshuttle.utils.todayDate
import com.buupass.quickshuttle.utils.formatted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PassengerCheckInViewModel(
    private val authRepository: AuthRepository,
    private val passengerCheckInRepository: PassengerCheckInRepository,

): ViewModel() {

    private val _uiState = MutableStateFlow(PassengerCheckInUiState())
    val uiState = _uiState.asStateFlow()

    private val _passengerCheckInEvent = MutableSharedFlow<PassengerCheckInEvent>()
    val passengerCheckInEvent = _passengerCheckInEvent.asSharedFlow()


    init {
        _uiState.update {
            it.copy(
                date = todayDate.formatted()
            )
        }
    }
    fun getCurrentUser() = authRepository.getUserDetails()

    fun updateCityList(cityList: List<City>){
        _uiState.update {
            it.copy(
                cityList = cityList,
                cityFrom = cityList[0],
                cityTo = cityList[1]
            )
        }
    }

    fun resetDate(){
        _uiState.update {
            it.copy(
                date = todayDate.formatted()
            )
        }
    }

    fun updateDestinations(isFrom: Boolean, city: City){
        if(isFrom){
            _uiState.update {
                it.copy(
                    cityFrom = city
                )
            }
        }else{
            _uiState.update {
                it.copy(
                    cityTo = city
                )
            }
        }
    }

    fun fetchTrips() {
        _uiState.update {
            it.copy(
                isLoading = true,
                tripList = null,
                loadingMessage = "Fetching Trips...",
                error = null
            )
        }
        viewModelScope.launch {
            val params = ManifestTripsRequestParams(
                pickup_point = uiState.value.cityFrom.id.toString(),
                drop_off_point = uiState.value.cityTo.id.toString(),
                travel_date = uiState.value.date ?: todayDate.formatted()
            )
            val tripsResult = passengerCheckInRepository.fetchManifestTrips(params)
            when (tripsResult) {
                is NetworkResult.Success -> {
                    val schedules = tripsResult.data
                    _uiState.update {
                        it.copy(
                            tripList = schedules,
                            isLoading = false,
                            loadingMessage = "",
                        )
                    }
                    _passengerCheckInEvent.emit(PassengerCheckInEvent.ShowSuccessMessage("Trips Fetched Successfuly"))
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            error = tripsResult.message,
                        )
                    }
                    _passengerCheckInEvent.emit(PassengerCheckInEvent.ShowErrorMessage("There was an error!"))
                }
            }
        }
    }

    fun fetchPassengerList(isRefresh : Boolean = false){
        _uiState.update {
            it.copy(
                isLoading = true,
                passengersList = if(isRefresh) it.passengersList else emptyList(),
                loadingMessage = if(isRefresh) "Refreshing Passengers..." else "Fetching Passengers...",
                error = null
            )
        }
        viewModelScope.launch {
            val params = FetchPassengerParams(
                manifest_date = _uiState.value.date ?: todayDate.formatted(),
                schedule_id = _uiState.value.selectedTrip?.schedule_id.toString()
            )
            val result = passengerCheckInRepository.fetchPassengerList(params)

            when(result){
                is NetworkResult.Success -> {
                    val passengers = result.data.passengers
                    _uiState.update {
                        it.copy(
                            passengersList = passengers,
                            isLoading = false
                        )
                    }
                    Log.d("PassengerCheckInViewModel", "fetchPassengerList: $passengers")
                    _passengerCheckInEvent.emit(PassengerCheckInEvent.ShowSuccessMessage("Passengers Fetched Successfully"))
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    _passengerCheckInEvent.emit(PassengerCheckInEvent.ShowErrorMessage("There was an error!"))
                }

            }
        }
    }

    fun onboardPassenger(passengerId: String){
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Onboarding Passenger...",
                error = null
            )
        }
        viewModelScope.launch {
            val request = OnboardPassengerRequest(
                passenger_id = passengerId
            )
            val result = passengerCheckInRepository.onboardPassenger(request)
            when(result){
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = ""
                        )
                    }
                    setPassengerAsBooked(passengerId = passengerId)

                    _passengerCheckInEvent.emit(PassengerCheckInEvent.ShowSuccessMessage("Passenger Onboarded Successfully"))

                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            error = result.message
                        )
                    }
                    Log.d("PassengerCheckInViewModel", "onboardPassenger: ${result.message}")
                    _passengerCheckInEvent.emit(PassengerCheckInEvent.ShowErrorMessage("An error occurred!"))
                }
            }
        }

    }

    private fun setPassengerAsBooked(passengerId: String){
        _uiState.update {
            it.copy(
                passengersList = it.passengersList.map {
                    if (it.passenger_id == passengerId)
                        it.copy(onboarded = true)
                    else
                        it
                }
            )
        }
    }

    fun updateSelectedTrip(trip: ManifestTrip){
        _uiState.update {
            it.copy(
                selectedTrip = trip
            )
        }
    }
    fun clearTrips(){
        _uiState.update {
            it.copy(
                tripList = null
            )
        }
    }

    fun clearPassengersToOnboard(){
        _uiState.update {
            it.copy(
                passengersList = emptyList()
            )
        }
    }


}