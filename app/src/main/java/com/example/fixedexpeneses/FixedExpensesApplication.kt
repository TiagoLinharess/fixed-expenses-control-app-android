package com.example.fixedexpeneses

import android.app.Application
import com.example.fixedexpeneses.di.AppContainer
import com.example.fixedexpeneses.di.DefaultAppContainer

class FixedExpensesApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
    }
}
