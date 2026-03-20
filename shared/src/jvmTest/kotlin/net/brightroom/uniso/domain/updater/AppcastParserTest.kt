package net.brightroom.uniso.domain.updater

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AppcastParserTest {
    @Test
    fun parsesValidAppcastXml() {
        val xml =
            """
            <rss xmlns:sparkle="http://www.andymatuschak.org/xml-namespaces/sparkle">
              <channel>
                <item>
                  <title>Version 1.1.0</title>
                  <sparkle:version>1.1.0</sparkle:version>
                  <description>Bug fixes and improvements</description>
                  <enclosure url="https://example.com/download/v1.1.0" />
                </item>
              </channel>
            </rss>
            """.trimIndent()

        val result = AppcastParser.parse(xml)

        assertNotNull(result)
        assertEquals("1.1.0", result.version)
        assertEquals("Bug fixes and improvements", result.releaseNotes)
        assertEquals("https://example.com/download/v1.1.0", result.downloadUrl)
    }

    @Test
    fun returnsNullForEmptyChannel() {
        val xml =
            """
            <rss xmlns:sparkle="http://www.andymatuschak.org/xml-namespaces/sparkle">
              <channel>
              </channel>
            </rss>
            """.trimIndent()

        assertNull(AppcastParser.parse(xml))
    }

    @Test
    fun returnsNullWhenVersionMissing() {
        val xml =
            """
            <rss xmlns:sparkle="http://www.andymatuschak.org/xml-namespaces/sparkle">
              <channel>
                <item>
                  <title>Some release</title>
                  <description>Notes</description>
                </item>
              </channel>
            </rss>
            """.trimIndent()

        assertNull(AppcastParser.parse(xml))
    }

    @Test
    fun handlesEmptyDescription() {
        val xml =
            """
            <rss xmlns:sparkle="http://www.andymatuschak.org/xml-namespaces/sparkle">
              <channel>
                <item>
                  <sparkle:version>2.0.0</sparkle:version>
                  <enclosure url="https://example.com/v2" />
                </item>
              </channel>
            </rss>
            """.trimIndent()

        val result = AppcastParser.parse(xml)

        assertNotNull(result)
        assertEquals("2.0.0", result.version)
        assertEquals("", result.releaseNotes)
        assertEquals("https://example.com/v2", result.downloadUrl)
    }

    @Test
    fun parsesFirstItemOnly() {
        val xml =
            """
            <rss xmlns:sparkle="http://www.andymatuschak.org/xml-namespaces/sparkle">
              <channel>
                <item>
                  <sparkle:version>2.0.0</sparkle:version>
                  <description>Latest</description>
                  <enclosure url="https://example.com/v2" />
                </item>
                <item>
                  <sparkle:version>1.0.0</sparkle:version>
                  <description>Old</description>
                  <enclosure url="https://example.com/v1" />
                </item>
              </channel>
            </rss>
            """.trimIndent()

        val result = AppcastParser.parse(xml)

        assertNotNull(result)
        assertEquals("2.0.0", result.version)
        assertEquals("Latest", result.releaseNotes)
    }
}
