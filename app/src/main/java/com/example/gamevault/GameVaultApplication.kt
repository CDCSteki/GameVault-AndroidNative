package com.example.gamevault

import android.app.Application
import com.example.gamevault.di.AppContainer

class GameVaultApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}