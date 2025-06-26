package com.buupass.quickshuttle.data.network

import com.buupass.quickshuttle.data.models.parcel.BookParcelRequest
import com.buupass.quickshuttle.data.models.parcel.DispatchParcelRequest
import com.buupass.quickshuttle.data.models.parcel.FetchParcelsFromFleetRequestParams
import com.buupass.quickshuttle.data.models.parcel.FetchUserBookedParcelsRequestParams
import com.buupass.quickshuttle.data.models.parcel.FleetParams
import com.buupass.quickshuttle.data.models.parcel.ReceiveParcelRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ParcelAPIService(
    private val mClient: HttpClient
) {

    suspend fun fetchParcelRoutes(): HttpResponse{
        return mClient.get("routes/")
    }

    suspend fun bookParcel(bookParcelRequest: BookParcelRequest): HttpResponse{
        return mClient.post("create/parcel/"){
            contentType(ContentType.Application.Json)
            setBody(bookParcelRequest)
        }
    }

    suspend fun fetchParcelFleets(fleetParams: FleetParams): HttpResponse{
        return mClient.get("parcel/fleet"){
            url{
                parameters.append("start_point", fleetParams.start_point)
                parameters.append("end_point", fleetParams.end_point)
                parameters.append("fleet_type", fleetParams.fleet_type)
            }
        }
    }

    suspend fun fetchParcelsForDispatch(params: FetchParcelsFromFleetRequestParams): HttpResponse{
        return mClient.get("parcel/queue/list"){
            url{
                parameters.append("fleet_id", params.fleet_id)
                parameters.append("start_point", params.start_point)
                parameters.append("end_point", params.end_point)
            }
        }
    }
    suspend fun fetchParcelsForReceipt(params: FetchParcelsFromFleetRequestParams): HttpResponse{
        return mClient.get("parcel/receive/list"){
            url{
                parameters.append("fleet_id", params.fleet_id)
                parameters.append("start_point", params.start_point)
                parameters.append("end_point", params.end_point)
            }
        }
    }

    suspend fun dispatchParcels(request: DispatchParcelRequest): HttpResponse{
        return mClient.post("parcel/queue/list/process/"){
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
    suspend fun receiveParcels(request: ReceiveParcelRequest): HttpResponse{
        return mClient.post("parcel/receive/list/process/"){
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun fetchUserBookedParcels(
        params: FetchUserBookedParcelsRequestParams
    ): HttpResponse {
        return mClient.get("parcel/user/"){
            url{
                parameters.append("user_id", params.user_id)
                parameters.append("date", params.date)
            }
        }
    }
}