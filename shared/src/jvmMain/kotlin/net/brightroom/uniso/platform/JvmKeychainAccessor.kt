package net.brightroom.uniso.platform

import java.util.prefs.Preferences

class JvmKeychainAccessor : KeychainAccessor {
    private val prefs = Preferences.userRoot().node(PREFS_NODE)

    override fun store(
        key: String,
        value: String,
    ) {
        prefs.put(key, value)
        prefs.flush()
    }

    override fun retrieve(key: String): String? = prefs.get(key, null)

    override fun delete(key: String) {
        prefs.remove(key)
        prefs.flush()
    }

    companion object {
        private const val PREFS_NODE = "net/brightroom/uniso/keychain"
    }
}
