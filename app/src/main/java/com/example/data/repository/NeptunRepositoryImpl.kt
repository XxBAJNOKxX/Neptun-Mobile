package com.example.data.repository

import com.example.core.security.EncryptedPreferencesManager
import com.example.data.local.NeptunDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.FinanceItemEntity
import com.example.data.local.entity.NeptunMessageEntity
import com.example.data.local.entity.SubjectGradeEntity
import com.example.data.network.MockNeptunDataSource
import com.example.data.network.NeptunApiClient
import com.example.data.network.NeptunAuthResult
import com.example.data.network.NeptunNetworkClient
import com.example.domain.model.CalendarEvent
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
    private val networkClient: NeptunNetworkClient,
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
                return refreshed
            }
        }

        if (currentToken.isNotBlank()) return currentToken

        if (creds.neptunCode.isNotEmpty() && password.isNotEmpty() && password != "******" && baseUrl.isNotEmpty()) {
            try {
                val authRes = neptunApiClient.authenticate(baseUrl, creds.neptunCode, password)
                if (authRes is NeptunAuthResult.Success && authRes.accessToken.isNotBlank()) {
                    prefsManager.setAccessToken(authRes.accessToken)
                    prefsManager.setBaseUrl(authRes.normalizedBaseUrl)
                    prefsManager.setIsModernApi(authRes.isModernApi)
                    return authRes.accessToken
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return currentToken
    }

    override suspend fun refreshCalendar(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var eventsToInsert = emptyList<CalendarEvent>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            try {
                val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                val token = ensureValidToken()
                val trainingId = prefsManager.getStudentTrainingId()
                val isModern = prefsManager.isModernApi()
                val password = prefsManager.getPassword()

                eventsToInsert = neptunApiClient.getCalendarEvents(
                    baseUrl = baseUrl,
                    token = token,
                    trainingId = trainingId.ifEmpty { null },
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (!fetchSucceeded || (isDemo && eventsToInsert.isEmpty())) {
            val existing = database.calendarDao().getAllEvents().first()
            if (existing.isEmpty()) {
                eventsToInsert = MockNeptunDataSource.getMockCalendarEvents()
            }
        }

        if (fetchSucceeded || eventsToInsert.isNotEmpty()) {
            database.calendarDao().clearAll()
            if (eventsToInsert.isNotEmpty()) {
                database.calendarDao().insertEvents(eventsToInsert.map { CalendarEventEntity.fromDomain(it) })
            }
        }

        Result.success(Unit)
    }

    override suspend fun refreshGrades(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var gradesToInsert = emptyList<SubjectGrade>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            try {
                val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                val token = ensureValidToken()
                val isModern = prefsManager.isModernApi()
                val password = prefsManager.getPassword()

                gradesToInsert = neptunApiClient.getGrades(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (!fetchSucceeded || (isDemo && gradesToInsert.isEmpty())) {
            val existing = database.gradesDao().getAllGrades().first()
            if (existing.isEmpty()) {
                gradesToInsert = MockNeptunDataSource.getMockGrades()
            }
        }

        if (fetchSucceeded || gradesToInsert.isNotEmpty()) {
            database.gradesDao().clearAll()
            if (gradesToInsert.isNotEmpty()) {
                database.gradesDao().insertGrades(gradesToInsert.map { SubjectGradeEntity.fromDomain(it) })
            }
        }

        Result.success(Unit)
    }

    override suspend fun refreshMessages(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var messagesToInsert = emptyList<NeptunMessage>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            try {
                val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                val token = ensureValidToken()
                val isModern = prefsManager.isModernApi()
                val password = prefsManager.getPassword()

                messagesToInsert = neptunApiClient.getMessages(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (!fetchSucceeded || (isDemo && messagesToInsert.isEmpty())) {
            val existing = database.messagesDao().getAllMessages().first()
            if (existing.isEmpty()) {
                messagesToInsert = MockNeptunDataSource.getMockMessages()
            }
        }

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
                database.messagesDao().insertMessages(entitiesToSave)
            }
        }

        Result.success(Unit)
    }

    override suspend fun refreshFinances(): Result<Unit> = withContext(Dispatchers.IO) {
        val creds = prefsManager.loadCredentials()
        var financesToInsert = emptyList<FinanceItem>()
        var fetchSucceeded = false

        if (creds != null && creds.neptunUrl.isNotEmpty()) {
            try {
                val baseUrl = prefsManager.getBaseUrl().ifEmpty { creds.neptunUrl }
                val token = ensureValidToken()
                val isModern = prefsManager.isModernApi()
                val password = prefsManager.getPassword()

                financesToInsert = neptunApiClient.getFinances(
                    baseUrl = baseUrl,
                    token = token,
                    username = creds.neptunCode,
                    password = password,
                    isModern = isModern
                )
                fetchSucceeded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val isDemo = creds?.neptunCode == "DEMO01"
        if (!fetchSucceeded || (isDemo && financesToInsert.isEmpty())) {
            val existing = database.financesDao().getAllFinances().first()
            if (existing.isEmpty()) {
                financesToInsert = MockNeptunDataSource.getMockFinances()
            }
        }

        if (fetchSucceeded || financesToInsert.isNotEmpty()) {
            database.financesDao().clearAll()
            if (financesToInsert.isNotEmpty()) {
                database.financesDao().insertFinances(financesToInsert.map { FinanceItemEntity.fromDomain(it) })
            }
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
                val token = prefsManager.getAccessToken()
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
                var token = prefsManager.getAccessToken()
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
                    database.messagesDao().updateMessageBody(
                        id = messageId,
                        bodyHtml = content,
                        previewText = content.take(150)
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
    }
}
