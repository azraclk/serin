package app.azracelik.serin.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritesTest {
    @Test
    fun toggleAddsMissingId() {
        assertEquals(setOf("sleep", "focus"), setOf("sleep").toggled("focus"))
    }

    @Test
    fun toggleRemovesPresentId() {
        assertEquals(setOf("sleep"), setOf("sleep", "focus").toggled("focus"))
    }

    @Test
    fun toggleTwiceRestoresSet() {
        val start = setOf("sleep")
        assertEquals(start, start.toggled("focus").toggled("focus"))
    }
}
