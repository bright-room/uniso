package net.brightroom.uniso

import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantsTest {
    @Test
    fun appNameIsCorrect() {
        assertEquals("Uniso", Constants.APP_NAME)
    }

    @Test
    fun appVersionIsCorrect() {
        assertEquals("1.0.0", Constants.APP_VERSION)
    }
}
