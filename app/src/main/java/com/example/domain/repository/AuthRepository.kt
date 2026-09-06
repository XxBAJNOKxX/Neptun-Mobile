package com.example.domain.repository

import com.example.domain.model.Neptun2FASession
import com.example.domain.model.StudentCredentials
import com.example.domain.model.University
import kotlinx.coroutines.flow.Flow

class TwoFactorRequiredException(val twoFactorToken: String) :
    Exception("Kétlépcsős azonosítás (2FA) szükséges! Kérjük, add meg az SMS-ben vagy hitelesítő appban kapott kódot.")

class TwoFactorSessionRequiredException(val session: Neptun2FASession) :
    Exception("Kétlépcsős azonosítás (2FA) szükséges az ELTE / Neptun fiókhoz!")

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
    suspend fun request2FAEmailCode(session: Neptun2FASession): Result<Neptun2FASession>
    suspend fun verify2FACode(session: Neptun2FASession, code: String, isTotp: Boolean): Result<StudentCredentials>
    suspend fun logout()
    suspend fun isOfflineModeAvailable(): Boolean
    fun getSavedUniversityId(): String
    fun getSavedUniversityUrl(): String
    fun getSavedUniversityName(): String
    fun saveSelectedUniversity(university: University)
    fun getSavedNeptunCode(): String
    fun saveNeptunCode(code: String)
}

