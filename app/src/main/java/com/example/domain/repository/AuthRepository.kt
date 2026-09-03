package com.example.domain.repository

import com.example.domain.model.StudentCredentials
import com.example.domain.model.University
import kotlinx.coroutines.flow.Flow

class TwoFactorRequiredException(val twoFactorToken: String) :
    Exception("Kétlépcsős azonosítás (2FA) szükséges! Kérjük, add meg az SMS-ben vagy hitelesítő appban kapott kódot.")

interface AuthRepository {
    fun getUniversities(): Flow<List<University>>
    fun getCredentials(): Flow<StudentCredentials?>
    suspend fun loadUniversitiesFromAssets(): List<University>
    suspend fun login(
        university: University,
        neptunCode: String,
        password: String,
        twoFactorCode: String = ""
    ): Result<StudentCredentials>
    suspend fun logout()
    suspend fun isOfflineModeAvailable(): Boolean
}
