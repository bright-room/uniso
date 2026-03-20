#!/usr/bin/env bash
set -euo pipefail

# Generate Sparkle-compatible Appcast XML for auto-update distribution.
# Usage: ./scripts/generate-appcast.sh <version-tag>
# Example: ./scripts/generate-appcast.sh v1.0.0

VERSION_TAG="${1:?Usage: generate-appcast.sh <version-tag>}"
VERSION_NAME="${VERSION_TAG#v}"
RELEASE_DATE=$(date -u +"%a, %d %b %Y %H:%M:%S %z")
REPO_URL="https://github.com/bright-room/uniso"

DMG_FILE=$(find artifacts/dmg-artifact -name "*.dmg" 2>/dev/null | head -1)
MSI_FILE=$(find artifacts/msi-artifact -name "*.msi" 2>/dev/null | head -1)

DMG_SIZE=0
MSI_SIZE=0

if [ -n "$DMG_FILE" ]; then
  DMG_SIZE=$(stat -c%s "$DMG_FILE" 2>/dev/null || stat -f%z "$DMG_FILE" 2>/dev/null || echo 0)
fi

if [ -n "$MSI_FILE" ]; then
  MSI_SIZE=$(stat -c%s "$MSI_FILE" 2>/dev/null || stat -f%z "$MSI_FILE" 2>/dev/null || echo 0)
fi

DMG_FILENAME=$(basename "${DMG_FILE:-Uniso-${VERSION_NAME}.dmg}")
MSI_FILENAME=$(basename "${MSI_FILE:-Uniso-${VERSION_NAME}.msi}")

cat > appcast.xml << EOF
<?xml version="1.0" encoding="utf-8"?>
<rss version="2.0" xmlns:sparkle="http://www.andymatuschak.org/xml-namespaces/sparkle">
  <channel>
    <title>Uniso Updates</title>
    <link>${REPO_URL}/releases</link>
    <description>Appcast feed for Uniso application updates.</description>
    <language>en</language>
    <item>
      <title>Uniso ${VERSION_NAME}</title>
      <pubDate>${RELEASE_DATE}</pubDate>
      <sparkle:version>${VERSION_NAME}</sparkle:version>
      <sparkle:releaseNotesLink>${REPO_URL}/releases/tag/${VERSION_TAG}</sparkle:releaseNotesLink>
      <enclosure
        url="${REPO_URL}/releases/download/${VERSION_TAG}/${DMG_FILENAME}"
        length="${DMG_SIZE}"
        type="application/octet-stream"
        sparkle:os="macos"
      />
      <enclosure
        url="${REPO_URL}/releases/download/${VERSION_TAG}/${MSI_FILENAME}"
        length="${MSI_SIZE}"
        type="application/octet-stream"
        sparkle:os="windows"
      />
    </item>
  </channel>
</rss>
EOF

echo "Generated appcast.xml for ${VERSION_NAME}"
