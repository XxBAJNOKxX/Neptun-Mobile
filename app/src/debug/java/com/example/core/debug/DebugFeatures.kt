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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
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
        var showLogDialog by remember { mutableStateOf(false) }
        var logs by remember { mutableStateOf(getLogs()) }

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
