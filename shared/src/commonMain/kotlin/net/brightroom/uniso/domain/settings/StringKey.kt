package net.brightroom.uniso.domain.settings

enum class StringKey(
    val key: String,
) {
    // Sidebar
    SIDEBAR_ADD_ACCOUNT("sidebar.add_account"),
    SIDEBAR_SETTINGS("sidebar.settings"),
    SIDEBAR_TOOLTIP_ACTIVE("sidebar.tooltip.active"),

    // Dialog — Add Account
    DIALOG_ADD_ACCOUNT_TITLE("dialog.add_account.title"),
    DIALOG_ADD_ACCOUNT_SUBTITLE("dialog.add_account.subtitle"),

    // Dialog — Delete Account
    DIALOG_DELETE_TITLE("dialog.delete.title"),
    DIALOG_DELETE_CONFIRM("dialog.delete.confirm"),
    DIALOG_DELETE_WARNING("dialog.delete.warning"),

    // Dialog — Account Select
    DIALOG_ACCOUNT_SELECT_TITLE("dialog.account_select.title"),

    // Dialog — Crash Recovery
    DIALOG_CRASH_TITLE("dialog.crash.title"),
    DIALOG_CRASH_MESSAGE("dialog.crash.message"),

    // Dialog — Update
    DIALOG_UPDATE_TITLE("dialog.update.title"),

    // Errors
    ERROR_CONNECTION_FAILED("error.connection_failed"),
    ERROR_RETRY("error.retry"),
    ERROR_OPEN_IN_BROWSER("error.open_in_browser"),
    ERROR_CEF_INIT_FAILED("error.cef_init_failed"),
    ERROR_DB_ERROR("error.db_error"),

    // Settings
    SETTINGS_TITLE("settings.title"),
    SETTINGS_LANGUAGE("settings.language"),
    SETTINGS_TELEMETRY("settings.telemetry"),
    SETTINGS_TELEMETRY_DESCRIPTION("settings.telemetry.description"),
    SETTINGS_VERSION("settings.version"),
    SETTINGS_CHECK_UPDATE("settings.check_update"),

    // Tutorial
    TUTORIAL_WELCOME("tutorial.welcome"),
    TUTORIAL_STEP1("tutorial.step1"),
    TUTORIAL_STEP2("tutorial.step2"),
    TUTORIAL_SKIP("tutorial.skip"),
    TUTORIAL_NEXT("tutorial.next"),
    TUTORIAL_DONE("tutorial.done"),

    // Common buttons
    BUTTON_CANCEL("button.cancel"),
    BUTTON_CLOSE("button.close"),
    BUTTON_DELETE("button.delete"),
    BUTTON_RESTORE("button.restore"),
    BUTTON_START_NEW("button.start_new"),
    BUTTON_UPDATE_NOW("button.update_now"),
    BUTTON_LATER("button.later"),
}
