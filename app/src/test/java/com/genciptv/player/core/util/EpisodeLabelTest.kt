package com.genciptv.player.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * [episodeName] returns the name only — the number is put in front by the
 * screen, through a string resource, because Turkish and English disagree about
 * where it goes. So these assert the extracted name, and null where the raw
 * title held nothing but its own number.
 */
class EpisodeLabelTest {

    @Test
    fun `strips series name and season episode code`() {
        assertEquals("Olivia", episodeName(1, "Sugar - S01E01 - Olivia"))
    }

    @Test
    fun `keeps a title that has no boilerplate`() {
        assertEquals("Kırmızı Oda", episodeName(7, "Kırmızı Oda"))
    }

    @Test
    fun `returns null when the title is only the episode code`() {
        assertNull(episodeName(3, "Dizi - S01E03"))
    }

    // ── The number is not repeated ────────────────────────────────────────────

    @Test
    fun `returns null when the provider only restated the number`() {
        // Rendered "4. 4. Bölüm" before: stripping up to S01E04 leaves
        // "4. Bölüm", and the number was then prefixed again.
        assertNull(episodeName(4, "Kasaba - S01E04 - 4. Bölüm"))
    }

    @Test
    fun `drops a repeated number in front of a real name`() {
        assertEquals("Olivia", episodeName(4, "Sugar - S01E04 - 4. Olivia"))
    }

    @Test
    fun `handles a zero padded number`() {
        assertNull(episodeName(4, "Kasaba - S01E04 - 04 - Bölüm"))
    }

    @Test
    fun `two digit episodes are not repeated either`() {
        assertNull(episodeName(12, "Kasaba - S01E12 - 12. Bölüm"))
    }

    @Test
    fun `keeps a leading number that is part of the title`() {
        // Episode 3 really is called "12 Angry Men" — the 12 is the title, not
        // a repeat of the episode number, so it has to survive.
        assertEquals("12 Angry Men", episodeName(3, "Dizi - S01E03 - 12 Angry Men"))
    }

    @Test
    fun `keeps a mismatched leading number`() {
        assertEquals("4. Sezonun Sonu", episodeName(5, "Dizi - S01E05 - 4. Sezonun Sonu"))
    }

    @Test
    fun `drops the bare word episode in either language`() {
        assertNull(episodeName(2, "Dizi - S01E02 - Bölüm"))
        assertNull(episodeName(2, "Show - S01E02 - Episode"))
    }
}
