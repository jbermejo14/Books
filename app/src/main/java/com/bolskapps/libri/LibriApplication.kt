package com.bolskapps.libri

import android.app.Application
import com.bolskapps.libri.di.AppContainer
import com.bolskapps.libri.di.DefaultAppContainer

class LibriApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
