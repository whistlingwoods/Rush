package com.shub39.rush.app

import android.app.Application
import com.shub39.rush.di.rushModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RushApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Check if main process
        if (packageName == getProcessName()) {
            startKoin {
                androidLogger()
                androidContext(this@RushApplication)
                modules(rushModules)
            }
        }

    }

}