package com.example.data.repository

import android.util.Log
import com.example.core.locale.StringProvider
import com.example.core.security.EncryptedPreferencesManager
import com.example.data.network.NeptunApiClient
import com.example.domain.model.NeptunLanguage
import com.example.domain.model.NeptunLanguages
import com.example.domain.repository.LanguageList
import com.example.domain.repository.LanguageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LanguageRepositoryImpl(
    private val prefsManager: EncryptedPreferencesManager,
    private val strings: StringProvider,
    private val neptunApiClient: NeptunApiClient = NeptunApiClient(strings)
) : LanguageRepository {

    override val selectedLcidFlow: Flow<Int>
        get() = prefsManager.serverLanguageFlow

    override fun getSelectedLcid(): Int = prefsManager.getServerLanguageLcid()

    override suspend fun setSelectedLcid(lcid: Int) = withContext(Dispatchers.IO) {
        if (lcid > 0) {
            prefsManager.setServerLanguageLcid(lcid)
        } else {
            Log.w(TAG, "Érvénytelen LCID elutasítva: $lcid")
        }
    }

    override fun getCachedLanguages(baseUrl: String): LanguageList {
        val cached = prefsManager.getCachedSupportedLanguages(baseUrl)
        return if (!cached.isNullOrEmpty()) {
            LanguageList(languages = cached, isFallback = false)
        } else {
            LanguageList(languages = NeptunLanguages.FALLBACK_LANGUAGES, isFallback = true)
        }
    }

    override suspend fun refreshLanguages(baseUrl: String): LanguageList = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            return@withContext getCachedLanguages(baseUrl)
        }
        try {
            val remote = neptunApiClient.getSupportedLanguages(baseUrl)
            if (remote.isNotEmpty()) {
                prefsManager.cacheSupportedLanguages(baseUrl, remote)
                ensureValidSelection(remote)
                return@withContext LanguageList(languages = remote, isFallback = false)
            } else {
                Log.d(TAG, "EnvironmentData nem adott nyelvi listát ($baseUrl), gyorsítótár/tartalék használata")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Nyelvi lista frissítése sikertelen ($baseUrl): ${e.message}")
        }
        getCachedLanguages(baseUrl)
    }

    /**
     * Ha a kiválasztott LCID nem szerepel a szerver (mérvadó) listájában –
     * pl. az intézmény nem támogatja a magyart –, automatikusan átváltunk a
     * szerver alapértelmezett nyelvére.
     */
    private fun ensureValidSelection(remote: List<NeptunLanguage>) {
        val current = prefsManager.getServerLanguageLcid()
        if (remote.none { it.lcid == current }) {
            val resolved = NeptunLanguages.resolveLcid(remote, current)
            prefsManager.setServerLanguageLcid(resolved)
            Log.i(TAG, "Szervernyelv automatikus korrekciója: $current → $resolved")
        }
    }

    companion object {
        private const val TAG = "LanguageRepo"
    }
}
