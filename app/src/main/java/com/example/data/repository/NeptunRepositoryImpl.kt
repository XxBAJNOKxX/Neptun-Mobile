package com.example.data.repository

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
import com.example.domain.model.CalendarEvent
import com.example.domain.model.ExamItem
import com.example.domain.model.FinanceItem
import com.example.domain.model.NeptunMessage
import com.example.domain.model.SubjectGrade
import com.example.domain.repository.NeptunRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    override suspend fun syncAllData(neptunCode: String, sessionToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = ensureValidToken(forceRefresh = false)
            val creds = prefsManager.loadCredentials()
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds?.neptunUrl ?: "" }
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
            prefsManager.updateLastSyncTime()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureValidToken(forceRefresh: Boolean = false): String {
        val creds = prefsManager.loadCredentials() ?: return ""
        val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
        val password = prefsManager.getPassword()
        val currentToken = prefsManager.getAccessToken()

        if (!forceRefresh && currentToken.isNotBlank()) return currentToken

        val refreshToken = prefsManager.getRefreshToken()
        if (prefsManager.isModernApi() && refreshToken.isNotBlank()) {
            val refreshed = neptunApiClient.refreshAccessToken(baseUrl, refreshToken)
            if (!refreshed.isNullOrBlank()) {
                prefsManager.setAccessToken(refreshed)
                prefsManager.clearSessionExpired()
                return refreshed
            }
        }

        if (creds.neptunCode.isNotEmpty() && password.isNotEmpty() && password != "******" && baseUrl.isNotEmpty()) {
            try {
                val authRes = neptunApiClient.authenticate(baseUrl, creds.neptunCode, password)
                if (authRes is NeptunAuthResult.Success && authRes.accessToken.isNotBlank()) {
                    prefsManager.setAccessToken(authRes.accessToken)
                    prefsManager.setBaseUrl(authRes.normalizedBaseUrl)
                    prefsManager.setIsModernApi(authRes.isModernApi)
                    authRes.refreshToken?.let { prefsManager.setRefreshToken(it) }
                    prefsManager.clearSessionExpired()
                    return authRes.accessToken
                } else if (authRes is NeptunAuthResult.Failure && prefsManager.isModernApi()) {
                    prefsManager.markSessionExpired()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (!forceRefresh) currentToken else ""
    }

    override suspend fun refreshCalendar(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var eventsToInsert = emptyList<CalendarEvent>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val trainingId = prefsManager.getStudentTrainingId()
            val password = prefsManager.getPassword()

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                eventsToInsert = neptunApiClient.getCalendarEvents(
                    baseUrl = baseUrl,
                    token = token,
                    trainingId = trainingId.ifEmpty { null },
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    try {
                        eventsToInsert = neptunApiClient.getCalendarEvents(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            trainingId = trainingId.ifEmpty { null },
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (fetchSucceeded) {
            prefsManager.setDataMode(if (isDemo) DataMode.DEMO else DataMode.REAL)
            if (eventsToInsert.isNotEmpty()) {
                database.calendarDao().clearAll()
                database.calendarDao().insertEvents(eventsToInsert.map { CalendarEventEntity.fromDomain(it) })
            }
        } else if (isDemo || (BuildConfig.DEBUG && database.calendarDao().getAllEvents().first().isEmpty())) {
            eventsToInsert = MockNeptunDataSource.getMockCalendarEvents()
            prefsManager.setDataMode(if (isDemo) DataMode.DEMO else DataMode.MOCK)
            database.calendarDao().clearAll()
            database.calendarDao().insertEvents(eventsToInsert.map { CalendarEventEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshGrades(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var gradesToInsert = emptyList<SubjectGrade>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                gradesToInsert = neptunApiClient.getGrades(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    try {
                        gradesToInsert = neptunApiClient.getGrades(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (fetchSucceeded) {
            if (gradesToInsert.isNotEmpty()) {
                prefsManager.setDataMode(if (isDemo) DataMode.DEMO else DataMode.REAL)
                database.gradesDao().clearAll()
                database.gradesDao().insertGrades(gradesToInsert.map { SubjectGradeEntity.fromDomain(it) })
            }
        } else if (isDemo || (BuildConfig.DEBUG && database.gradesDao().getAllGrades().first().isEmpty())) {
            gradesToInsert = MockNeptunDataSource.getMockGrades()
            prefsManager.setDataMode(if (isDemo) DataMode.DEMO else DataMode.MOCK)
            database.gradesDao().clearAll()
            database.gradesDao().insertGrades(gradesToInsert.map { SubjectGradeEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshMessages(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var messagesToInsert = emptyList<NeptunMessage>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                messagesToInsert = neptunApiClient.getMessages(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    try {
                        messagesToInsert = neptunApiClient.getMessages(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (fetchSucceeded || messagesToInsert.isNotEmpty()) {
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
                prefsManager.setDataMode(if (isDemo) DataMode.DEMO else DataMode.REAL)
                database.messagesDao().insertMessages(entitiesToSave)
            }
        } else if (isDemo || (BuildConfig.DEBUG && database.messagesDao().getAllMessages().first().isEmpty())) {
            messagesToInsert = MockNeptunDataSource.getMockMessages()
            prefsManager.setDataMode(if (isDemo) DataMode.DEMO else DataMode.MOCK)
            database.messagesDao().clearAll()
            database.messagesDao().insertMessages(messagesToInsert.map { NeptunMessageEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshFinances(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var financesToInsert = emptyList<FinanceItem>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                financesToInsert = neptunApiClient.getFinances(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    try {
                        financesToInsert = neptunApiClient.getFinances(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (fetchSucceeded) {
            if (financesToInsert.isNotEmpty()) {
                prefsManager.setDataMode(if (isDemo) DataMode.DEMO else DataMode.REAL)
            }
            database.financesDao().clearAll()
            if (financesToInsert.isNotEmpty()) {
                database.financesDao().insertFinances(financesToInsert.map { FinanceItemEntity.fromDomain(it) })
            }
        } else if (isDemo || (BuildConfig.DEBUG && database.financesDao().getAllFinances().first().isEmpty())) {
            financesToInsert = MockNeptunDataSource.getMockFinances()
            prefsManager.setDataMode(if (isDemo) DataMode.DEMO else DataMode.MOCK)
            database.financesDao().clearAll()
            database.financesDao().insertFinances(financesToInsert.map { FinanceItemEntity.fromDomain(it) })
        }

        Result.success(Unit)
    }

    override suspend fun refreshExams(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var examsToInsert = emptyList<ExamItem>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
            val password = prefsManager.getPassword()

            var token = ensureValidToken(forceRefresh = false)
            var isModern = prefsManager.isModernApi()

            try {
                examsToInsert = neptunApiClient.getExams(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: NeptunUnauthorizedException) {
                token = ensureValidToken(forceRefresh = true)
                if (token.isNotBlank()) {
                    val updatedBaseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                    val updatedIsModern = prefsManager.isModernApi()
                    try {
                        examsToInsert = neptunApiClient.getExams(
                            baseUrl = updatedBaseUrl,
                            token = token,
                            username = creds.neptunCode,
                            password = password,
                            isModern = updatedIsModern
                        )
                        fetchSucceeded = true
                    } catch (retryEx: Exception) {
                        retryEx.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (fetchSucceeded) {
            database.examsDao().clearAll()
            if (examsToInsert.isNotEmpty()) {
                database.examsDao().insertExams(examsToInsert.map { ExamItemEntity.fromDomain(it) })
            }
        } else if (isDemo || (BuildConfig.DEBUG && database.examsDao().getAllExams().first().isEmpty())) {
            examsToInsert = MockNeptunDataSource.getMockExams()
            database.examsDao().clearAll()
            database.examsDao().insertExams(examsToInsert.map { ExamItemEntity.fromDomain(it) })
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
                if (content.isBlank() && isModern && creds.neptunCode.isNotEmpty() && password.isNotEmpty()) {
                    val authRes = neptunApiClient.authenticate(baseUrl, creds.neptunCode, password)
                    if (authRes is NeptunAuthResult.Success && authRes.accessToken.isNotBlank()) {
                        token = authRes.accessToken
                        prefsManager.setAccessToken(token)
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
    }
}
