package com.example.core.debug

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

data class DebugLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val maskedMessage: String,
    val isError: Boolean = false
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

object DebugFeatures {
    val isDebug: Boolean = true
    val isDemoAllowed: Boolean = true

    private const val MAX_LOGS = 150
    private val logBuffer = ConcurrentLinkedDeque<DebugLogEntry>()

    private val passwordRegex = Regex("""(?i)("?password"?\s*[:=]\s*"?)[^"&\s]+("?)""")
    private val tokenRegex = Regex("""(?i)("?(sessionToken|token|accessToken|bearer)"?\s*[:=]\s*"?)[^"&\s]+("?)""")
    private val cookieRegex = Regex("""(?i)((ASP\.NET_SessionId|\.ASPXAUTH|NeptunSession|deviceCookie)=)[^;,\s]+""")
    private val twoFaRegex = Regex("""(?i)("?(TOTPCode|EmailCode|twoFactorCode)"?\s*[:=]\s*"?)[^"&\s]+("?)""")
    private val verificationTokenRegex = Regex("""(?i)(__RequestVerificationToken=)[^;,\s&]+""")

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
        logNetworkSession("DebugFeatures", "StrictMode inicializálva a Debug variánsban.")
    }

    fun maskSensitive(text: String): String {
        return text
            .replace(passwordRegex, "$1***MASKED***$3")
            .replace(tokenRegex, "$1***MASKED***$3")
            .replace(cookieRegex, "$1***MASKED***")
            .replace(twoFaRegex, "$1***MASKED***$3")
            .replace(verificationTokenRegex, "$1***MASKED***")
    }

    fun logNetworkSession(tag: String, rawMessage: String, isError: Boolean = false) {
        val masked = maskSensitive(rawMessage)
        logBuffer.addLast(DebugLogEntry(tag = tag, maskedMessage = masked, isError = isError))
        while (logBuffer.size > MAX_LOGS) {
            logBuffer.pollFirst()
        }
    }

    fun getLogs(): List<DebugLogEntry> = logBuffer.toList().reversed()

    fun clearLogs() {
        logBuffer.clear()
    }

    @Composable
    fun DevMenuSection(
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        var showLogDialog by remember { mutableStateOf(false) }
        var logs by remember { mutableStateOf(getLogs()) }

        var refreshDbCountsKey by remember { mutableIntStateOf(0) }
        var dbEventsCount by remember { mutableStateOf<Int?>(null) }
        var dbGradesCount by remember { mutableStateOf<Int?>(null) }
        var dbMessagesCount by remember { mutableStateOf<Int?>(null) }
        var dbFinancesCount by remember { mutableStateOf<Int?>(null) }
        var dbExamsCount by remember { mutableStateOf<Int?>(null) }
        var isLoadingCounts by remember { mutableStateOf(false) }

        LaunchedEffect(refreshDbCountsKey) {
            isLoadingCounts = true
            try {
                withContext(Dispatchers.IO) {
                    val app = context.applicationContext as? com.example.NeptunApp
                    val db = app?.appContainer?.database
                    if (db != null) {
                        dbEventsCount = db.calendarDao().getAllEvents().first().size
                        dbGradesCount = db.gradesDao().getAllGrades().first().size
                        dbMessagesCount = db.messagesDao().getAllMessages().first().size
                        dbFinancesCount = db.financesDao().getAllFinances().first().size
                        dbExamsCount = db.examsDao().getAllExams().first().size
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingCounts = false
            }
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Debug Menü",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fejlesztői Eszközök (Debug)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "DEBUG",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Alkalmazás verzió: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Csomagnév: ${BuildConfig.APPLICATION_ID}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "StrictMode: Aktív | LeakCanary: Aktív",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Alkalmazásikon Előnézet (Launcher Preview)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(60.dp)
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    android.widget.ImageView(ctx).apply {
                                        setImageDrawable(ctx.packageManager.getApplicationIcon(ctx.packageName))
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Kerek (Pixel)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(60.dp)
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    android.widget.ImageView(ctx).apply {
                                        setImageDrawable(ctx.packageManager.getApplicationIcon(ctx.packageName))
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Squircle (OneUI)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(60.dp)
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    android.widget.ImageView(ctx).apply {
                                        setImageDrawable(ctx.packageManager.getApplicationIcon(ctx.packageName))
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Négyzet", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            logs = getLogs()
                            showLogDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Napló megtekintése")
                    }
                    OutlinedButton(
                        onClick = {
                            clearLogs()
                            logs = emptyList()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Napló törlése")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Szimulációs Eszközök",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val app = context.applicationContext as? com.example.NeptunApp
                        val prefs = app?.appContainer?.prefsManager
                        prefs?.clearSessionExpiredSnooze()
                        prefs?.markSessionExpired()
                        logNetworkSession("DebugFeatures", "Munkamenet lejárata szimulálva.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Munkamenet lejáratának szimulálása")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Adatbázis állapot / darabszámok",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { refreshDbCountsKey++ },
                        enabled = !isLoadingCounts
                    ) {
                        if (isLoadingCounts) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Darabszámok frissítése"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DbCountRow(label = "Órarend elemek", count = dbEventsCount)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        DbCountRow(label = "Érdemjegyek", count = dbGradesCount)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        DbCountRow(label = "Üzenetek", count = dbMessagesCount)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        DbCountRow(label = "Pénzügyek", count = dbFinancesCount)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        DbCountRow(label = "Vizsgák", count = dbExamsCount)
                    }
                }
            }
        }

        if (showLogDialog) {
            AlertDialog(
                onDismissRequest = { showLogDialog = false },
                title = { Text("Biztonságos Debug Napló") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        Text(
                            text = "Minden jelszó, token, cookie és 2FA kód szigorúan maszkolva van.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (logs.isEmpty()) {
                            Text("Nincsenek naplóbejegyzések.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(logs) { entry ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (entry.isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "[${entry.formattedTime}] ${entry.tag}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = entry.maskedMessage,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLogDialog = false }) {
                        Text("Bezárás")
                    }
                }
            )
        }
    }
}

@Composable
private fun DbCountRow(label: String, count: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (count != null) "$count db" else "...",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

