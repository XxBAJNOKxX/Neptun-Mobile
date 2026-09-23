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
    val tagName: String,
    val assetName: String = "",
    val expectedSizeBytes: Long = 0L
)

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String = "",
    val size: Long = 0L
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

    suspend fun checkForUpdates(
        channel: UpdateChannel = UpdateChannel.STABLE,
        isDebugApp: Boolean = isCurrentAppDebug()
    ): UpdateInfo? = withContext(Dispatchers.IO) {
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

                    var bestRelease: JSONObject? = null
                    var bestParsedVersion: ParsedVersion? = null

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.optJSONObject(i) ?: continue
                        val tag = item.optString("tag_name", "").trim()
                        if (tag.isEmpty()) continue

                        val assets = item.optJSONArray("assets")
                        var hasApk = false
                        if (assets != null) {
                            for (j in 0 until assets.length()) {
                                if (assets.optJSONObject(j)?.optString("name", "")?.endsWith(".apk", ignoreCase = true) == true) {
                                    hasApk = true
                                    break
                                }
                            }
                        }
                        if (!hasApk) continue

                        val parsed = ParsedVersion.parse(tag)
                        if (bestParsedVersion == null || parsed.isNewerThan(bestParsedVersion)) {
                            bestParsedVersion = parsed
                            bestRelease = item
                        }
                    }

                    bestRelease ?: jsonArray.getJSONObject(0)
                } else {
                    JSONObject(body)
                }

                val tagName = releaseObj.optString("tag_name", "").trim()
                val htmlUrl = releaseObj.optString("html_url", "https://github.com/$repo/releases")
                val releaseNotes = releaseObj.optString("body", "").trim()

                val assets = releaseObj.optJSONArray("assets")
                val chosenAsset = selectBestApkAsset(assets, isCurrentAppDebug = isDebugApp)
                val apkDownloadUrl = chosenAsset?.optString("browser_download_url")
                val assetName = chosenAsset?.optString("name", "") ?: ""
                val expectedSizeBytes = chosenAsset?.optLong("size", 0L) ?: 0L

                val targetDownloadUrl = apkDownloadUrl ?: htmlUrl
                val currentVersion = BuildConfig.VERSION_NAME
                val isNewer = isNewerVersion(tagName, currentVersion)

                UpdateInfo(
                    isUpdateAvailable = isNewer,
                    currentVersion = currentVersion,
                    latestVersion = tagName.removePrefix("v"),
                    downloadUrl = targetDownloadUrl,
                    releaseNotes = releaseNotes,
                    tagName = tagName,
                    assetName = assetName,
                    expectedSizeBytes = expectedSizeBytes
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun downloadApk(
        context: Context,
        info: UpdateInfo,
        onProgress: (Float, Long, Long) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val baseName = getSafeUpdateFileName(info.tagName, info.assetName, info.downloadUrl)
        val finalFile = File(updatesDir, "$baseName.apk")
        val partFile = File(updatesDir, "$baseName.apk.part")

        // 1. Ha a végleges fájl már létezik és mérete egyezik (vagy nem ismert a méret)
        if (finalFile.exists() && finalFile.length() > 0) {
            if (info.expectedSizeBytes <= 0 || finalFile.length() == info.expectedSizeBytes) {
                onProgress(1f, finalFile.length(), finalFile.length())
                return@withContext finalFile
            } else {
                finalFile.delete()
            }
        }

        // 2. Részleges letöltés (.part) ellenőrzése a folytatáshoz
        var existingBytes = if (partFile.exists()) partFile.length() else 0L

        if (info.expectedSizeBytes > 0 && existingBytes >= info.expectedSizeBytes) {
            partFile.delete()
            existingBytes = 0L
        }

        val requestBuilder = Request.Builder()
            .url(info.downloadUrl)
            .header("User-Agent", "NeptunMobileApp")

        if (existingBytes > 0) {
            requestBuilder.header("Range", "bytes=$existingBytes-")
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val responseCode = response.code

        if (responseCode == 416) {
            // Range Not Satisfiable: sérült vagy nem érvényes offset, újrakezdjük 0-ról
            response.close()
            partFile.delete()
            return@withContext downloadApk(context, info.copy(expectedSizeBytes = 0), onProgress)
        }

        val isPartial = responseCode == 206
        val isOk = responseCode == 200

        if (!isPartial && !isOk) {
            response.close()
            throw IllegalStateException("A letöltés meghiúsult: HTTP $responseCode")
        }

        val body = response.body ?: throw IllegalStateException("Üres letöltési válasz érkezett")
        val bodyLength = body.contentLength()

        val totalBytes = if (isPartial) {
            val contentRange = response.header("Content-Range")
            val totalFromHeader = contentRange?.substringAfterLast('/')?.toLongOrNull()
            totalFromHeader ?: if (bodyLength > 0) existingBytes + bodyLength else info.expectedSizeBytes
        } else {
            existingBytes = 0L
            if (bodyLength > 0) bodyLength else info.expectedSizeBytes
        }

        val append = isPartial && existingBytes > 0
        body.byteStream().use { input ->
            FileOutputStream(partFile, append).use { output ->
                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int
                var totalBytesRead = existingBytes

                val initialProgress = if (totalBytes > 0) {
                    (totalBytesRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                onProgress(initialProgress, totalBytesRead, totalBytes)

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = if (totalBytes > 0) {
                        (totalBytesRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0.5f
                    }
                    onProgress(progress, totalBytesRead, totalBytes)
                }
                output.flush()
            }
        }

        if (totalBytes > 0 && partFile.length() < totalBytes) {
            throw IllegalStateException("A letöltés megszakadt (${partFile.length()}/$totalBytes bájt)")
        }

        if (finalFile.exists()) {
            finalFile.delete()
        }
        if (!partFile.renameTo(finalFile)) {
            partFile.copyTo(finalFile, overwrite = true)
            partFile.delete()
        }

        finalFile
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float, Long, Long) -> Unit
    ): File = downloadApk(
        context = context,
        info = UpdateInfo(
            isUpdateAvailable = true,
            currentVersion = "",
            latestVersion = "",
            downloadUrl = downloadUrl,
            releaseNotes = "",
            tagName = ""
        ),
        onProgress = onProgress
    )

    fun selectBestApkAsset(
        assets: JSONArray?,
        isCurrentAppDebug: Boolean = isCurrentAppDebug()
    ): JSONObject? = Companion.selectBestApkAsset(assets, isCurrentAppDebug)

    fun clearUpdateCache(context: Context) = Companion.clearUpdateCache(context)

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

    companion object {
        fun isCurrentAppDebug(): Boolean {
            return BuildConfig.DEBUG || BuildConfig.VERSION_NAME.contains("dev", ignoreCase = true)
        }

        fun getSafeUpdateFileName(tagName: String, assetName: String, downloadUrl: String): String {
            val rawName = when {
                assetName.isNotBlank() -> "${tagName.removePrefix("v")}_$assetName"
                tagName.isNotBlank() -> "update_${tagName.removePrefix("v")}"
                else -> "update_${downloadUrl.hashCode()}"
            }
            return rawName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .removeSuffix(".apk")
        }

        fun selectBestAsset(
            assets: List<ReleaseAsset>,
            isCurrentAppDebug: Boolean = isCurrentAppDebug()
        ): ReleaseAsset? {
            val apkAssets = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
            if (apkAssets.isEmpty()) return null

            return if (isCurrentAppDebug) {
                // Priority for Debug app:
                // 1. Exact match "app-debug.apk" or contains "debug"
                // 2. Does NOT contain "release" (e.g. neptun-mobile-X-dev.apk)
                // 3. Fallback to any APK
                apkAssets.firstOrNull { it.name.contains("debug", ignoreCase = true) }
                    ?: apkAssets.firstOrNull { !it.name.contains("release", ignoreCase = true) }
                    ?: apkAssets.firstOrNull()
            } else {
                // Priority for Release/Stable app:
                // 1. Contains "release" (e.g. neptun-mobile-X-release.apk or app-release.apk)
                // 2. Does NOT contain "debug" (e.g. neptun-mobile-X.apk)
                // 3. Fallback to any APK
                apkAssets.firstOrNull { it.name.contains("release", ignoreCase = true) }
                    ?: apkAssets.firstOrNull { !it.name.contains("debug", ignoreCase = true) }
                    ?: apkAssets.firstOrNull()
            }
        }

        fun selectBestApkAsset(
            assets: JSONArray?,
            isCurrentAppDebug: Boolean = isCurrentAppDebug()
        ): JSONObject? {
            if (assets == null || assets.length() == 0) return null

            val list = mutableListOf<Pair<ReleaseAsset, JSONObject>>()
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val name = asset.optString("name", "")
                val url = asset.optString("browser_download_url", "")
                val size = asset.optLong("size", 0L)
                if (name.endsWith(".apk", ignoreCase = true)) {
                    list.add(ReleaseAsset(name, url, size) to asset)
                }
            }
            if (list.isEmpty()) return null

            val best = selectBestAsset(list.map { it.first }, isCurrentAppDebug) ?: return null
            return list.firstOrNull { it.first == best }?.second
        }

        fun clearUpdateCache(context: Context) {
            try {
                val updatesDir = File(context.cacheDir, "updates")
                if (updatesDir.exists() && updatesDir.isDirectory) {
                    updatesDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                }
            } catch (_: Exception) {
            }
        }

        fun checkAndCleanUpdateCacheOnNewVersion(context: Context) {
            try {
                val prefs = context.getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
                val lastCode = prefs.getInt("last_installed_version_code", -1)
                val currentCode = BuildConfig.VERSION_CODE

                if (lastCode != -1 && lastCode != currentCode) {
                    // Új verzió lett feltelepítve: takarítsuk a letöltött telepítőfájlokat
                    clearUpdateCache(context)
                }
                if (lastCode != currentCode) {
                    prefs.edit().putInt("last_installed_version_code", currentCode).apply()
                }
            } catch (_: Exception) {
            }
        }
    }
}
