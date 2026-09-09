package com.example.core.notification

/**
 * Diff-alapú értesítések tárolója: a már egyszer értesített elemek azonosítóit
 * tárolja, így csak az ÚJ elemekről kap értesítést a felhasználó.
 */
interface NotifiedStore {
    fun getNotifiedIds(key: String): Set<String>
    fun setNotifiedIds(key: String, ids: Set<String>)
    fun isBaselineDone(key: String): Boolean
    fun markBaselineDone(key: String)
}

/** In-memory implementáció tesztekhez. */
class InMemoryNotifiedStore : NotifiedStore {
    private val ids = mutableMapOf<String, MutableSet<String>>()
    private val baselines = mutableSetOf<String>()

    override fun getNotifiedIds(key: String): Set<String> = ids[key] ?: emptySet()
    override fun setNotifiedIds(key: String, ids: Set<String>) {
        this.ids[key] = ids.toMutableSet()
    }
    override fun isBaselineDone(key: String): Boolean = key in baselines
    override fun markBaselineDone(key: String) {
        baselines.add(key)
    }
}

/**
 * Kiszűri azokat az elemeket, amelyekről MÉG nem értesítettük a felhasználót.
 *
 * Első futásnál (baseline) az aktuális elemeket "már látottnak" jelöli és üres
 * listát ad, így frissítés után nem kap egyszerre 50 értesítést a felhasználó.
 * A tárolt azonosítóhalmazt felülről korlátozzuk, hogy ne nőjön a végtelenségig.
 */
class NotifiedItemsTracker(
    private val store: NotifiedStore,
    private val maxTrackedIds: Int = 400
) {

    fun <T> filterNewItems(
        key: String,
        items: List<T>,
        idOf: (T) -> String
    ): List<T> = filterNewItems(key, items, idOf, null)

    fun <T> filterNewItems(
        key: String,
        items: List<T>,
        idOf: (T) -> String,
        altIdOf: ((T) -> String)?
    ): List<T> {
        if (items.isEmpty()) return emptyList()

        if (!store.isBaselineDone(key)) {
            val allKeys = items.flatMap { item ->
                val main = idOf(item)
                val alt = altIdOf?.invoke(item)
                listOfNotNull(main, alt)
            }.toSet()
            store.setNotifiedIds(key, allKeys)
            store.markBaselineDone(key)
            return emptyList()
        }

        val notified = store.getNotifiedIds(key)
        return items.filter { item ->
            val main = idOf(item)
            val alt = altIdOf?.invoke(item)
            val isNotified = (main in notified) || (alt != null && alt in notified)
            !isNotified
        }
    }

    fun markNotified(key: String, ids: List<String>) {
        if (ids.isEmpty()) return
        val notified = store.getNotifiedIds(key).toMutableSet()
        notified.addAll(ids)
        if (notified.size > maxTrackedIds) {
            // Hegyében vágjuk a halmaznak; a régi elemek ismét "újak" lehetnek,
            // de ritka eset, és jobb mint a korlátlan növekedés.
            store.setNotifiedIds(key, notified.toList().takeLast(maxTrackedIds).toSet())
        } else {
            store.setNotifiedIds(key, notified)
        }
    }
}
