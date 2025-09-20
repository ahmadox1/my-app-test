package com.ahmadox.smartcoach

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartCoachApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}