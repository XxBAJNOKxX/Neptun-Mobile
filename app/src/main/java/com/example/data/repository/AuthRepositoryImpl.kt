package com.example.data.repository

import android.content.Context
import com.example.core.security.EncryptedPreferencesManager
import com.example.data.network.NeptunApiClient
import com.example.data.network.NeptunAuthResult
import com.example.data.network.NeptunNetworkClient
import com.example.domain.model.Neptun2FASession
import com.example.domain.model.StudentCredentials
import com.example.domain.model.University
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.TwoFactorRequiredException
import com.example.domain.repository.TwoFactorSessionRequiredException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class AuthRepositoryImpl(
    private val context: Context,
    private val prefsManager: EncryptedPreferencesManager,
    private val networkClient: NeptunNetworkClient,
    private val neptunApiClient: NeptunApiClient = NeptunApiClient()
) : AuthRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _universitiesFlow = MutableStateFlow<List<University>>(emptyList())
    private var lastAttemptedUniversity: University? = null

    override fun getUniversities(): Flow<List<University>> = _universitiesFlow.asStateFlow()

    override fun getCredentials(): Flow<StudentCredentials?> = prefsManager.credentialsFlow

    override suspend fun loadUniversitiesFromAssets(): List<University> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("universities.json")
            val reader = InputStreamReader(inputStream)
            val text = reader.readText()
            reader.close()
            val list = json.decodeFromString<List<University>>(text)
            _universitiesFlow.value = list
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun login(
        university: University,
        neptunCode: String,
        password: String,
        twoFactorCode: String
    ): Result<StudentCredentials> = withContext(Dispatchers.IO) {
        val trimmedCode = neptunCode.trim().uppercase()
        val trimmedPassword = password.trim()
        lastAttemptedUniversity = university

        if (trimmedCode.length != 6) {
            return@withContext Result.failure(IllegalArgumentException("A Neptun kódnak pontosan 6 karakterből kell állnia!"))
        }
        if (trimmedPassword.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("A jelszó mező nem lehet üres!"))
        }

        // Demo test mode bypass
        if (trimmedCode == "DEMO01" || trimmedPassword.equals("demo", ignoreCase = true) || trimmedPassword.equals("jelszo", ignoreCase = true)) {
            prefsManager.saveCredentials(
                neptunCode = trimmedCode,
                password = trimmedPassword,
                universityId = university.id,
                universityName = university.name,
                neptunUrl = university.neptunUrl,
                studentName = "Kovács Dániel",
                sessionToken = "demo-token"
            )
            prefsManager.setBaseUrl(university.neptunUrl)
            prefsManager.setIsModernApi(true)

            val creds = prefsManager.loadCredentials() ?: StudentCredentials(
                neptunCode = trimmedCode,
                universityId = university.id,
                universityName = university.name,
                neptunUrl = university.neptunUrl,
                studentName = "Kovács Dániel",
                trainingProgram = "Mérnökinformatikus BSc",
                isLoggedIn = true,
                lastSyncTime = System.currentTimeMillis()
            )
            return@withContext Result.success(creds)
        }

        // Real network authentication via NeptunApiClient
        val deviceCookie = prefsManager.getDeviceCookie(trimmedCode)
        val authResult = neptunApiClient.authenticate(
            rawUrl = university.neptunUrl,
            neptunCode = trimmedCode,
            password = trimmedPassword,
            twoFactorCode = twoFactorCode,
            savedDeviceCookie = deviceCookie
        )

        when (authResult) {
            is NeptunAuthResult.TwoFactorRequired -> {
                Result.failure(TwoFactorRequiredException(authResult.twoFactorToken))
            }
            is NeptunAuthResult.TwoFactorSessionRequired -> {
                Result.failure(TwoFactorSessionRequiredException(authResult.session))
            }
            is NeptunAuthResult.Success -> {
                prefsManager.saveCredentials(
                    neptunCode = trimmedCode,
                    password = trimmedPassword,
                    universityId = university.id,
                    universityName = university.name,
                    neptunUrl = authResult.normalizedBaseUrl,
                    studentName = authResult.studentName,
                    sessionToken = authResult.accessToken
                )
                prefsManager.setAccessToken(authResult.accessToken)
                authResult.refreshToken?.let { prefsManager.setRefreshToken(it) }
                authResult.deviceCookie?.let { prefsManager.setDeviceCookie(trimmedCode, it) }
                prefsManager.setIsModernApi(authResult.isModernApi)
                prefsManager.setBaseUrl(authResult.normalizedBaseUrl)

                val creds = prefsManager.loadCredentials() ?: StudentCredentials(
                    neptunCode = trimmedCode,
                    universityId = university.id,
                    universityName = university.name,
                    neptunUrl = authResult.normalizedBaseUrl,
                    studentName = authResult.studentName,
                    trainingProgram = authResult.trainingProgram,
                    isLoggedIn = true,
                    lastSyncTime = System.currentTimeMillis()
                )
                Result.success(creds)
            }
            is NeptunAuthResult.Failure -> {
                Result.failure(Exception(authResult.message))
            }
        }
    }

    override suspend fun request2FAEmailCode(session: Neptun2FASession): Result<Neptun2FASession> {
        return neptunApiClient.request2FAEmailCode(session)
    }

    override suspend fun verify2FACode(
        session: Neptun2FASession,
        code: String,
        isTotp: Boolean
    ): Result<StudentCredentials> = withContext(Dispatchers.IO) {
        val authResult = neptunApiClient.verify2FACode(session, code, isTotp)
        when (authResult) {
            is NeptunAuthResult.Success -> {
                val uni = lastAttemptedUniversity ?: University(
                    id = "elte",
                    name = "Eötvös Loránd Tudományegyetem",
                    shortName = "ELTE",
                    city = "Budapest",
                    neptunUrl = session.baseUrl
                )

                prefsManager.saveCredentials(
                    neptunCode = session.neptunCode,
                    password = "******",
                    universityId = uni.id,
                    universityName = uni.name,
                    neptunUrl = authResult.normalizedBaseUrl,
                    studentName = authResult.studentName,
                    sessionToken = authResult.accessToken
                )
                prefsManager.setAccessToken(authResult.accessToken)
                authResult.deviceCookie?.let { prefsManager.setDeviceCookie(session.neptunCode, it) }
                prefsManager.setIsModernApi(authResult.isModernApi)
                prefsManager.setBaseUrl(authResult.normalizedBaseUrl)

                val creds = prefsManager.loadCredentials() ?: StudentCredentials(
                    neptunCode = session.neptunCode,
                    universityId = uni.id,
                    universityName = uni.name,
                    neptunUrl = authResult.normalizedBaseUrl,
                    studentName = authResult.studentName,
                    trainingProgram = authResult.trainingProgram,
                    isLoggedIn = true,
                    lastSyncTime = System.currentTimeMillis()
                )
                Result.success(creds)
            }
            is NeptunAuthResult.Failure -> {
                Result.failure(Exception(authResult.message))
            }
            else -> {
                Result.failure(Exception("Nem sikerült befejezni a kétlépcsős azonosítást!"))
            }
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        prefsManager.clear()
    }

    override suspend fun isOfflineModeAvailable(): Boolean = withContext(Dispatchers.IO) {
        prefsManager.loadCredentials() != null
    }
}
