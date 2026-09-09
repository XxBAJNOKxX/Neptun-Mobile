package com.example.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.core.network.SslTrustHelper
import com.example.core.security.UpdateChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val tagName: String
)

sealed class InAppUpdateState {
    object Idle : InAppUpdateState()
    object Checking : InAppUpdateState()
    data class UpdateAvailable(val info: UpdateInfo) : InAppUpdateState()
    data class Downloading(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val info: UpdateInfo
    ) : InAppUpdateState()
    data class ReadyToInstall(val apkFile: File, val info: UpdateInfo) : InAppUpdateState()
    data class Error(val message: String) : InAppUpdateState()
}

class AppUpdateManager(
    private val client: OkHttpClient = SslTrustHelper.configureOkHttpClient(
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
    ).build()
) {

    suspend fun checkForUpdates(channel: UpdateChannel = UpdateChannel.STABLE): UpdateInfo? = withContext(Dispatchers.IO) {
        val repo = BuildConfig.GITHUB_REPO
        try {
            val url = if (channel == UpdateChannel.STABLE) {
                "https://api.github.com/repos/$repo/releases/latest"
            } else {
                "https://api.github.com/repos/$repo/releases"
            }

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "NeptunMobileApp")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val body = response.body?.string() ?: return@withContext null
                val releaseObj: JSONObject = if (channel == UpdateChannel.DEV) {
                    val jsonArray = JSONArray(body)
                    if (jsonArray.length() == 0) return@withContext null
                    jsonArray.getJSONObject(0)
                } else {
                    JSONObject(body)
                }

                val tagName = releaseObj.optString("tag_name", "").trim()
                val htmlUrl = releaseObj.optString("html_url", "https://github.com/$repo/releases")
                val releaseNotes = releaseObj.optString("body", "").trim()

                var apkDownloadUrl: String? = null
                val assets = releaseObj.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url")
                            break
                        }
                    }
                }

                val targetDownloadUrl = apkDownloadUrl ?: htmlUrl
                val currentVersion = BuildConfig.VERSION_NAME
                val isNewer = isNewerVersion(tagName, currentVersion)

                UpdateInfo(
                    isUpdateAvailable = isNewer,
                    currentVersion = currentVersion,
                    latestVersion = tagName.removePrefix("v"),
                    downloadUrl = targetDownloadUrl,
                    releaseNotes = releaseNotes,
                    tagName = tagName
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float, Long, Long) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(downloadUrl)
            .header("User-Agent", "NeptunMobileApp")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("A letöltés meghiúsult: HTTP ${response.code}")
        }

        val body = response.body ?: throw IllegalStateException("Üres letöltési válasz érkezett")
        val contentLength = body.contentLength()

        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = File(updatesDir, "neptun-update.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }

        body.byteStream().use { input ->
            FileOutputStream(apkFile).use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = if (contentLength > 0) {
                        (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0.5f
                    }
                    onProgress(progress, totalBytesRead, contentLength)
                }
                output.flush()
            }
        }

        apkFile
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isNewerVersion(latest: String, current: String): Boolean {
        val lVer = ParsedVersion.parse(latest)
        val cVer = ParsedVersion.parse(current)
        return lVer.isNewerThan(cVer)
    }

    private data class ParsedVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val isDev: Boolean
    ) {
        fun isNewerThan(other: ParsedVersion): Boolean {
            if (this.major != other.major) return this.major > other.major
            if (this.minor != other.minor) return this.minor > other.minor
            if (this.patch != other.patch) return this.patch > other.patch
            if (this.isDev != other.isDev) {
                return !this.isDev && other.isDev
            }
            return false
        }

        companion object {
            fun parse(raw: String): ParsedVersion {
                val clean = raw.removePrefix("v").trim()
                val isDev = clean.contains("dev", ignoreCase = true)
                val numbers = Regex("\\d+").findAll(clean).map { it.value.toInt() }.toList()
                val major = numbers.getOrElse(0) { 0 }
                val minor = numbers.getOrElse(1) { 0 }
                val patch = numbers.getOrElse(2) { 0 }
                return ParsedVersion(major, minor, patch, isDev)
            }
        }
    }
}
