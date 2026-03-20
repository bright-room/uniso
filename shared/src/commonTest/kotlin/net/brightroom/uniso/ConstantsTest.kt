package net.brightroom.uniso

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstantsTest {
    @Test
    fun appNameIsCorrect() {
        assertEquals("Uniso", Constants.APP_NAME)
    }

    @Test
    fun appVersionIsCorrect() {
        assertTrue(
            Regex("""\d+\.\d+\.\d+""").matches(Constants.APP_VERSION),
            "APP_VERSION should follow semantic versioning (x.y.z), but was: ${Constants.APP_VERSION}"
        )
    }
}
