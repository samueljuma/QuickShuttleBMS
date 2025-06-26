package com.buupass.quickshuttle

import android.app.Application
import com.buupass.quickshuttle.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class QuickShuttleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@QuickShuttleApplication)
            modules(appModules)
        }
    }
}