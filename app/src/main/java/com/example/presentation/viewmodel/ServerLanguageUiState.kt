package com.example.presentation.viewmodel

import com.example.domain.model.NeptunLanguage
import com.example.domain.model.NeptunLanguages

/**
 * A Neptun szervernyelv-választó UI-állapota. Közös a bejelentkezési és a
 * beállítások képernyő között.
 */
data class ServerLanguageUiState(
    /** Az intézmény által támogatott nyelvek (szerverről vagy tartalék lista). */
    val languages: List<NeptunLanguage> = NeptunLanguages.FALLBACK_LANGUAGES,
    /** A kiválasztott nyelv LCID-je. */
    val selectedLcid: Int = NeptunLanguages.DEFAULT_LCID,
    /** Szerverről frissítés folyamatban. */
    val isLoading: Boolean = false,
    /** Igaz, ha a beépített tartalék lista látszik (offline / régi szerver). */
    val isFallback: Boolean = false
)
