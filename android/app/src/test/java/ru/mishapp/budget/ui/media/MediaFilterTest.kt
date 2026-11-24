package ru.mishapp.budget.ui.media

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.mishapp.budget.domain.MediaItem

class MediaFilterTest {

    private val movie = MediaItem(1, "Фильм", "MOVIE", "PLAN_TO_WATCH", null, null)
    private val series = MediaItem(2, "Сериал", "SERIES", "WATCHED", 9, null)

    private fun state(
        typeFilter: String? = null,
        statusFilter: String? = null,
    ) = MediaUiState(
        loading = false,
        items = listOf(movie, series),
        typeFilter = typeFilter,
        statusFilter = statusFilter,
    )

    @Test
    fun `no filter returns all items`() {
        assertEquals(2, state().visibleItems.size)
    }

    @Test
    fun `type filter shows only matching`() {
        val visible = state(typeFilter = "SERIES").visibleItems
        assertEquals(listOf(series), visible)
    }

    @Test
    fun `status filter shows only matching`() {
        val visible = state(statusFilter = "PLAN_TO_WATCH").visibleItems
        assertEquals(listOf(movie), visible)
    }

    @Test
    fun `combined filters intersect`() {
        val visible = state(typeFilter = "MOVIE", statusFilter = "WATCHED").visibleItems
        assertEquals(emptyList<MediaItem>(), visible)
    }

    @Test
    fun `fallback labels used when reference is empty`() {
        assertEquals("Фильм", state().typeLabel("MOVIE"))
        assertEquals("Сериал", state().typeLabel("SERIES"))
        assertEquals("Просмотрено", state().statusLabel("WATCHED"))
    }

    @Test
    fun `transitions exclude current status`() {
        val transitions = state().transitions("WATCHED")
        assertEquals(true, transitions.isNotEmpty())
        assertEquals(false, transitions.contains("WATCHED"))
    }
}
