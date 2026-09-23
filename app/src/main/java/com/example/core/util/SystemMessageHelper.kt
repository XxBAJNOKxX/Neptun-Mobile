package com.example.core.util

object SystemMessageHelper {

    /**
     * Normalizálja a feladó nevét.
     * Ha a Neptun szerverről rendszerüzenetként érkezett a bejegyzés
     * (pl. "SYSTEM USER", "SYSTEM", "SYSTEM_USER", "Rendszerüzenet", üres feladó stb.),
     * akkor a felhasználóbarát "Rendszerüzenet" nevet adja vissza.
     */
    fun normalizeSenderName(sender: String?, fallback: String = "Rendszerüzenet"): String {
        val trimmed = sender?.trim() ?: ""
        if (isSystemSender(trimmed)) {
            return fallback
        }
        return trimmed
    }

    /**
     * Eldönti, hogy egy feladó rendszer-szintű üzenetküldő-e.
     */
    fun isSystemSender(sender: String?): Boolean {
        val trimmed = sender?.trim() ?: ""
        return trimmed.isEmpty() ||
                trimmed.equals("SYSTEM USER", ignoreCase = true) ||
                trimmed.equals("SYSTEM", ignoreCase = true) ||
                trimmed.equals("SYSTEM_USER", ignoreCase = true) ||
                trimmed.equals("Rendszerüzenet", ignoreCase = true) ||
                trimmed.equals("System message", ignoreCase = true) ||
                trimmed.equals("Systemnachricht", ignoreCase = true) ||
                trimmed.equals("Neptun", ignoreCase = true) ||
                trimmed.equals("Neptun Rendszer", ignoreCase = true)
    }
}
