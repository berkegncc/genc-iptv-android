package com.genciptv.player.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeLabelTest {

    @Test
    fun `strips series name and season episode code`() {
        assertEquals("1. Olivia", episodeDisplayTitle(1, "Sugar - S01E01 - Olivia"))
    }

    @Test
    fun `keeps a title that has no boilerplate`() {
        assertEquals("7. Kırmızı Oda", episodeDisplayTitle(7, "Kırmızı Oda"))
    }

    @Test
    fun `falls back when the title is only the episode code`() {
        assertEquals("3. Bölüm", episodeDisplayTitle(3, "Dizi - S01E03"))
    }

    // ── The number is not repeated ────────────────────────────────────────────

    @Test
    fun `does not repeat a number the provider already wrote`() {
        // Was rendering "4. 4. Bölüm": the code strips up to S01E04, which
        // leaves "4. Bölüm", and the number was then prefixed again.
        assertEquals("4. Bölüm", episodeDisplayTitle(4, "Kasaba - S01E04 - 4. Bölüm"))
    }

    @Test
    fun `does not repeat a number in front of a real name`() {
        assertEquals("4. Olivia", episodeDisplayTitle(4, "Sugar - S01E04 - 4. Olivia"))
    }

    @Test
    fun `handles a zero padded number`() {
        assertEquals("4. Bölüm", episodeDisplayTitle(4, "Kasaba - S01E04 - 04 - Bölüm"))
    }

    @Test
    fun `two digit episodes are not repeated either`() {
        assertEquals("12. Bölüm", episodeDisplayTitle(12, "Kasaba - S01E12 - 12. Bölüm"))
    }

    @Test
    fun `keeps a leading number that is part of the title`() {
        // Episode 3 really is called "12 Angry Men" — the 12 is the title, not a
        // repeat of the episode number, so it has to survive.
        assertEquals("3. 12 Angry Men", episodeDisplayTitle(3, "Dizi - S01E03 - 12 Angry Men"))
    }

    @Test
    fun `keeps a mismatched leading number`() {
        assertEquals("5. 4. Sezonun Sonu", episodeDisplayTitle(5, "Dizi - S01E05 - 4. Sezonun Sonu"))
    }
}
