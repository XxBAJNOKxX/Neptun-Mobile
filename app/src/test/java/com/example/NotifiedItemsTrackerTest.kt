package com.example

import com.example.core.notification.InMemoryNotifiedStore
import com.example.core.notification.NotifiedItemsTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotifiedItemsTrackerTest {

    data class Item(val id: String)

    private val store = InMemoryNotifiedStore()
    private val tracker = NotifiedItemsTracker(store)

    @Test
    fun `first run marks baseline and returns empty`() {
        val items = listOf(Item("a"), Item("b"), Item("c"))
        val result = tracker.filterNewItems("grades", items) { it.id }

        assertTrue(result.isEmpty())
        assertEquals(setOf("a", "b", "c"), store.getNotifiedIds("grades"))
        assertTrue(store.isBaselineDone("grades"))
    }

    @Test
    fun `returns only unseen items after baseline`() {
        val items = listOf(Item("a"), Item("b"))
        tracker.filterNewItems("grades", items) { it.id }

        val updated = listOf(Item("a"), Item("b"), Item("c"), Item("d"))
        val result = tracker.filterNewItems("grades", updated) { it.id }

        assertEquals(listOf(Item("c"), Item("d")), result)
    }

    @Test
    fun `markNotified prevents repeated notifications`() {
        val items = listOf(Item("a"))
        tracker.filterNewItems("messages", items) { it.id }

        val fresh = listOf(Item("a"), Item("b"))
        val first = tracker.filterNewItems("messages", fresh) { it.id }
        assertEquals(listOf(Item("b")), first)

        tracker.markNotified("messages", first.map { it.id })
        val second = tracker.filterNewItems("messages", fresh) { it.id }
        assertTrue(second.isEmpty())
    }

    @Test
    fun `separate keys are independent`() {
        val grades = listOf(Item("g1"))
        val messages = listOf(Item("m1"))

        tracker.filterNewItems("grades", grades) { it.id }
        tracker.filterNewItems("messages", messages) { it.id }

        val newGrade = tracker.filterNewItems("grades", listOf(Item("g1"), Item("g2"))) { it.id }
        val newMessage = tracker.filterNewItems("messages", listOf(Item("m1"), Item("m2"))) { it.id }

        assertEquals(listOf(Item("g2")), newGrade)
        assertEquals(listOf(Item("m2")), newMessage)
    }

    @Test
    fun `empty input returns empty`() {
        val result = tracker.filterNewItems("whatever", emptyList<Item>()) { it.id }
        assertTrue(result.isEmpty())
    }
}
