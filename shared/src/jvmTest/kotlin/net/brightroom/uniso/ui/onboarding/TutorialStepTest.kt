package net.brightroom.uniso.ui.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals

class TutorialStepTest {
    @Test
    fun tutorialHasFourSteps() {
        assertEquals(4, TutorialStep.entries.size)
    }

    @Test
    fun stepsAreInCorrectOrder() {
        val steps = TutorialStep.entries
        assertEquals(TutorialStep.WELCOME, steps[0])
        assertEquals(TutorialStep.ADD_ACCOUNT, steps[1])
        assertEquals(TutorialStep.SWITCH_ACCOUNT, steps[2])
        assertEquals(TutorialStep.COMPLETE, steps[3])
    }

    @Test
    fun eachStepHasUniqueStringKeys() {
        val titleKeys = TutorialStep.entries.map { it.titleKey }.toSet()
        val descKeys = TutorialStep.entries.map { it.descriptionKey }.toSet()
        assertEquals(4, titleKeys.size)
        assertEquals(4, descKeys.size)
    }
}
