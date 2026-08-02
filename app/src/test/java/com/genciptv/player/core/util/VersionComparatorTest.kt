package com.genciptv.player.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    // ── Cases named in the update-system spec ────────────────────────────────

    @Test
    fun `two-part versionName equals the three-part tag it shipped as`() {
        // Builds before v1.2.0 carry versionName "1.1" while the tag is v1.1.0.
        assertFalse(VersionComparator.isNewer("1.1", "v1.1.0"))
    }

    @Test
    fun `same version is not an update`() {
        assertFalse(VersionComparator.isNewer("1.2.0", "v1.2.0"))
    }

    @Test
    fun `minor version compares numerically not lexicographically`() {
        // "1.10.0" sorts before "1.2.0" as text; numerically it is newer.
        assertTrue(VersionComparator.isNewer("1.2.0", "v1.10.0"))
    }

    @Test
    fun `shorter newer tag still counts as an update`() {
        assertTrue(VersionComparator.isNewer("1.2.0", "v1.3"))
    }

    @Test
    fun `older patch is not an update`() {
        assertFalse(VersionComparator.isNewer("1.2.0", "v1.1.9"))
    }

    @Test
    fun `pre-release suffix is stripped and the number still wins`() {
        assertTrue(VersionComparator.isNewer("1.2.0", "v2.0.0-beta"))
    }

    // ── Supporting behaviour ─────────────────────────────────────────────────

    @Test
    fun `normalize strips prefix and suffixes`() {
        assertEquals("1.2.3", VersionComparator.normalize("v1.2.3"))
        assertEquals("1.2.3", VersionComparator.normalize("V1.2.3"))
        assertEquals("1.2.3", VersionComparator.normalize("1.2.3-rc.1"))
        assertEquals("1.2.3", VersionComparator.normalize("v1.2.3+build7"))
        assertEquals("1.2.3", VersionComparator.normalize("  v1.2.3  "))
    }

    @Test
    fun `major version dominates`() {
        assertTrue(VersionComparator.isNewer("1.9.9", "v2.0.0"))
        assertFalse(VersionComparator.isNewer("2.0.0", "v1.9.9"))
    }

    @Test
    fun `trailing zeros do not make a version newer`() {
        assertFalse(VersionComparator.isNewer("1.2", "v1.2.0"))
        assertFalse(VersionComparator.isNewer("1.2.0", "v1.2"))
    }

    @Test
    fun `garbage parts read as zero instead of throwing`() {
        assertFalse(VersionComparator.isNewer("1.2.0", "vabc"))
        assertTrue(VersionComparator.isNewer("0.9.0", "v1.x.0"))
    }
}
