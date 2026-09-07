package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.update.AppUpdateManager
import com.example.core.update.InAppUpdateState
import com.example.core.update.UpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AppUpdateViewModel(
    private val updateManager: AppUpdateManager = AppUpdateManager()
) : ViewModel() {

    private val _updateState = MutableStateFlow<InAppUpdateState>(InAppUpdateState.Idle)
    val updateState: StateFlow<InAppUpdateState> = _updateState.asStateFlow()

    private var downloadedApkFile: File? = null

    fun checkForUpdatesOnLaunch() {
        viewModelScope.launch {
            _updateState.value = InAppUpdateState.Checking
            val info = updateManager.checkForUpdates()
            if (info != null && info.isUpdateAvailable) {
                _updateState.value = InAppUpdateState.UpdateAvailable(info)
            } else {
                _updateState.value = InAppUpdateState.Idle
            }
        }
    }

    fun startInAppUpdate(context: Context, info: UpdateInfo) {
        viewModelScope.launch {
            _updateState.value = InAppUpdateState.Downloading(
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = 0L,
                info = info
            )

            try {
                val apkFile = updateManager.downloadApk(
                    context = context,
                    downloadUrl = info.downloadUrl,
                    onProgress = { progress, downloaded, total ->
                        _updateState.value = InAppUpdateState.Downloading(
                            progress = progress,
                            downloadedBytes = downloaded,
                            totalBytes = total,
                            info = info
                        )
                    }
                )
                downloadedApkFile = apkFile
                _updateState.value = InAppUpdateState.ReadyToInstall(apkFile, info)

                // Immediately attempt to install
                installDownloadedApk(context, apkFile)
            } catch (e: Exception) {
                _updateState.value = InAppUpdateState.Error(
                    e.localizedMessage ?: "Hiba történt a letöltés során."
                )
            }
        }
    }

    fun installDownloadedApk(context: Context, file: File? = downloadedApkFile) {
        val targetFile = file ?: downloadedApkFile ?: return
        if (!targetFile.exists()) return

        if (!updateManager.canRequestPackageInstalls(context)) {
            updateManager.openInstallPermissionSettings(context)
            return
        }

        updateManager.installApk(context, targetFile)
    }

    fun dismissUpdate() {
        _updateState.value = InAppUpdateState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppUpdateViewModel() as T
            }
        }
    }
}
