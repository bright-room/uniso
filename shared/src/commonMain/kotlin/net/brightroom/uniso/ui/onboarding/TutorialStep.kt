package net.brightroom.uniso.ui.onboarding

import net.brightroom.uniso.domain.settings.StringKey

/**
 * Defines the steps in the onboarding tutorial.
 * Each step has a title, description, and an icon hint for the UI.
 */
enum class TutorialStep(
    val titleKey: StringKey,
    val descriptionKey: StringKey,
    val icon: String,
) {
    WELCOME(
        titleKey = StringKey.TUTORIAL_WELCOME_TITLE,
        descriptionKey = StringKey.TUTORIAL_WELCOME_DESCRIPTION,
        icon = "\uD83C\uDF10", // 🌐
    ),
    ADD_ACCOUNT(
        titleKey = StringKey.TUTORIAL_ADD_ACCOUNT_TITLE,
        descriptionKey = StringKey.TUTORIAL_ADD_ACCOUNT_DESCRIPTION,
        icon = "\u2795", // ➕
    ),
    SWITCH_ACCOUNT(
        titleKey = StringKey.TUTORIAL_SWITCH_ACCOUNT_TITLE,
        descriptionKey = StringKey.TUTORIAL_SWITCH_ACCOUNT_DESCRIPTION,
        icon = "\uD83D\uDD00", // 🔀
    ),
    COMPLETE(
        titleKey = StringKey.TUTORIAL_COMPLETE_TITLE,
        descriptionKey = StringKey.TUTORIAL_COMPLETE_DESCRIPTION,
        icon = "\u2705", // ✅
    ),
}
