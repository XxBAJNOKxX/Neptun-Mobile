package com.example.core.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
        isDebugApp: Boolean = isCurrentAppDebug(),
        forceRefresh: Boolean = false
    ): UpdateInfo? = withContext(Dispatchers.IO) {
        val repo = BuildConfig.GITHUB_REPO
        try {
            val url = if (channel == UpdateChannel.STABLE) {
                "https://api.github.com/repos/$repo/releases/latest"
            } else {
                "https://api.github.com/repos/$repo/releases"
            }

            val cached = apiCache[url]
            val isCacheValid = !forceRefresh && cached != null && (System.currentTimeMillis() - cached.timestamp < CACHE_TTL_MS)

            val body: String = if (isCacheValid) {
                cached!!.body
            } else {
                val requestBuilder = Request.Builder()
                    .url(url)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "NeptunMobileApp")

                if (cached?.etag != null) {
                    requestBuilder.header("If-None-Match", cached.etag)
                }

                val response = client.newCall(requestBuilder.build()).execute()
                response.use { resp ->
                    when {
                        resp.code == 304 && cached != null -> {
                            // Tartalom nem változott, ETag egyezik; frissítjük az érvényességet
                            apiCache[url] = cached.copy(timestamp = System.currentTimeMillis())
                            cached.body
                        }
                        resp.code == 403 || resp.code == 429 -> {
                            // Rate limit túllépve
                            android.util.Log.w("AppUpdateManager", "GitHub API rate limit elérve (HTTP ${resp.code})")
                            cached?.body ?: return@withContext null
                        }
                        !resp.isSuccessful -> {
                            return@withContext null
                        }
                        else -> {
                            val freshBody = resp.body?.string() ?: return@withContext null
                            val etag = resp.header("ETag")
                            apiCache[url] = CachedApiResponse(freshBody, etag, System.currentTimeMillis())
                            freshBody
                        }
                    }
                }
            }

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
                    var hasMatchingApk = false
                    if (assets != null) {
                        for (j in 0 until assets.length()) {
                            val assetObj = assets.optJSONObject(j) ?: continue
                            val assetName = assetObj.optString("name", "")
                            if (assetName.endsWith(".apk", ignoreCase = true)) {
                                if (isDebugApp && assetName.contains("debug", ignoreCase = true)) {
                                    hasMatchingApk = true
                                    break
                                } else if (!isDebugApp && assetName.contains("release", ignoreCase = true) && !assetName.contains("debug", ignoreCase = true)) {
                                    hasMatchingApk = true
                                    break
                                }
                            }
                        }
                    }
                    if (!hasMatchingApk) continue

                    val parsed = ParsedVersion.parse(tag)
                    if (bestParsedVersion == null || parsed.isNewerThan(bestParsedVersion)) {
                        bestParsedVersion = parsed
                        bestRelease = item
                    }
                }

                bestRelease ?: return@withContext null
            } else {
                JSONObject(body)
            }

            val tagName = releaseObj.optString("tag_name", "").trim()
            val htmlUrl = releaseObj.optString("html_url", "https://github.com/$repo/releases")
            val releaseNotes = releaseObj.optString("body", "").trim()

            val assets = releaseObj.optJSONArray("assets")
            val chosenAsset = selectBestApkAsset(assets, isCurrentAppDebug = isDebugApp)
                ?: return@withContext null

            val apkDownloadUrl = chosenAsset.optString("browser_download_url")
            val assetName = chosenAsset.optString("name", "")
            val expectedSizeBytes = chosenAsset.optLong("size", 0L)

            val targetDownloadUrl = if (apkDownloadUrl.isNotBlank()) apkDownloadUrl else htmlUrl
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

        if (info.expectedSizeBytes > 0 && finalFile.length() != info.expectedSizeBytes) {
            finalFile.delete()
            throw IllegalStateException("A letöltött fájl mérete (${finalFile.length()} B) eltér a várt mérettől (${info.expectedSizeBytes} B)!")
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

    fun calculateSha256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Ellenőrzi, hogy a letöltött APK csomagneve és aláíró tanúsítványa
     * megegyezik-e a jelenleg futó alkalmazáséval.
     */
    fun verifyApkSignature(context: Context, apkFile: File): Boolean {
        return try {
            val pm = context.packageManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            val archiveInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
                ?: return false

            if (archiveInfo.packageName != context.packageName) {
                android.util.Log.e("AppUpdateManager", "APK csomagnév eltérés: ${archiveInfo.packageName} vs ${context.packageName}")
                return false
            }

            val currentInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val currentSigningInfo = currentInfo.signingInfo ?: return false
                val archiveSigningInfo = archiveInfo.signingInfo ?: return false

                val currentSignatures = if (currentSigningInfo.hasMultipleSigners()) {
                    currentSigningInfo.apkContentsSigners
                } else {
                    currentSigningInfo.signingCertificateHistory
                }

                val archiveSignatures = if (archiveSigningInfo.hasMultipleSigners()) {
                    archiveSigningInfo.apkContentsSigners
                } else {
                    archiveSigningInfo.signingCertificateHistory
                }

                if (currentSignatures.isNullOrEmpty() || archiveSignatures.isNullOrEmpty()) return false
                val currentHashes = currentSignatures.map { it.toByteArray().contentHashCode() }.toSet()
                val archiveHashes = archiveSignatures.map { it.toByteArray().contentHashCode() }.toSet()
                val matches = currentHashes.intersect(archiveHashes).isNotEmpty()
                if (!matches) {
                    android.util.Log.e("AppUpdateManager", "APK aláíró tanúsítvány nem egyezik a futó appéval!")
                }
                matches
            } else {
                @Suppress("DEPRECATION")
                val currentSignatures = currentInfo.signatures
                @Suppress("DEPRECATION")
                val archiveSignatures = archiveInfo.signatures
                if (currentSignatures.isNullOrEmpty() || archiveSignatures.isNullOrEmpty()) return false
                val currentHashes = currentSignatures.map { it.toByteArray().contentHashCode() }.toSet()
                val archiveHashes = archiveSignatures.map { it.toByteArray().contentHashCode() }.toSet()
                val matches = currentHashes.intersect(archiveHashes).isNotEmpty()
                if (!matches) {
                    android.util.Log.e("AppUpdateManager", "APK aláíró tanúsítvány nem egyezik a futó appéval!")
                }
                matches
            }
        } catch (e: Exception) {
            android.util.Log.e("AppUpdateManager", "Hiba az APK ellenőrzésekor: ${e.message}")
            false
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        if (!verifyApkSignature(context, apkFile)) {
            android.util.Log.e("AppUpdateManager", "Az APK aláírása vagy csomagneve nem egyezik a futó alkalmazással!")
            apkFile.delete()
            return false
        }
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

    data class ParsedVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val isDebugOrDev: Boolean,
        val buildNumber: Long = 0L
    ) {
        fun isNewerThan(other: ParsedVersion): Boolean {
            if (this.major != other.major) return this.major > other.major
            if (this.minor != other.minor) return this.minor > other.minor
            if (this.patch != other.patch) return this.patch > other.patch

            // Ha a major.minor.patch megegyezik:
            // 1. Ha mindkettő debug/dev, vagy eltér a build számuk:
            if (this.isDebugOrDev == other.isDebugOrDev && (this.buildNumber > 0 || other.buildNumber > 0)) {
                return this.buildNumber > other.buildNumber
            }

            // 2. Ha az egyik release (végleges), a másik debug/dev:
            // A végleges release stabilabb / újabb, mint az azonos számú debug/dev
            if (this.isDebugOrDev != other.isDebugOrDev) {
                return !this.isDebugOrDev && other.isDebugOrDev
            }

            return false
        }

        companion object {
            fun parse(raw: String): ParsedVersion {
                val clean = raw.removePrefix("v").trim()
                val isDebugOrDev = clean.contains("debug", ignoreCase = true) || clean.contains("dev", ignoreCase = true)

                // Build szám keresése: pl. +105 vagy .105 vagy -105 a debug/dev után
                val buildNumber = if (clean.contains("+")) {
                    Regex("""\+(\d+)""").find(clean)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                } else if (isDebugOrDev) {
                    Regex("""(?:debug|dev)[.-]?(\d+)""", RegexOption.IGNORE_CASE).find(clean)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                } else {
                    0L
                }

                val versionPart = clean.substringBefore("-").substringBefore("+")
                val numbers = Regex("""\d+""").findAll(versionPart).map { it.value.toInt() }.toList()
                val major = numbers.getOrElse(0) { 0 }
                val minor = numbers.getOrElse(1) { 0 }
                val patch = numbers.getOrElse(2) { 0 }

                return ParsedVersion(major, minor, patch, isDebugOrDev, buildNumber)
            }
        }
    }

    companion object {
        data class CachedApiResponse(
            val body: String,
            val etag: String?,
            val timestamp: Long
        )

        val apiCache = java.util.concurrent.ConcurrentHashMap<String, CachedApiResponse>()
        const val CACHE_TTL_MS = 30 * 60 * 1000L // 30 perc

        fun clearApiCache() {
            apiCache.clear()
        }

        fun isCurrentAppDebug(): Boolean {
            return BuildConfig.DEBUG || BuildConfig.APPLICATION_ID.endsWith(".debug")
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
                // Debug alkalmazás: KIZÁRÓLAG debug APK-t fogad el.
                // Pl. NeptunMobile-0.2.0-debug.apk vagy app-debug.apk
                apkAssets.firstOrNull { it.name.contains("debug", ignoreCase = true) }
            } else {
                // Release alkalmazás: KIZÁRÓLAG olyan release APK-t fogad el, ami NEM debug.
                // Pl. NeptunMobile-0.2.0-release.apk vagy app-release.apk
                apkAssets.firstOrNull {
                    it.name.contains("release", ignoreCase = true) && !it.name.contains("debug", ignoreCase = true)
                } ?: apkAssets.firstOrNull {
                    !it.name.contains("debug", ignoreCase = true) && !it.name.contains("dev", ignoreCase = true)
                }
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
