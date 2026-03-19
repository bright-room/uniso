package net.brightroom.uniso.platform

interface KeychainAccessor {
    fun store(
        key: String,
        value: String,
    )

    fun retrieve(key: String): String?

    fun delete(key: String)
}

class InMemoryKeychainAccessor : KeychainAccessor {
    private val store = mutableMapOf<String, String>()

    override fun store(
        key: String,
        value: String,
    ) {
        store[key] = value
    }

    override fun retrieve(key: String): String? = store[key]

    override fun delete(key: String) {
        store.remove(key)
    }
}
