package com.example.domain.repository

import com.example.domain.model.NeptunLanguage
import kotlinx.coroutines.flow.Flow

/**
 * Egy intézmény által támogatott nyelvek és a kiválasztott szervernyelv
 * (LCID) lekérdezése. A lista forrása az `EnvironmentData` végpont,
 * gyorsítótárazva; ha az nem elérhető, a beépített hu/en/de tartalék lista.
 */
data class LanguageList(
    val languages: List<NeptunLanguage>,
    /** Igaz, ha egyik forrás sem volt elérhető, és a tartalék lista látszik. */
    val isFallback: Boolean = false
)

interface LanguageRepository {
    /** A kiválasztott szervernyelv LCID-je (pl. 1038 = magyar). */
    val selectedLcidFlow: Flow<Int>

    fun getSelectedLcid(): Int

    suspend fun setSelectedLcid(lcid: Int)

    /**
     * Azonnal visszaadja az ismert listát: gyorsítótárból, vagy ha nincs,
     * a tartalék listát. Nem indít hálózati kérést.
     */
    fun getCachedLanguages(baseUrl: String): LanguageList

    /**
     * Frissíti a listát a szerverről (`EnvironmentData`). Sikeres letöltés
     * után a gyorsítótárat frissíti, és ha a kiválasztott nyelv nem szerepel
     * a szerver listájában, automatikusan átvált a szerver alapértelmezett
     * nyelvére. Hiba esetén a gyorsítótárazott, majd a tartalék lista tér vissza.
     */
    suspend fun refreshLanguages(baseUrl: String): LanguageList
}
