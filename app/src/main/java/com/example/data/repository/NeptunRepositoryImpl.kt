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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class NeptunRepositoryImpl(
    private val database: NeptunDatabase,
    private val prefsManager: EncryptedPreferencesManager,
    private val neptunApiClient: NeptunApiClient = NeptunApiClient()
) : NeptunRepository {

    private val tokenMutex = Mutex()
    private val _calendarEventsFlow = MutableStateFlow<List<CalendarEvent>>(prefsManager.getCalendarEvents())

    override fun getCalendarEvents(): Flow<List<CalendarEvent>> = _calendarEventsFlow.asStateFlow()

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

    private val _degreeProgressFlow = MutableStateFlow<DegreeProgress?>(prefsManager.getDegreeProgress())
    private val _academicPeriodsFlow = MutableStateFlow<List<AcademicPeriod>>(prefsManager.getAcademicPeriods())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            database.calendarDao().getAllEvents().collect { entities ->
                val domainEvents = entities.map { it.toDomain() }
                _calendarEventsFlow.value = domainEvents
                if (domainEvents.isNotEmpty()) {
                    prefsManager.saveCalendarEvents(domainEvents)
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            prefsManager.personalizationFlow.collect { personalization ->
                _degreeProgressFlow.update { current ->
                    if (current != null && current.totalRequiredCredits != personalization.targetCredits) {
                        val updated = current.copy(totalRequiredCredits = personalization.targetCredits)
                        prefsManager.saveDegreeProgress(updated)
                        updated
                    } else {
                        current
                    }
                }
            }
        }
    }

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
            val isDemo = neptunCode.equals("DEMO01", ignoreCase = true) || creds?.neptunCode.equals("DEMO01", ignoreCase = true)
            if (isDemo) {
                prefsManager.setDataMode(DataMode.DEMO)
                prefsManager.clearSessionExpired()
                coroutineScope {
                    val d1 = async { refreshCalendar() }
                    val d2 = async { refreshGrades() }
                    val d3 = async { refreshMessages() }
                    val d4 = async { refreshFinances() }
                    val d5 = async { refreshExams() }
                    val d6 = async { refreshDegreeProgress() }
                    val d7 = async { refreshAcademicPeriods() }
                    d1.await()
                    d2.await()
                    d3.await()
                    d4.await()
                    d5.await()
                    d6.await()
                    d7.await()
                }
                prefsManager.updateLastSyncTime()
                return@withContext Result.success(Unit)
            }
            prefsManager.setDataMode(DataMode.REAL)
            cleanupMockDataIfPresent()
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
            val (calRes, grdRes, msgRes, finRes, exmRes) = coroutineScope {
                val calDeferred = async { refreshCalendar() }
                val grdDeferred = async { refreshGrades() }
                val msgDeferred = async { refreshMessages() }
                val finDeferred = async { refreshFinances() }
                val exmDeferred = async { refreshExams() }
                val degDeferred = async { refreshDegreeProgress() }
                val acaDeferred = async { refreshAcademicPeriods() }

                degDeferred.await()
                acaDeferred.await()

                listOf(
                    calDeferred.await(),
                    grdDeferred.await(),
                    msgDeferred.await(),
                    finDeferred.await(),
                    exmDeferred.await()
                )
            }
            prefsManager.updateLastSyncTime()
            val anySucceeded = calRes.isSuccess || grdRes.isSuccess || msgRes.isSuccess || finRes.isSuccess || exmRes.isSuccess
            val finalToken = prefsManager.getAccessToken()
            if (anySucceeded && finalToken.isNotBlank() && baseUrl.isNotBlank() && prefsManager.isModernApi()) {
                val devCookie = creds?.let { prefsManager.getDeviceCookie(it.neptunCode) } ?: ""
                if (neptunApiClient.isTokenValid(baseUrl, finalToken, devCookie)) {
                    prefsManager.clearSessionExpired()
                }
            }
            if (calRes.isFailure && grdRes.isFailure && msgRes.isFailure && finRes.isFailure && exmRes.isFailure) {
                Result.failure(calRes.exceptionOrNull() ?: IllegalStateException("Sync failed"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun cleanupMockDataIfPresent() {
        try {
            val mockIds = setOf("msg_0", "msg_1", "msg_2", "msg_3", "msg_4", "msg_5")
            val existing = database.messagesDao().getAllMessages().first()
            if (existing.any { it.id in mockIds }) {
                val realOnly = existing.filterNot { it.id in mockIds }
                database.messagesDao().replaceMessages(realOnly)
            }
        } catch (_: Exception) {
        }
    }

    private suspend fun ensureValidToken(forceRefresh: Boolean = false): String {
        val creds = prefsManager.loadCredentials() ?: return ""
        val isDemo = creds.neptunCode.equals("DEMO01", ignoreCase = true)
        if (isDemo) {
            prefsManager.clearSessionExpired()
            return "demo-token"
        }
        val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
        val currentToken = prefsManager.getAccessToken()
        val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
        val isModern = prefsManager.isModernApi()

        // 1. Gyors helyi ellenőrzés (ha nincs forceRefresh és a token még nem járt le)
        if (!forceRefresh && currentToken.isNotBlank()) {
            if (isModern) {
                if (!neptunApiClient.isJwtExpired(currentToken, bufferSeconds = 30L)) {
                    return currentToken
                }
            } else if (neptunApiClient.isTokenValid(baseUrl, currentToken, deviceCookie)) {
                return currentToken
            }
        }

        // Szinkronizált megújítás Mutex-szel, hogy a párhuzamos lekérések ne versengjenek egymással
        return tokenMutex.withLock {
            val freshToken = prefsManager.getAccessToken()
            val freshModern = prefsManager.isModernApi()
            val freshBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val freshDeviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
            val freshLoginUrl = prefsManager.getLoginUrl().ifEmpty { creds.neptunUrl }

            // Ha egy másik szál már sikeresen megújította a tokent amíg a zárra vártunk:
            if (!forceRefresh && freshToken.isNotBlank()) {
                if (freshModern) {
                    if (!neptunApiClient.isJwtExpired(freshToken, bufferSeconds = 30L)) {
                        return@withLock freshToken
                    }
                } else if (neptunApiClient.isTokenValid(freshBaseUrl, freshToken, freshDeviceCookie)) {
                    return@withLock freshToken
                }
            }

            // 2. 1. szintű megújítás (GetNewTokens a meglévő vagy refresh tokennel, sessionCookie-val és deviceCookie-val)
            val tokenToRefresh = prefsManager.getRefreshToken().takeIf { it.isNotBlank() } ?: freshToken
            val sessionCookie = prefsManager.getSessionCookie()
            if (freshModern && (tokenToRefresh.isNotBlank() || sessionCookie.isNotBlank())) {
                try {
                    val refreshed = neptunApiClient.refreshAccessToken(
                        baseUrl = freshBaseUrl,
                        tokenOrRefreshToken = tokenToRefresh.ifBlank { freshToken },
                        sessionCookie = sessionCookie,
                        deviceCookie = freshDeviceCookie,
                        username = creds.neptunCode
                    )
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        prefsManager.setAccessToken(refreshed.accessToken)
                        refreshed.refreshToken?.let { prefsManager.setRefreshToken(it) }
                        refreshed.sessionCookie?.let { prefsManager.setSessionCookie(it) }
                        refreshed.deviceCookie?.let { prefsManager.setDeviceCookie(creds.neptunCode, it) }
                        prefsManager.clearSessionExpired()
                        Log.d("NeptunRepo", "Token megújítva GetNewTokens végponttal")
                        return@withLock refreshed.accessToken
                    }
                } catch (e: Exception) {
                    Log.w("NeptunRepo", "GetNewTokens sikertelen: ${e.message}")
                }
            }

            // 3. 2. szintű megújítás ELTE esetén (renewSessionWithCookies)
            val isElte = freshLoginUrl.contains("neptun.elte.hu", ignoreCase = true) ||
                         freshBaseUrl.contains("neptun.elte.hu", ignoreCase = true) ||
                         creds.neptunUrl.contains("neptun.elte.hu", ignoreCase = true)

            if (isElte && freshDeviceCookie.isNotBlank()) {
                try {
                    val aspBaseUrl = normalizeAspBaseUrl(freshLoginUrl.ifEmpty { creds.neptunUrl })
                    val renewResult = neptunApiClient.renewSessionWithCookies(aspBaseUrl, freshDeviceCookie, creds.neptunCode)
                    when (renewResult) {
                        is NeptunAuthResult.Success -> {
                            prefsManager.setAccessToken(renewResult.accessToken)
                            prefsManager.setBaseUrl(renewResult.normalizedBaseUrl)
                            prefsManager.setIsModernApi(renewResult.isModernApi)
                            renewResult.refreshToken?.let { prefsManager.setRefreshToken(it) }
                            renewResult.sessionCookie?.let { prefsManager.setSessionCookie(it) }
                            renewResult.deviceCookie?.let { prefsManager.setDeviceCookie(creds.neptunCode, it) }
                            renewResult.studentTrainingId?.let { prefsManager.setStudentTrainingId(it) }
                            prefsManager.clearSessionExpired()
                            Log.i("NeptunRepo", "ELTE munkamenet sikeresen megújítva cookie-kkal: ${renewResult.normalizedBaseUrl}")
                            return@withLock renewResult.accessToken
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

            // 4. Jelszavas belépés (végső fallback kizárólag régebbi legacy WCF egyetemeknél)
            val password = prefsManager.getPassword()
            if (!freshModern && creds.neptunCode.isNotEmpty() && password.isNotEmpty() && password != "******") {
                try {
                    val authRes = neptunApiClient.authenticate(freshLoginUrl, creds.neptunCode, password, freshDeviceCookie)
                    when {
                        authRes is NeptunAuthResult.Success && authRes.accessToken.isNotBlank() -> {
                            prefsManager.setAccessToken(authRes.accessToken)
                            prefsManager.setBaseUrl(authRes.normalizedBaseUrl)
                            prefsManager.setIsModernApi(authRes.isModernApi)
                            authRes.refreshToken?.let { prefsManager.setRefreshToken(it) }
                            authRes.sessionCookie?.let { prefsManager.setSessionCookie(it) }
                            authRes.deviceCookie?.let { prefsManager.setDeviceCookie(creds.neptunCode, it) }
                            authRes.studentTrainingId?.let { prefsManager.setStudentTrainingId(it) }
                            prefsManager.clearSessionExpired()
                            return@withLock authRes.accessToken
                        }
                        authRes is NeptunAuthResult.TwoFactorRequired || authRes is NeptunAuthResult.TwoFactorSessionRequired -> {
                            val isStillValid = if (freshModern) !neptunApiClient.isJwtExpired(freshToken, bufferSeconds = 0)
                                               else neptunApiClient.isTokenValid(freshBaseUrl, freshToken, freshDeviceCookie)
                            if (!isStillValid) {
                                prefsManager.markSessionExpired()
                            }
                        }
                        authRes is NeptunAuthResult.Failure -> {
                            val msg = authRes.message.lowercase()
                            val isCredentialError = msg.contains("jelszó") ||
                                                    msg.contains("password") ||
                                                    msg.contains("felhasználónév") ||
                                                    msg.contains("user") ||
                                                    msg.contains("hitelesítés") ||
                                                    msg.contains("nem megfelelő") ||
                                                    msg.contains("érvénytelen") ||
                                                    msg.contains("letiltva") ||
                                                    msg.contains("fiók")
                            val isStillValid = if (freshModern) !neptunApiClient.isJwtExpired(freshToken, bufferSeconds = 0)
                                               else neptunApiClient.isTokenValid(freshBaseUrl, freshToken, freshDeviceCookie)
                            if (isCredentialError && !isStillValid) {
                                prefsManager.markSessionExpired()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (!forceRefresh) freshToken else ""
        }
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
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)

        if (isDemo) {
            val eventsToInsert = MockNeptunDataSource.getMockCalendarEvents()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.calendarDao().replaceEvents(eventsToInsert.map { CalendarEventEntity.fromDomain(it) })
            _calendarEventsFlow.value = eventsToInsert
            prefsManager.saveCalendarEvents(eventsToInsert)
            return@withContext Result.success(Unit)
        }

        var eventsToInsert = emptyList<CalendarEvent>()
        var fetchSucceeded = false
        var fetchError: Throwable? = null

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            var token = ensureValidToken(forceRefresh = false)
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val trainingId = prefsManager.getStudentTrainingId()
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
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
                        fetchError = retryEx
                        retryEx.printStackTrace()
                    }
                } else {
                    fetchError = e
                }
            } catch (e: Exception) {
                fetchError = e
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
            prefsManager.setDataMode(DataMode.REAL)
            if (eventsToInsert.isNotEmpty()) {
                prefsManager.clearSessionExpired()
                database.calendarDao().replaceEvents(eventsToInsert.map { CalendarEventEntity.fromDomain(it) })
                _calendarEventsFlow.value = eventsToInsert
                prefsManager.saveCalendarEvents(eventsToInsert)
                return@withContext Result.success(Unit)
            } else {
                Log.w("NeptunRepo", "Üres órarend érkezett a szervertől, meglévő adatok megőrzése.")
                return@withContext Result.failure(IllegalStateException("Üres órarend érkezett a szervertől"))
            }
        }

        Result.failure(fetchError ?: IllegalStateException("Failed to fetch calendar events from server"))
    }

    override suspend fun refreshGrades(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)

        if (isDemo) {
            val gradesToInsert = MockNeptunDataSource.getMockGrades()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.gradesDao().replaceGrades(gradesToInsert.map { SubjectGradeEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var gradesToInsert = emptyList<SubjectGrade>()
        var fetchSucceeded = false
        var fetchError: Throwable? = null

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            var token = ensureValidToken(forceRefresh = false)
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
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
                        fetchError = retryEx
                        retryEx.printStackTrace()
                    }
                } else {
                    fetchError = e
                }
            } catch (e: Exception) {
                fetchError = e
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
            prefsManager.setDataMode(DataMode.REAL)
            if (gradesToInsert.isNotEmpty()) {
                prefsManager.clearSessionExpired()
                val existingGrades = database.gradesDao().getAllGrades().first()
                val ghostMap = existingGrades.mapNotNull { if (it.ghostGrade != null) it.id to it.ghostGrade else null }.toMap()
                val entitiesToSave = gradesToInsert.map { grade ->
                    val ghost = ghostMap[grade.id]
                    SubjectGradeEntity.fromDomain(if (ghost != null) grade.copy(ghostGrade = ghost) else grade)
                }
                database.gradesDao().replaceGrades(entitiesToSave)
                return@withContext Result.success(Unit)
            } else {
                Log.w("NeptunRepo", "Üres jegylista érkezett a szervertől, meglévő adatok megőrzése.")
                return@withContext Result.failure(IllegalStateException("Üres jegylista érkezett a szervertől"))
            }
        }

        Result.failure(fetchError ?: IllegalStateException("Failed to fetch grades from server"))
    }

    override suspend fun refreshMessages(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)

        if (isDemo) {
            val messagesToInsert = MockNeptunDataSource.getMockMessages()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.messagesDao().replaceMessages(messagesToInsert.map { NeptunMessageEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var messagesToInsert = emptyList<NeptunMessage>()
        var fetchSucceeded = false
        var fetchError: Throwable? = null

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            var token = ensureValidToken(forceRefresh = false)
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
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
                        fetchError = retryEx
                        retryEx.printStackTrace()
                    }
                } else {
                    fetchError = e
                }
            } catch (e: Exception) {
                fetchError = e
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
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
            if (entitiesToSave.isNotEmpty()) {
                prefsManager.clearSessionExpired()
                prefsManager.setDataMode(DataMode.REAL)
                database.messagesDao().replaceMessages(entitiesToSave)
                if (existingEntities.isEmpty() || !prefsManager.isBaselineDone("messages")) {
                    val tracker = com.example.core.notification.NotifiedItemsTracker(prefsManager)
                    tracker.recordKnownItems(
                        key = "messages",
                        items = entitiesToSave,
                        idOf = { it.id },
                        altIdOf = { "${it.sender.trim()}_${it.subject.trim()}_${it.sendDate.trim()}" }
                    )
                }
                return@withContext Result.success(Unit)
            } else {
                Log.w("NeptunRepo", "Üres üzenetlista érkezett a szervertől, meglévő adatok megőrzése.")
                return@withContext Result.failure(IllegalStateException("Üres üzenetlista érkezett a szervertől"))
            }
        }

        Result.failure(fetchError ?: IllegalStateException("Failed to fetch messages from server"))
    }


    override suspend fun refreshFinances(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)

        if (isDemo) {
            val financesToInsert = MockNeptunDataSource.getMockFinances()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.financesDao().replaceFinances(financesToInsert.map { FinanceItemEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var financesToInsert = emptyList<FinanceItem>()
        var fetchSucceeded = false
        var fetchError: Throwable? = null

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            var token = ensureValidToken(forceRefresh = false)
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
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
                        fetchError = retryEx
                        retryEx.printStackTrace()
                    }
                } else {
                    fetchError = e
                }
            } catch (e: Exception) {
                fetchError = e
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
            prefsManager.setDataMode(DataMode.REAL)
            if (financesToInsert.isNotEmpty()) {
                prefsManager.clearSessionExpired()
                database.financesDao().replaceFinances(financesToInsert.map { FinanceItemEntity.fromDomain(it) })
                return@withContext Result.success(Unit)
            } else {
                Log.w("NeptunRepo", "Üres pénzügyi lista érkezett a szervertől, meglévő adatok megőrzése.")
                return@withContext Result.failure(IllegalStateException("Üres pénzügyi lista érkezett a szervertől"))
            }
        }

        Result.failure(fetchError ?: IllegalStateException("Failed to fetch finances from server"))
    }

    override suspend fun refreshExams(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)

        if (isDemo) {
            val examsToInsert = MockNeptunDataSource.getMockExams()
            prefsManager.setDataMode(DataMode.DEMO)
            prefsManager.clearSessionExpired()
            database.examsDao().replaceExams(examsToInsert.map { ExamItemEntity.fromDomain(it) })
            return@withContext Result.success(Unit)
        }

        var examsToInsert = emptyList<ExamItem>()
        var fetchSucceeded = false
        var fetchError: Throwable? = null

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            var token = ensureValidToken(forceRefresh = false)
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()
            val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
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
                        fetchError = retryEx
                        retryEx.printStackTrace()
                    }
                } else {
                    fetchError = e
                }
            } catch (e: Exception) {
                fetchError = e
                e.printStackTrace()
            }
        }

        if (fetchSucceeded) {
            prefsManager.setDataMode(DataMode.REAL)
            if (examsToInsert.isNotEmpty()) {
                prefsManager.clearSessionExpired()
                database.examsDao().replaceExams(examsToInsert.map { ExamItemEntity.fromDomain(it) })
                return@withContext Result.success(Unit)
            } else {
                Log.w("NeptunRepo", "Üres vizsgalista érkezett a szervertől, meglévő adatok megőrzése.")
                return@withContext Result.failure(IllegalStateException("Üres vizsgalista érkezett a szervertől"))
            }
        }

        Result.failure(fetchError ?: IllegalStateException("Failed to fetch exams from server"))
    }

    override suspend fun refreshDegreeProgress(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)
        if (isDemo) {
            val mock = MockNeptunDataSource.getMockDegreeProgress()
            _degreeProgressFlow.value = mock
            prefsManager.saveDegreeProgress(mock)
            return@withContext Result.success(Unit)
        }

        var progress: DegreeProgress? = null
        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val token = ensureValidToken(forceRefresh = false)
            val trainingId = prefsManager.getStudentTrainingId().ifEmpty { creds.trainingProgram }
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

            val userTarget = prefsManager.loadPersonalization().targetCredits.coerceIn(30, 400)
            val effectiveTarget = if (!prefsManager.getShouldAutoSetTargetCredits()) {
                userTarget
            } else if (progress.totalRequiredCredits in 30..400) {
                progress.totalRequiredCredits
            } else {
                userTarget
            }

            progress = progress.copy(
                totalRequiredCredits = effectiveTarget,
                completedCredits = effectiveCompleted,
                cumulativeWeightedAverage = effectiveAvg,
                cumulativeCreditIndex = effectiveIndex
            )

            _degreeProgressFlow.value = progress
            prefsManager.saveDegreeProgress(progress)
            if (prefsManager.getShouldAutoSetTargetCredits() && progress.totalRequiredCredits in 30..400) {
                prefsManager.setTargetCredits(progress.totalRequiredCredits)
            }
        } else {
            val existingProgress = _degreeProgressFlow.value ?: prefsManager.getDegreeProgress()
            if (existingProgress != null && (existingProgress.templates.isNotEmpty() || existingProgress.totalCurriculums > 0)) {
                val allGrades = try { database.gradesDao().getAllGrades().first() } catch (_: Exception) { emptyList() }
                val completedGrades = allGrades.filter { (it.grade ?: 0) >= 2 }
                val completedCredits = completedGrades.sumOf { it.credit }
                val sumGradeTimesCredits = completedGrades.sumOf { (it.grade ?: 0) * it.credit }
                val avg = if (completedCredits > 0) sumGradeTimesCredits.toDouble() / completedCredits else 0.0
                val userTarget = prefsManager.loadPersonalization().targetCredits.coerceIn(30, 400)
                val effectiveTarget = if (!prefsManager.getShouldAutoSetTargetCredits()) userTarget else existingProgress.totalRequiredCredits
                val updated = existingProgress.copy(
                    completedCredits = if (completedCredits > 0) completedCredits else existingProgress.completedCredits,
                    totalRequiredCredits = effectiveTarget,
                    cumulativeWeightedAverage = if (avg > 0.0) ((avg * 100).toInt() / 100.0) else existingProgress.cumulativeWeightedAverage,
                    cumulativeCreditIndex = if (completedCredits > 0) ((avg * 0.95) * 100).toInt() / 100.0 else existingProgress.cumulativeCreditIndex
                )
                _degreeProgressFlow.value = updated
                prefsManager.saveDegreeProgress(updated)
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
                    prefsManager.saveDegreeProgress(progress)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        Result.success(Unit)
    }

    override suspend fun refreshAcademicPeriods(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)
        if (isDemo) {
            val mock = MockNeptunDataSource.getMockAcademicPeriods()
            _academicPeriodsFlow.value = mock
            prefsManager.saveAcademicPeriods(mock)
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

        if (periods.isNotEmpty()) {
            _academicPeriodsFlow.value = periods
            prefsManager.saveAcademicPeriods(periods)
        } else {
            val existing = _academicPeriodsFlow.value.ifEmpty { prefsManager.getAcademicPeriods() }
            _academicPeriodsFlow.value = existing
        }

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
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)
        if (isDemo) return@withContext

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            try {
                val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                val token = ensureValidToken()
                val isModern = prefsManager.isModernApi()
                val password = prefsManager.getPassword()
                val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)
                neptunApiClient.markMessageAsReadOnServer(
                    baseUrl = baseUrl,
                    token = token,
                    messageId = messageId,
                    isModern = isModern,
                    username = creds.neptunCode,
                    password = password,
                    deviceCookie = deviceCookie
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun getMessageContent(messageId: String): String = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        val isDemo = creds?.neptunCode.equals("DEMO01", ignoreCase = true)
        if (isDemo) {
            val existing = database.messagesDao().getAllMessages().first().firstOrNull { it.id == messageId }
            if (existing != null) {
                database.messagesDao().markAsRead(messageId)
                return@withContext existing.bodyHtml.ifBlank { existing.previewText }
            }
            return@withContext ""
        }

        // 1. Check if existing message in local db already has content
        val cached = database.messagesDao().getAllMessages().first().firstOrNull { it.id == messageId }
        if (cached != null && cached.bodyHtml.isNotBlank()) {
            database.messagesDao().markAsRead(messageId)
            return@withContext cached.bodyHtml
        }

        // 2. Fetch from server
        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            try {
                val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                var token = ensureValidToken()
                val isModern = prefsManager.isModernApi()
                val password = prefsManager.getPassword()
                val deviceCookie = prefsManager.getDeviceCookie(creds.neptunCode)

                var content = neptunApiClient.getMessageContent(
                    baseUrl = baseUrl,
                    token = token,
                    messageId = messageId,
                    isModern = isModern,
                    username = creds.neptunCode,
                    password = password,
                    deviceCookie = deviceCookie
                )

                // If content is empty and modern API, attempt token refresh and retry
                if (content.isBlank() && isModern && creds.neptunCode.isNotEmpty()) {
                    token = ensureValidToken(forceRefresh = true)
                    if (token.isNotBlank()) {
                        val updatedCookie = prefsManager.getDeviceCookie(creds.neptunCode)
                        content = neptunApiClient.getMessageContent(
                            baseUrl = baseUrl,
                            token = token,
                            messageId = messageId,
                            isModern = isModern,
                            username = creds.neptunCode,
                            password = password,
                            deviceCookie = updatedCookie
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

        ""
    }

    override suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        database.calendarDao().clearAll()
        database.gradesDao().clearAll()
        database.messagesDao().clearAll()
        database.financesDao().clearAll()
        database.examsDao().clearAll()
        _calendarEventsFlow.value = emptyList()
        prefsManager.clearCalendarEvents()
        _degreeProgressFlow.value = null
        prefsManager.clearDegreeProgress()
        _academicPeriodsFlow.value = emptyList()
        prefsManager.clearAcademicPeriods()
    }
}
