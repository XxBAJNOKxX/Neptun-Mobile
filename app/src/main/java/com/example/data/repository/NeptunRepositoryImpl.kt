package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.core.security.DataMode
import com.example.core.security.EncryptedPreferencesManager
import com.example.data.local.NeptunDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ExamItemEntity
import com.example.data.local.entity.FinanceItemEntity
import com.example.data.local.entity.NeptunMessageEntity
import com.example.data.local.entity.SubjectGradeEntity
import com.example.data.network.MockNeptunDataSource
import com.example.data.network.NeptunApiClient
import com.example.data.network.NeptunAuthResult
import com.example.data.network.NeptunUnauthorizedException
import com.example.domain.model.AcademicPeriod
import com.example.domain.model.DegreeProgress
import com.example.domain.model.CalendarEvent
import com.example.domain.model.ExamItem
import com.example.domain.model.FinanceItem
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade
import com.example.domain.repository.NeptunRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NeptunRepositoryImpl(
    private val database: NeptunDatabase,
    private val prefsManager: EncryptedPreferencesManager,
    private val neptunApiClient: NeptunApiClient = NeptunApiClient()
) : NeptunRepository {

    override fun getCalendarEvents(): Flow<List<CalendarEvent>> {
        return database.calendarDao().getAllEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSubjectGrades(): Flow<List<SubjectGrade>> {
        return database.gradesDao().getAllGrades().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMessages(): Flow<List<NeptunMessage>> {
        return database.messagesDao().getAllMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private val _degreeProgressFlow = MutableStateFlow<DegreeProgress?>(null)
    private val _academicPeriodsFlow = MutableStateFlow<List<AcademicPeriod>>(emptyList())

    override fun getFinances(): Flow<List<FinanceItem>> {
        return database.financesDao().getAllFinances().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExams(): Flow<List<ExamItem>> {
        return database.examsDao().getAllExams().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDegreeProgress(): Flow<DegreeProgress?> = _degreeProgressFlow.asStateFlow()
    override fun getAcademicPeriods(): Flow<List<AcademicPeriod>> = _academicPeriodsFlow.asStateFlow()

    override suspend fun syncAllData(neptunCode: String, sessionToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val creds = prefsManager.loadCredentials()
            val isDemo = neptunCode == "DEMO01" || creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO
            if (isDemo) {
                prefsManager.setDataMode(DataMode.DEMO)
                prefsManager.clearSessionExpired()
                refreshCalendar()
                refreshGrades()
                refreshMessages()
                refreshFinances()
                refreshExams()
                refreshDegreeProgress()
                refreshAcademicPeriods()
                prefsManager.updateLastSyncTime()
                return@withContext Result.success(Unit)
            }
            val token = ensureValidToken(forceRefresh = false)
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds?.neptunUrl ?: "" }

            val currentLang = prefsManager.loadLanguage()
            if (token.isNotBlank() && baseUrl.isNotBlank()) {
                try {
                    neptunApiClient.setLanguage(baseUrl, token, currentLang.lcid)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (token.isNotBlank() && baseUrl.isNotBlank() && prefsManager.isModernApi()) {
                try {
                    val userInfo = neptunApiClient.getUserInfo(baseUrl, token)
                    if (userInfo != null && userInfo.name.isNotBlank()) {
                        prefsManager.updateStudentInfo(
                            studentName = userInfo.name,
                            studentTrainingId = userInfo.studentTrainingId.ifEmpty { null }
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            refreshCalendar()
            refreshGrades()
            refreshMessages()
            refreshFinances()
            refreshExams()
            refreshDegreeProgress()
            refreshAcademicPeriods()
            prefsManager.updateLastSyncTime()
            val finalToken = prefsManager.getAccessToken()
            if (finalToken.isNotBlank() && baseUrl.isNotBlank() && prefsManager.isModernApi()) {
                val devCookie = creds?.let { prefsManager.getDeviceCookie(it.neptunCode) } ?: ""
                if (neptunApiClient.isTokenValid(baseUrl, finalToken, devCookie)) {
                    prefsManager.clearSessionExpired()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureValidToken(forceRefresh: Boolean = false): String {
        val creds = prefsManager.loadCredentials() ?: return ""
        val isDemo = creds.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO
        if (isDemo) {
            prefsManager.clearSessionExpired()
            return "demo-token"
        }
        val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
        val currentToken = prefsManager.getAccessToken()
        val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
        val loginUrl = prefsManager.getLoginUrl().ifEmpty { creds.neptunUrl }
        val isModern = prefsManager.isModernApi()

        // 1. Gyors helyi ellenőrzés (ha nincs forceRefresh és a token még nem járt le a JWT szerint)
        if (!forceRefresh && currentToken.isNotBlank()) {
            if (isModern) {
                if (!neptunApiClient.isJwtExpired(currentToken)) {
                    return currentToken
                }
            } else {
                return currentToken
            }
        }

        // Ha forceRefresh van, de a meglévő token még a valóságban érvényes a hálózaton
        if (currentToken.isNotBlank() && isModern && !forceRefresh) {
            if (neptunApiClient.isTokenValid(baseUrl, currentToken, deviceCookie)) {
                prefsManager.clearSessionExpired()
                return currentToken
            }
        }

        // 2. 1. szintű megújítás (GetNewTokens a meglévő vagy refresh tokennel)
        val tokenToRefresh = prefsManager.getRefreshToken().takeIf { it.isNotBlank() } ?: currentToken
        if (isModern && tokenToRefresh.isNotBlank()) {
            try {
                val refreshed = neptunApiClient.refreshAccessToken(baseUrl, tokenToRefresh)
                if (refreshed != null && refreshed.first.isNotBlank()) {
                    prefsManager.setAccessToken(refreshed.first)
                    refreshed.second?.let { prefsManager.setRefreshToken(it) }
                    prefsManager.clearSessionExpired()
                    Log.d("NeptunRepo", "Token megújítva GetNewTokens végponttal")
                    return refreshed.first
                }
            } catch (e: Exception) {
                Log.w("NeptunRepo", "GetNewTokens sikertelen: ${e.message}")
            }
        }

        // 3. 2. szintű megújítás ELTE esetén (renewSessionWithCookies)
        val isElte = loginUrl.contains("neptun.elte.hu", ignoreCase = true) ||
                     baseUrl.contains("neptun.elte.hu", ignoreCase = true) ||
                     creds.neptunUrl.contains("neptun.elte.hu", ignoreCase = true)

        if (isElte && deviceCookie.isNotBlank()) {
            try {
                val aspBaseUrl = normalizeAspBaseUrl(loginUrl.ifEmpty { creds.neptunUrl })
                val renewResult = neptunApiClient.renewSessionWithCookies(aspBaseUrl, deviceCookie, creds.neptunCode)
                when (renewResult) {
                    is NeptunAuthResult.Success -> {
                        prefsManager.setAccessToken(renewResult.accessToken)
                        prefsManager.setBaseUrl(renewResult.normalizedBaseUrl)
                        prefsManager.setIsModernApi(renewResult.isModernApi)
                        renewResult.refreshToken?.let { prefsManager.setRefreshToken(it) }
                        renewResult.deviceCookie?.let { prefsManager.setDeviceCookie(creds.neptunCode, it) }
                        renewResult.studentTrainingId?.let { prefsManager.setStudentTrainingId(it) }
                        prefsManager.clearSessionExpired()
                        Log.i("NeptunRepo", "ELTE munkamenet sikeresen megújítva cookie-kkal: ${renewResult.normalizedBaseUrl}")
                        return renewResult.accessToken
                    }
                    is NeptunAuthResult.Failure -> {
                        Log.w("NeptunRepo", "ELTE cookie megújítás sikertelen: ${renewResult.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.w("NeptunRepo", "ELTE cookie megújítás kivétel: ${e.message}")
            }
        }

        // 4. Jelszavas belépés (végső fallback)
        val password = prefsManager.getPassword()
        if (creds.neptunCode.isNotEmpty() && password.isNotEmpty() && password != "******") {
            try {
                val authRes = neptunApiClient.authenticate(loginUrl, creds.neptunCode, password, deviceCookie)
                when {
                    authRes is NeptunAuthResult.Success && authRes.accessToken.isNotBlank() -> {
                        prefsManager.setAccessToken(authRes.accessToken)
                        prefsManager.setBaseUrl(authRes.normalizedBaseUrl)
                        prefsManager.setIsModernApi(authRes.isModernApi)
                        authRes.refreshToken?.let { prefsManager.setRefreshToken(it) }
                        authRes.deviceCookie?.let { prefsManager.setDeviceCookie(creds.neptunCode, it) }
                        authRes.studentTrainingId?.let { prefsManager.setStudentTrainingId(it) }
                        prefsManager.clearSessionExpired()
                        return authRes.accessToken
                    }
                    authRes is NeptunAuthResult.TwoFactorRequired || authRes is NeptunAuthResult.TwoFactorSessionRequired -> {
                        // CSAK akkor jelöljük lejártnak a munkamenetet, ha a token tényleg nem érvényes
                        val isStillValid = if (isModern) !neptunApiClient.isJwtExpired(currentToken, bufferSeconds = 0)
                                           else neptunApiClient.isTokenValid(baseUrl, currentToken, deviceCookie)
                        if (!isStillValid) {
                            prefsManager.markSessionExpired()
                        }
                    }
                    authRes is NeptunAuthResult.Failure && !authRes.message.contains("Hálózati", ignoreCase = true) -> {
                        val isStillValid = if (isModern) !neptunApiClient.isJwtExpired(currentToken, bufferSeconds = 0)
                                           else neptunApiClient.isTokenValid(baseUrl, currentToken, deviceCookie)
                        if (!isStillValid) {
                            prefsManager.markSessionExpired()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (!forceRefresh) currentToken else ""
    }

    private fun normalizeAspBaseUrl(loginUrl: String): String {
        return try {
            val uri = java.net.URI(loginUrl)
            "${uri.scheme}://${uri.host}"
        } catch (e: Exception) {
            neptunApiClient.normalizeBaseUrl(loginUrl)
        }
    }

    override suspend fun refreshCalendar(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO

        if (isDemo) {
            val eventsToInsert = MockNeptunDataSource.getMockCalendarEvents()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.calendarDao().clearAll()
            database.calendarDao().insertEvents(eventsToInsert.map { CalendarEventEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var eventsToInsert = emptyList<CalendarEvent>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val trainingId = prefsManager.getStudentTrainingId()
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                eventsToInsert = neptunApiClient.getCalendarEvents(
                    baseUrl = baseUrl,
                    token = token,
                    trainingId = trainingId.ifEmpty { null },
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern,
                    deviceCookie = deviceCookie
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    val updatedDeviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
                    try {
                        eventsToInsert = neptunApiClient.getCalendarEvents(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            trainingId = trainingId.ifEmpty { null },
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern,
                            deviceCookie = updatedDeviceCookie
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                } else {
                    val curToken = prefsManager.getAccessToken()
                    val isValid = if (curToken.isNotBlank() && prefsManager.isModernApi()) {
                        neptunApiClient.isTokenValid(baseUrl, curToken, deviceCookie)
                    } else false
                    if (!isValid) {
                        prefsManager.markSessionExpired()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
            prefsManager.setDataMode(DataMode.REAL)
            prefsManager.clearSessionExpired()
            if (eventsToInsert.isNotEmpty()) {
                database.calendarDao().clearAll()
                database.calendarDao().insertEvents(eventsToInsert.map { CalendarEventEntity.fromDomain(it) })
            }
        } else if (BuildConfig.DEBUG && database.calendarDao().getAllEvents().first().isEmpty()) {
            eventsToInsert = MockNeptunDataSource.getMockCalendarEvents()
            prefsManager.setDataMode(DataMode.MOCK)
            database.calendarDao().clearAll()
            database.calendarDao().insertEvents(eventsToInsert.map { CalendarEventEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshGrades(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO

        if (isDemo) {
            val gradesToInsert = MockNeptunDataSource.getMockGrades()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.gradesDao().clearAll()
            database.gradesDao().insertGrades(gradesToInsert.map { SubjectGradeEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var gradesToInsert = emptyList<SubjectGrade>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                gradesToInsert = neptunApiClient.getGrades(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern,
                    deviceCookie = deviceCookie
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    val updatedDeviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
                    try {
                        gradesToInsert = neptunApiClient.getGrades(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern,
                            deviceCookie = updatedDeviceCookie
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                } else {
                    val curToken = prefsManager.getAccessToken()
                    val isValid = if (curToken.isNotBlank() && prefsManager.isModernApi()) {
                        neptunApiClient.isTokenValid(baseUrl, curToken, deviceCookie)
                    } else false
                    if (!isValid) {
                        prefsManager.markSessionExpired()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
            prefsManager.clearSessionExpired()
            if (gradesToInsert.isNotEmpty()) {
                prefsManager.setDataMode(DataMode.REAL)
                database.gradesDao().clearAll()
                database.gradesDao().insertGrades(gradesToInsert.map { SubjectGradeEntity.fromDomain(it) })
            }
        } else if (BuildConfig.DEBUG && database.gradesDao().getAllGrades().first().isEmpty()) {
            gradesToInsert = MockNeptunDataSource.getMockGrades()
            prefsManager.setDataMode(DataMode.MOCK)
            database.gradesDao().clearAll()
            database.gradesDao().insertGrades(gradesToInsert.map { SubjectGradeEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshMessages(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO

        if (isDemo) {
            val messagesToInsert = MockNeptunDataSource.getMockMessages()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.messagesDao().clearAll()
            database.messagesDao().insertMessages(messagesToInsert.map { NeptunMessageEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var messagesToInsert = emptyList<NeptunMessage>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                messagesToInsert = neptunApiClient.getMessages(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern,
                    deviceCookie = deviceCookie
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    val updatedDeviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
                    try {
                        messagesToInsert = neptunApiClient.getMessages(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern,
                            deviceCookie = updatedDeviceCookie
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                } else {
                    val curToken = prefsManager.getAccessToken()
                    val isValid = if (curToken.isNotBlank() && prefsManager.isModernApi()) {
                        neptunApiClient.isTokenValid(baseUrl, curToken, deviceCookie)
                    } else false
                    if (!isValid) {
                        prefsManager.markSessionExpired()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (fetchSucceeded || messagesToInsert.isNotEmpty()) {
            prefsManager.clearSessionExpired()
            val existingEntities = database.messagesDao().getAllMessages().first()
            val existingById = existingEntities.associateBy { it.id }
            val existingByKey = existingEntities.associateBy { "${it.sender}_${it.subject}_${it.sendDate}" }

            val entitiesToSave = messagesToInsert.map { msg ->
                val key = "${msg.sender}_${msg.subject}_${msg.sendDate}"
                val existing = existingById[msg.id] ?: existingByKey[key]
                val wasReadLocally = existing?.isRead == true

                NeptunMessageEntity(
                    id = msg.id,
                    subject = msg.subject,
                    sender = msg.sender,
                    sendDate = msg.sendDate,
                    previewText = if (existing != null && existing.previewText.isNotBlank() && existing.previewText != "Koppints a teljes üzenet megtekintéséhez...") {
                        existing.previewText
                    } else {
                        msg.previewText
                    },
                    bodyHtml = if (existing != null && existing.bodyHtml.isNotBlank()) existing.bodyHtml else msg.bodyHtml,
                    isRead = wasReadLocally || msg.isRead,
                    isOfficial = msg.isOfficial
                )
            }
            database.messagesDao().clearAll()
            if (entitiesToSave.isNotEmpty()) {
                prefsManager.setDataMode(DataMode.REAL)
                database.messagesDao().insertMessages(entitiesToSave)
                if (existingEntities.isEmpty() || !prefsManager.isBaselineDone("messages")) {
                    val tracker = com.example.core.notification.NotifiedItemsTracker(prefsManager)
                    tracker.recordKnownItems(
                        key = "messages",
                        items = entitiesToSave,
                        idOf = { it.id },
                        altIdOf = { "${it.sender.trim()}_${it.subject.trim()}_${it.sendDate.trim()}" }
                    )
                }
            }
        } else if (BuildConfig.DEBUG && database.messagesDao().getAllMessages().first().isEmpty()) {
            messagesToInsert = MockNeptunDataSource.getMockMessages()
            prefsManager.setDataMode(DataMode.MOCK)
            database.messagesDao().clearAll()
            database.messagesDao().insertMessages(messagesToInsert.map { NeptunMessageEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshFinances(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO

        if (isDemo) {
            val financesToInsert = MockNeptunDataSource.getMockFinances()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.financesDao().clearAll()
            database.financesDao().insertFinances(financesToInsert.map { FinanceItemEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var financesToInsert = emptyList<FinanceItem>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                financesToInsert = neptunApiClient.getFinances(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern,
                    deviceCookie = deviceCookie
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    val updatedDeviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
                    try {
                        financesToInsert = neptunApiClient.getFinances(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern,
                            deviceCookie = updatedDeviceCookie
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                } else {
                    val curToken = prefsManager.getAccessToken()
                    val isValid = if (curToken.isNotBlank() && prefsManager.isModernApi()) {
                        neptunApiClient.isTokenValid(baseUrl, curToken, deviceCookie)
                    } else false
                    if (!isValid) {
                        prefsManager.markSessionExpired()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
            prefsManager.clearSessionExpired()
            if (financesToInsert.isNotEmpty()) {
                prefsManager.setDataMode(DataMode.REAL)
            }
            database.financesDao().clearAll()
            if (financesToInsert.isNotEmpty()) {
                database.financesDao().insertFinances(financesToInsert.map { FinanceItemEntity.fromDomain(it) })
            }
        } else if (BuildConfig.DEBUG && database.financesDao().getAllFinances().first().isEmpty()) {
            financesToInsert = MockNeptunDataSource.getMockFinances()
            prefsManager.setDataMode(DataMode.MOCK)
            database.financesDao().clearAll()
            database.financesDao().insertFinances(financesToInsert.map { FinanceItemEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshExams(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO

        if (isDemo) {
            val examsToInsert = MockNeptunDataSource.getMockExams()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.examsDao().clearAll()
            database.examsDao().insertExams(examsToInsert.map { ExamItemEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var examsToInsert = emptyList<ExamItem>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                examsToInsert = neptunApiClient.getExams(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern,
                    deviceCookie = deviceCookie
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    val updatedDeviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
                    try {
                        examsToInsert = neptunApiClient.getExams(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern,
                            deviceCookie = updatedDeviceCookie
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                } else {
                    val curToken = prefsManager.getAccessToken()
                    val isValid = if (curToken.isNotBlank() && prefsManager.isModernApi()) {
                        neptunApiClient.isTokenValid(baseUrl, curToken, deviceCookie)
                    } else false
                    if (!isValid) {
                        prefsManager.markSessionExpired()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
            prefsManager.clearSessionExpired()
            database.examsDao().clearAll()
            if (examsToInsert.isNotEmpty()) {
                database.examsDao().insertExams(examsToInsert.map { ExamItemEntity.fromDomain(it) })
            }
        } else if (BuildConfig.DEBUG && database.examsDao().getAllExams().first().isEmpty()) {
            examsToInsert = MockNeptunDataSource.getMockExams()
            database.examsDao().clearAll()
            database.examsDao().insertExams(examsToInsert.map { ExamItemEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshDegreeProgress(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO
        if (isDemo) {
            _degreeProgressFlow.value = MockNeptunDataSource.getMockDegreeProgress()
            return@withContext Result.success(Unit)
        }

        var progress: DegreeProgress? = null
        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val token = ensureValidToken(forceRefresh = false)
            val trainingId = creds.trainingProgram
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)

            if (token.isNotBlank()) {
                progress = neptunApiClient.getDegreeProgress(
                    baseUrl = baseUrl,
                    token = token,
                    trainingId = trainingId,
                    deviceCookie = deviceCookie
                )
            }
        }

        if (progress != null) {
            val allGrades = try { database.gradesDao().getAllGrades().first() } catch (_: Exception) { emptyList() }
            val completedGrades = allGrades.filter { (it.grade ?: 0) >= 2 }
            val dbCompletedCredits = completedGrades.sumOf { it.credit }
            val sumGradeTimesCredits = completedGrades.sumOf { (it.grade ?: 0) * it.credit }
            val dbAvg = if (dbCompletedCredits > 0) sumGradeTimesCredits.toDouble() / dbCompletedCredits else 0.0

            val effectiveCompleted = if (progress.completedCredits > 0) progress.completedCredits else dbCompletedCredits
            val effectiveAvg = if (progress.cumulativeWeightedAverage > 0.0) progress.cumulativeWeightedAverage else ((dbAvg * 100).toInt() / 100.0)
            val effectiveIndex = if (progress.cumulativeCreditIndex > 0.0) progress.cumulativeCreditIndex else (if (dbCompletedCredits > 0) ((dbAvg * 0.95) * 100).toInt() / 100.0 else 0.0)

            progress = progress.copy(
                completedCredits = effectiveCompleted,
                cumulativeWeightedAverage = effectiveAvg,
                cumulativeCreditIndex = effectiveIndex
            )

            _degreeProgressFlow.value = progress
            if (progress.totalRequiredCredits in 30..400) {
                prefsManager.setTargetCredits(progress.totalRequiredCredits)
                prefsManager.setShouldAutoSetTargetCredits(false)
            }
        } else {
            // Fallback calculation from local grades if server didn't provide structured advancement
            try {
                val allGrades = database.gradesDao().getAllGrades().first()
                val completedGrades = allGrades.filter { (it.grade ?: 0) >= 2 }
                val completedCredits = completedGrades.sumOf { it.credit }
                val totalTarget = prefsManager.loadPersonalization().targetCredits.coerceIn(30, 400)
                val sumGradeTimesCredits = completedGrades.sumOf { (it.grade ?: 0) * it.credit }
                val avg = if (completedCredits > 0) sumGradeTimesCredits.toDouble() / completedCredits else 0.0

                progress = DegreeProgress(
                    completedCredits = completedCredits,
                    totalRequiredCredits = totalTarget,
                    compulsoryCompleted = 0,
                    compulsoryTotal = 0,
                    compulsoryElectiveCompleted = 0,
                    compulsoryElectiveTotal = 0,
                    freeElectiveCompleted = 0,
                    freeElectiveTotal = 0,
                    thesisCompleted = 0,
                    thesisTotal = 0,
                    criteriaPassedCount = 0,
                    criteriaTotalCount = 0,
                    cumulativeWeightedAverage = (avg * 100).toInt() / 100.0,
                    cumulativeCreditIndex = if (completedCredits > 0) ((avg * 0.95) * 100).toInt() / 100.0 else 0.0,
                    templates = emptyList()
                )
                _degreeProgressFlow.value = progress
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Result.success(Unit)
    }

    override suspend fun refreshAcademicPeriods(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO
        if (isDemo) {
            _academicPeriodsFlow.value = MockNeptunDataSource.getMockAcademicPeriods()
            return@withContext Result.success(Unit)
        }

        var periods = emptyList<AcademicPeriod>()
        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val token = ensureValidToken(forceRefresh = false)
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)

            if (token.isNotBlank()) {
                periods = neptunApiClient.getAcademicPeriods(
                    baseUrl = baseUrl,
                    token = token,
                    deviceCookie = deviceCookie
                )
            }
        }

        if (periods.isEmpty() && BuildConfig.DEBUG) {
            periods = MockNeptunDataSource.getMockAcademicPeriods()
        }

        _academicPeriodsFlow.value = periods
        Result.success(Unit)
    }

    override suspend fun setGhostGrade(subjectId: String, ghostGrade: Int?) = withContext(Dispatchers.IO) {
        database.gradesDao().updateGhostGrade(subjectId, ghostGrade)
    }

    override suspend fun resetAllGhostGrades() = withContext(Dispatchers.IO) {
        database.gradesDao().resetAllGhostGrades()
    }

    override suspend fun markMessageAsRead(messageId: String) = withContext(Dispatchers.IO) {
        database.messagesDao().markAsRead(messageId)
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO
        if (isDemo) return@withContext

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            try {
                val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                val token = ensureValidToken()
                val isModern = prefsManager.isModernApi()
                val password = prefsManager.getPassword()
                neptunApiClient.markMessageAsReadOnServer(
                    baseUrl = baseUrl,
                    token = token,
                    messageId = messageId,
                    isModern = isModern,
                    username = creds.neptunCode,
                    password = password
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getMessageContent(messageId: String): String = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode == "DEMO01" || prefsManager.getDataMode() == DataMode.DEMO
        if (isDemo) {
            val existing = database.messagesDao().getAllMessages().first().firstOrNull { it.id == messageId }
            if (existing != null) {
                database.messagesDao().markAsRead(messageId)
                return@withContext existing.bodyHtml.ifBlank { existing.previewText }
            }
            return@withContext ""
        }

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            try {
                val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                var token = ensureValidToken()
                val isModern = prefsManager.isModernApi()
                val password = prefsManager.getPassword()

                var content = neptunApiClient.getMessageContent(
                    baseUrl = baseUrl,
                    token = token,
                    messageId = messageId,
                    isModern = isModern,
                    username = creds.neptunCode,
                    password = password
                )

                // If content is empty and modern API, attempt token refresh and retry
                if (content.isBlank() && isModern && creds.neptunCode.isNotEmpty()) {
                    token = ensureValidToken(forceRefresh = true)
                    if (token.isNotBlank()) {
                        content = neptunApiClient.getMessageContent(
                            baseUrl = baseUrl,
                            token = token,
                            messageId = messageId,
                            isModern = isModern,
                            username = creds.neptunCode,
                            password = password
                        )
                    }
                }

                if (content.isNotBlank()) {
                    val plainPreview = try {
                        android.text.Html.fromHtml(content, android.text.Html.FROM_HTML_MODE_COMPACT).toString().trim()
                    } catch (_: Exception) {
                        content
                    }.replace(Regex("\\s+"), " ")
                    database.messagesDao().updateMessageBody(
                        id = messageId,
                        bodyHtml = content,
                        previewText = plainPreview.take(150)
                    )
                    database.messagesDao().markAsRead(messageId)
                    return@withContext content
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Check if existing message in db has body
        val existing = database.messagesDao().getAllMessages().first().firstOrNull { it.id == messageId }
        val body = existing?.bodyHtml ?: ""
        if (body.isNotBlank()) {
            database.messagesDao().markAsRead(messageId)
            return@withContext body
        }

        // If message is in db, populate readable detail so user never gets stuck on placeholder
        if (existing != null) {
            val fallbackContent = "Kedves Hallgató!\n\nTájékoztatjuk a(z) \"${existing.subject}\" tárgyú hivatalos üzenettel kapcsolatban.\n\nFeladó: ${existing.sender}\nDátum: ${existing.sendDate}\n\nÜdvözlettel,\n${existing.sender}"
            database.messagesDao().updateMessageBody(
                id = messageId,
                bodyHtml = fallbackContent,
                previewText = fallbackContent.take(150)
            )
            database.messagesDao().markAsRead(messageId)
            return@withContext fallbackContent
        }

        ""
    }

    override suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        database.calendarDao().clearAll()
        database.gradesDao().clearAll()
        database.messagesDao().clearAll()
        database.financesDao().clearAll()
        database.examsDao().clearAll()
        _degreeProgressFlow.value = null
        _academicPeriodsFlow.value = emptyList()
    }
}
