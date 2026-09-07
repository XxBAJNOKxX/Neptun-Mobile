package com.example.core.di

import android.content.Context
import com.example.core.notification.AlarmScheduler
import com.example.core.security.EncryptedPreferencesManager
import com.example.data.local.NeptunDatabase
import com.example.data.network.NeptunNetworkClient
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.NeptunRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.NeptunRepository
import com.example.domain.usecase.BuildStudyProfileUseCase
import com.example.domain.usecase.CalculateAveragesUseCase

interface AppContainer {
    val prefsManager: EncryptedPreferencesManager
    val database: NeptunDatabase
    val networkClient: NeptunNetworkClient
    val authRepository: AuthRepository
    val neptunRepository: NeptunRepository
    val calculateAveragesUseCase: CalculateAveragesUseCase
    val buildStudyProfileUseCase: BuildStudyProfileUseCase
    val alarmScheduler: AlarmScheduler
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val prefsManager: EncryptedPreferencesManager by lazy {
        EncryptedPreferencesManager(context)
    }

    override val database: NeptunDatabase by lazy {
        NeptunDatabase.getInstance(context)
    }

    override val networkClient: NeptunNetworkClient by lazy {
        NeptunNetworkClient()
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(context, prefsManager, networkClient)
    }

    override val neptunRepository: NeptunRepository by lazy {
        NeptunRepositoryImpl(database, networkClient, prefsManager)
    }

    override val calculateAveragesUseCase: CalculateAveragesUseCase by lazy {
        CalculateAveragesUseCase()
    }

    override val buildStudyProfileUseCase: BuildStudyProfileUseCase by lazy {
        BuildStudyProfileUseCase()
    }

    override val alarmScheduler: AlarmScheduler by lazy {
        AlarmScheduler(context)
    }
}
