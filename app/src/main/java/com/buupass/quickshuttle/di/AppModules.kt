package com.buupass.quickshuttle.di

import android.util.Log
import com.buupass.quickshuttle.MainViewModel
import com.buupass.quickshuttle.data.network.ParcelAPIService
import com.buupass.quickshuttle.data.network.PassengerAPIService
import com.buupass.quickshuttle.data.network.apiHeaders
import com.buupass.quickshuttle.data.printer.AndroidPrinterManager
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.data.repositories.BookingRepository
import com.buupass.quickshuttle.data.repositories.ParcelRepository
import com.buupass.quickshuttle.data.repositories.PassengerCheckInRepository
import com.buupass.quickshuttle.data.repositories.PaymentRepository
import com.buupass.quickshuttle.data.session.SessionManager
import com.buupass.quickshuttle.domain.printer.PrinterManager
import com.buupass.quickshuttle.ui.screens.auth.AuthViewModel
import com.buupass.quickshuttle.ui.screens.passengerbooking.booking.BookingScreenViewModel
import com.buupass.quickshuttle.ui.screens.common.printer.PrinterViewModel
import com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel.BookParcelViewModel
import com.buupass.quickshuttle.ui.screens.parcelbooking.dispatchparcel.DispatchReceiveParcelViewModel
import com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels.ShowParcelsViewModel
import com.buupass.quickshuttle.ui.screens.passengerbooking.payment.PaymentsViewModel
import com.buupass.quickshuttle.ui.screens.reprintticket.ReprintTicketViewModel
import com.buupass.quickshuttle.ui.screens.passengerbooking.showbookings.ShowBookingsScreenViewModel
import com.buupass.quickshuttle.ui.screens.passengercheckin.PassengerCheckInViewModel
import com.buupass.quickshuttle.utils.BASE_URL
import com.buupass.quickshuttle.utils.NetworkMonitor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModules = module {

    single {
        // Get Auth Repository
        val authRepository: AuthRepository by inject()

        HttpClient(CIO) {
            expectSuccess = true

            defaultRequest {
                url(BASE_URL)
                // Add auth token if available
                authRepository.getAuthToken().let { token ->
                    headers[HttpHeaders.Authorization] = token
                }
                // Add common headers
                apiHeaders.forEach { (key, value) ->
                    headers[key] = value.toString()
                }
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorLogger", message)
                    }
                }
                level = LogLevel.ALL
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        encodeDefaults = true
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    single { NetworkMonitor(androidContext()) }
    single { Dispatchers.IO }
    single { PassengerAPIService(get()) }
    single { ParcelAPIService(get()) }
    single { SessionManager(androidContext()) }

    single { AuthRepository(get(), get(), get()) }
    single { BookingRepository(get(), get()) }
    single { PaymentRepository(get(), get()) }
    single { ParcelRepository(get(), get()) }
    single { PassengerCheckInRepository(get(), get()) }

    viewModel { AuthViewModel(get()) }
    viewModel { BookingScreenViewModel(get(), get()) }
    viewModel { ReprintTicketViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { PaymentsViewModel(get(), get()) }
    viewModel { ShowBookingsScreenViewModel(get()) }

    single<PrinterManager> { AndroidPrinterManager(androidContext()) }
    viewModel { PrinterViewModel(get()) }
    viewModel { BookParcelViewModel(get(), get(), get()) }
    viewModel { DispatchReceiveParcelViewModel(get(), get(), get()) }
    viewModel { ShowParcelsViewModel(get(), get()) }
    viewModel { PassengerCheckInViewModel(get(), get()) }

    viewModel { MainViewModel(get()) }

}