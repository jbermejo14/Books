package com.example.booktracker

import android.app.Application
import com.example.booktracker.di.AppContainer
import com.example.booktracker.di.DefaultAppContainer

class BookTrackerApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
