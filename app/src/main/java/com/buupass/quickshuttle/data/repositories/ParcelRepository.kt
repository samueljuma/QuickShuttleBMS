package com.buupass.quickshuttle.data.repositories

import com.buupass.quickshuttle.data.models.parcel.BookParcelRequest
import com.buupass.quickshuttle.data.models.parcel.BookParcelResponse
import com.buupass.quickshuttle.data.models.parcel.DispatchParcelRequest
import com.buupass.quickshuttle.data.models.parcel.DispatchParcelsResponse
import com.buupass.quickshuttle.data.models.parcel.FetchParcelsFromFleetResponse
import com.buupass.quickshuttle.data.models.parcel.FetchParcelsFromFleetRequestParams
import com.buupass.quickshuttle.data.models.parcel.FetchUserBookedParcelsRequestParams
import com.buupass.quickshuttle.data.models.parcel.FleetParams
import com.buupass.quickshuttle.data.models.parcel.ParcelFleetResponse
import com.buupass.quickshuttle.data.models.parcel.ParcelRoutesResponse
import com.buupass.quickshuttle.data.models.parcel.ReceiveParcelRequest
import com.buupass.quickshuttle.data.models.parcel.ReceiveParcelsResponse
import com.buupass.quickshuttle.data.models.parcel.UserBookedParcelsResponse
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.network.ParcelAPIService
import com.buupass.quickshuttle.data.network.safeApiCall
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher

class ParcelRepository(
    private val parcelApiService: ParcelAPIService,
    private val dispatcher: CoroutineDispatcher

) {
    suspend fun fetchParcelRoutes(): NetworkResult<ParcelRoutesResponse>{
        return safeApiCall(dispatcher){
            val response = parcelApiService.fetchParcelRoutes()
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<ParcelRoutesResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }
        }
    }

    suspend fun bookParcel(bookParcelRequest: BookParcelRequest): NetworkResult<BookParcelResponse>{
        return safeApiCall(dispatcher){
            val response = parcelApiService.bookParcel(bookParcelRequest)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<BookParcelResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }
        }
    }

    suspend fun fetchParcelFleets(fleetParams: FleetParams): NetworkResult<ParcelFleetResponse>{
        return safeApiCall(dispatcher) {
            val response = parcelApiService.fetchParcelFleets(fleetParams)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<ParcelFleetResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }
        }
    }

    suspend fun fetchParcelsForDispatch(params: FetchParcelsFromFleetRequestParams): NetworkResult<FetchParcelsFromFleetResponse>{
        return safeApiCall(dispatcher){
            val response = parcelApiService.fetchParcelsForDispatch(params)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<FetchParcelsFromFleetResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }

        }
    }

    suspend fun fetchParcelsForReceipt(params: FetchParcelsFromFleetRequestParams): NetworkResult<FetchParcelsFromFleetResponse>{
        return safeApiCall(dispatcher){
            val response = parcelApiService.fetchParcelsForReceipt(params)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<FetchParcelsFromFleetResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }

        }
    }

    suspend fun dispatchParcels(request: DispatchParcelRequest): NetworkResult<DispatchParcelsResponse>{
        return safeApiCall(dispatcher){
            val response = parcelApiService.dispatchParcels(request)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<DispatchParcelsResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }
        }

    }
    suspend fun receiveParcels(request: ReceiveParcelRequest): NetworkResult<ReceiveParcelsResponse>{
        return safeApiCall(dispatcher){
            val response = parcelApiService.receiveParcels(request)
            when(response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<ReceiveParcelsResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }
        }
    }

    suspend fun fetchUserBookedParcels(params: FetchUserBookedParcelsRequestParams): NetworkResult<UserBookedParcelsResponse>{
        return safeApiCall(dispatcher){
            val response = parcelApiService.fetchUserBookedParcels(params)
            when(response.status){
                HttpStatusCode.OK -> {
                    val result = response.body<UserBookedParcelsResponse>()
                    NetworkResult.Success(result)
                }
                else -> {
                    NetworkResult.Error(response.status.description)
                }
            }
        }
    }

}