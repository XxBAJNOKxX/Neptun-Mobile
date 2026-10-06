package com.example.test

import com.example.domain.model.AcademicPeriod
import com.example.domain.model.CalendarEvent
import com.example.domain.model.DegreeProgress
import com.example.domain.model.ExamItem
import com.example.domain.model.FinanceItem
import com.example.domain.model.Neptun2FASession
import com.example.domain.model.NeptunLanguage
import com.example.domain.model.NeptunMessage
import com.example.domain.model.StudentCredentials
import com.example.domain.model.SubjectGrade
import com.example.domain.model.University
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.NeptunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {
    val universitiesList = listOf(
        University("bme", "Budapesti Műszaki és Gazdaságtudományi Egyetem", "BME", "Budapest", "https://neptun.bme.hu"),
        University("elte", "Eötvös Loránd Tudományegyetem", "ELTE", "Budapest", "https://neptun.elte.hu"),
        University("corvinus", "Budapesti Corvinus Egyetem", "Corvinus", "Budapest", "https://neptun.uni-corvinus.hu")
    )
    val credentialsFlow = MutableStateFlow<StudentCredentials?>(null)
    val languageFlow = MutableStateFlow(NeptunLanguage.HUNGARIAN)
    val supportedLanguagesFlow = MutableStateFlow(NeptunLanguage.DEFAULT_LANGUAGES)

    var storedUniId: String = "bme"
    var storedUniUrl: String = "https://neptun.bme.hu"
    var storedUniName: String = "Budapesti Műszaki és Gazdaságtudományi Egyetem"
    var storedNeptunCode: String = "TEST01"
    var offlineAvailable: Boolean = true

    override fun getUniversities(): Flow<List<University>> = MutableStateFlow(universitiesList)
    override fun getCredentials(): Flow<StudentCredentials?> = credentialsFlow.asStateFlow()
    override suspend fun loadUniversitiesFromAssets(): List<University> = universitiesList

    var shouldRequire2FASession: Boolean = false
    var twoFASessionToReturn: Neptun2FASession? = null
    var verify2FACodeResult: Result<StudentCredentials>? = null

    override suspend fun login(
        university: University,
        neptunCode: String,
        password: String,
        twoFactorCode: String
    ): Result<StudentCredentials> {
        if (shouldRequire2FASession) {
            val session = twoFASessionToReturn ?: Neptun2FASession(
                neptunCode = neptunCode,
                key = "KEY_123",
                phase = "RequestEmailCode",
                rendered = "",
                verificationToken = "token",
                hasTotp = true,
                hasEmail = true,
                codePrefix = "",
                cookies = emptyMap(),
                baseUrl = university.neptunUrl
            )
            return Result.failure(com.example.domain.repository.TwoFactorSessionRequiredException(session))
        }
        val creds = StudentCredentials(
            neptunCode = neptunCode,
            universityId = university.id,
            universityName = university.name,
            neptunUrl = university.neptunUrl,
            studentName = "Teszt Hallgató",
            trainingProgram = "Informatika BSc",
            isLoggedIn = true,
            lastSyncTime = System.currentTimeMillis()
        )
        credentialsFlow.value = creds
        return Result.success(creds)
    }

    override suspend fun request2FAEmailCode(session: Neptun2FASession): Result<Neptun2FASession> =
        Result.success(session.copy(codePrefix = "AZ", phase = "RequestEmailCode"))

    override suspend fun verify2FACode(session: Neptun2FASession, code: String, isTotp: Boolean): Result<StudentCredentials> {
        verify2FACodeResult?.let { return it }
        if (code != "123456") {
            return Result.failure(Exception("A megadott biztonsági kód érvénytelen vagy lejárt!"))
        }
        val creds = StudentCredentials(
            neptunCode = session.neptunCode,
            universityId = "elte",
            universityName = "ELTE",
            neptunUrl = session.baseUrl,
            studentName = "Teszt Hallgató",
            trainingProgram = "BSc",
            isLoggedIn = true,
            lastSyncTime = System.currentTimeMillis()
        )
        credentialsFlow.value = creds
        return Result.success(creds)
    }

    override suspend fun logout() {
        credentialsFlow.value = null
    }

    override suspend fun isOfflineModeAvailable(): Boolean = offlineAvailable
    override suspend fun continueOffline(): Result<StudentCredentials> {
        val creds = StudentCredentials(
            neptunCode = "OFFLINE01",
            universityId = "bme",
            universityName = "BME",
            neptunUrl = "https://neptun.bme.hu",
            studentName = "Offline Hallgató",
            trainingProgram = "BSc",
            isLoggedIn = true,
            lastSyncTime = System.currentTimeMillis()
        )
        credentialsFlow.value = creds
        return Result.success(creds)
    }

    override fun getSavedUniversityId(): String = storedUniId
    override fun getSavedUniversityUrl(): String = storedUniUrl
    override fun getSavedUniversityName(): String = storedUniName
    override fun saveSelectedUniversity(university: University) {
        storedUniId = university.id
        storedUniUrl = university.neptunUrl
        storedUniName = university.name
    }
    override fun getSavedNeptunCode(): String = storedNeptunCode
    override fun saveNeptunCode(code: String) {
        storedNeptunCode = code
    }
    override suspend fun getSupportedLanguages(universityUrl: String?): List<NeptunLanguage> = NeptunLanguage.DEFAULT_LANGUAGES
    override suspend fun setLanguage(language: NeptunLanguage): Result<Unit> {
        languageFlow.value = language
        return Result.success(Unit)
    }
    override fun getSelectedLanguage(): Flow<NeptunLanguage> = languageFlow.asStateFlow()
    override fun getCachedSupportedLanguages(): Flow<List<NeptunLanguage>> = supportedLanguagesFlow.asStateFlow()
    var storedPassword: String = ""
    override fun getSavedPassword(): String = storedPassword
    override fun savePassword(password: String) { storedPassword = password }
    override fun getSavedCredentials(): StudentCredentials? = credentialsFlow.value
    override fun clearSessionExpired() {}
}

class FakeNeptunRepository : NeptunRepository {
    val calendarEventsFlow = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val subjectGradesFlow = MutableStateFlow<List<SubjectGrade>>(emptyList())
    val messagesFlow = MutableStateFlow<List<NeptunMessage>>(emptyList())
    val financesFlow = MutableStateFlow<List<FinanceItem>>(emptyList())
    val examsFlow = MutableStateFlow<List<ExamItem>>(emptyList())
    val degreeProgressFlow = MutableStateFlow<DegreeProgress?>(null)
    val academicPeriodsFlow = MutableStateFlow<List<AcademicPeriod>>(emptyList())

    override fun getCalendarEvents(): Flow<List<CalendarEvent>> = calendarEventsFlow.asStateFlow()
    override fun getSubjectGrades(): Flow<List<SubjectGrade>> = subjectGradesFlow.asStateFlow()
    override fun getMessages(): Flow<List<NeptunMessage>> = messagesFlow.asStateFlow()
    override fun getFinances(): Flow<List<FinanceItem>> = financesFlow.asStateFlow()
    override fun getExams(): Flow<List<ExamItem>> = examsFlow.asStateFlow()
    override fun getDegreeProgress(): Flow<DegreeProgress?> = degreeProgressFlow.asStateFlow()
    override fun getAcademicPeriods(): Flow<List<AcademicPeriod>> = academicPeriodsFlow.asStateFlow()

    override suspend fun syncAllData(neptunCode: String, sessionToken: String): Result<Unit> = Result.success(Unit)
    override suspend fun refreshCalendar(): Result<Unit> = Result.success(Unit)
    override suspend fun refreshGrades(): Result<Unit> = Result.success(Unit)
    override suspend fun refreshMessages(): Result<Unit> = Result.success(Unit)
    override suspend fun refreshFinances(): Result<Unit> = Result.success(Unit)
    override suspend fun refreshExams(): Result<Unit> = Result.success(Unit)
    override suspend fun refreshDegreeProgress(): Result<Unit> = Result.success(Unit)
    override suspend fun refreshAcademicPeriods(): Result<Unit> = Result.success(Unit)

    override suspend fun setGhostGrade(subjectId: String, ghostGrade: Int?) {
        subjectGradesFlow.value = subjectGradesFlow.value.map {
            if (it.id == subjectId) it.copy(ghostGrade = ghostGrade) else it
        }
    }

    override suspend fun resetAllGhostGrades() {
        subjectGradesFlow.value = subjectGradesFlow.value.map {
            it.copy(ghostGrade = null)
        }
    }

    override suspend fun markMessageAsRead(messageId: String) {}
    override suspend fun getMessageContent(messageId: String): String = "Message body"
    override suspend fun clearLocalData() {
        calendarEventsFlow.value = emptyList()
        subjectGradesFlow.value = emptyList()
        messagesFlow.value = emptyList()
        financesFlow.value = emptyList()
        examsFlow.value = emptyList()
    }
    override suspend fun keepAliveSession(): Boolean = true
}
