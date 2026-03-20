package net.brightroom.uniso.domain.updater

import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parses Sparkle-compatible Appcast XML to extract the latest version info.
 *
 * Expected format:
 * ```xml
 * <rss xmlns:sparkle="http://www.andymatuschak.org/xml-namespaces/sparkle">
 *   <channel>
 *     <item>
 *       <title>Version 1.1.0</title>
 *       <sparkle:version>1.1.0</sparkle:version>
 *       <sparkle:releaseNotesLink>https://...</sparkle:releaseNotesLink>
 *       <description>Release notes here</description>
 *       <enclosure url="https://download-url" />
 *     </item>
 *   </channel>
 * </rss>
 * ```
 */
object AppcastParser {
    private const val SPARKLE_NS = "http://www.andymatuschak.org/xml-namespaces/sparkle"

    fun parse(xml: String): UpdateInfo? {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(xml.byteInputStream())

        val items = document.getElementsByTagName("item")
        if (items.length == 0) return null

        val item = items.item(0)
        var version = ""
        var description = ""
        var downloadUrl = ""

        val children = item.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i) ?: continue
            when {
                node.namespaceURI == SPARKLE_NS && node.localName == "version" -> {
                    version = node.textContent.trim()
                }

                node.nodeName == "description" -> {
                    description = node.textContent.trim()
                }

                node.nodeName == "enclosure" -> {
                    downloadUrl =
                        node.attributes
                            ?.getNamedItem("url")
                            ?.nodeValue
                            .orEmpty()
                }
            }
        }

        if (version.isBlank()) return null

        return UpdateInfo(
            version = version,
            releaseNotes = description,
            downloadUrl = downloadUrl,
        )
    }
}
