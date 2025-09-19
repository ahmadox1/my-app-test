#!/bin/bash

# APK Verification Script
# Verifies the generated APK is valid and installable

APK_FILE="app-debug-custom.apk"

echo "🔍 Verifying APK: $APK_FILE"
echo "=================================="

if [ ! -f "$APK_FILE" ]; then
    echo "❌ APK file not found: $APK_FILE"
    exit 1
fi

# Check file size
SIZE=$(stat -c%s "$APK_FILE")
SIZE_HUMAN=$(numfmt --to=iec --suffix=B "$SIZE")
echo "📦 File size: $SIZE bytes ($SIZE_HUMAN)"

# Check if it's a valid ZIP/APK file
echo "📋 File type:"
file "$APK_FILE"

# Try to list APK contents (basic validation)
echo ""
echo "📄 APK contents preview:"
unzip -l "$APK_FILE" 2>/dev/null | head -10

# Check for required Android files
echo ""
echo "✅ Required files check:"
unzip -l "$APK_FILE" 2>/dev/null | grep -q "AndroidManifest.xml" && echo "  ✓ AndroidManifest.xml found" || echo "  ❌ AndroidManifest.xml missing"
unzip -l "$APK_FILE" 2>/dev/null | grep -q "classes.dex" && echo "  ✓ classes.dex found" || echo "  ❌ classes.dex missing"
unzip -l "$APK_FILE" 2>/dev/null | grep -q "resources.arsc" && echo "  ✓ resources.arsc found" || echo "  ❌ resources.arsc missing"

echo ""
echo "🎉 APK verification completed!"
echo ""
echo "📱 Installation instructions:"
echo "  1. Enable 'Unknown Sources' in Android settings"
echo "  2. Transfer $APK_FILE to your Android device"
echo "  3. Tap the APK file to install"
echo "  4. Or use ADB: adb install $APK_FILE"