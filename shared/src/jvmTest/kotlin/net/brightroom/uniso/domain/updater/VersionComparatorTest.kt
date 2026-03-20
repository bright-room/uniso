package net.brightroom.uniso.domain.updater

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionComparatorTest {
    @Test
    fun newerMajorVersionIsDetected() {
        assertTrue(VersionComparator.isNewer("1.0.0", "2.0.0"))
    }

    @Test
    fun newerMinorVersionIsDetected() {
        assertTrue(VersionComparator.isNewer("1.0.0", "1.1.0"))
    }

    @Test
    fun newerPatchVersionIsDetected() {
        assertTrue(VersionComparator.isNewer("1.0.0", "1.0.1"))
    }

    @Test
    fun sameVersionIsNotNewer() {
        assertFalse(VersionComparator.isNewer("1.0.0", "1.0.0"))
    }

    @Test
    fun olderVersionIsNotNewer() {
        assertFalse(VersionComparator.isNewer("2.0.0", "1.0.0"))
    }

    @Test
    fun olderMinorVersionIsNotNewer() {
        assertFalse(VersionComparator.isNewer("1.2.0", "1.1.0"))
    }

    @Test
    fun differentLengthVersionsCompareCorrectly() {
        assertTrue(VersionComparator.isNewer("1.0", "1.0.1"))
        assertFalse(VersionComparator.isNewer("1.0.1", "1.0"))
    }

    @Test
    fun emptyVersionIsOlderThanAny() {
        assertTrue(VersionComparator.isNewer("", "1.0.0"))
    }
}
