#!/bin/bash

# Simple validation test for the Smart Gaming Assistant APK
# This script tests basic APK structure and Android manifest

set -e

echo "🧪 APK Validation Test"
echo "===================="

APK_FILE="./app-debug.apk"

if [ ! -f "$APK_FILE" ]; then
    echo "❌ APK file not found: $APK_FILE"
    echo "Please build the APK first using: ./local_build.sh build"
    exit 1
fi

echo "✅ APK file exists: $APK_FILE"

# Check APK size
APK_SIZE=$(du -h "$APK_FILE" | cut -f1)
echo "📊 APK size: $APK_SIZE"

# Extract APK info if aapt is available
if command -v aapt &> /dev/null; then
    echo ""
    echo "📋 APK Information:"
    echo "=================="
    
    # Get package info
    PACKAGE_INFO=$(aapt dump badging "$APK_FILE" 2>/dev/null | grep "package:" || echo "Could not extract package info")
    echo "$PACKAGE_INFO"
    
    # Get permissions
    echo ""
    echo "🔐 Permissions:"
    aapt dump permissions "$APK_FILE" 2>/dev/null | head -10 || echo "Could not extract permissions"
    
    # Get activities
    echo ""
    echo "🎯 Main Activity:"
    aapt dump badging "$APK_FILE" 2>/dev/null | grep "launchable-activity" || echo "Could not extract activity info"
else
    echo "⚠️  aapt not found - skipping detailed APK analysis"
fi

# Try to extract manifest if unzip is available
if command -v unzip &> /dev/null && command -v xmllint &> /dev/null; then
    echo ""
    echo "📄 Extracting AndroidManifest.xml..."
    TEMP_DIR=$(mktemp -d)
    if unzip -q "$APK_FILE" AndroidManifest.xml -d "$TEMP_DIR" 2>/dev/null; then
        echo "✅ AndroidManifest.xml extracted successfully"
        # Note: Android binary XML format requires special tools to read
        echo "ℹ️  Manifest is in binary XML format (normal for APK)"
    else
        echo "⚠️  Could not extract AndroidManifest.xml"
    fi
    rm -rf "$TEMP_DIR"
fi

# Test if APK is properly signed
if command -v jarsigner &> /dev/null; then
    echo ""
    echo "🔏 Signature Verification:"
    if jarsigner -verify "$APK_FILE" &>/dev/null; then
        echo "✅ APK is properly signed"
    else
        echo "⚠️  APK signature verification failed (may be unsigned or debug-signed)"
    fi
else
    echo "⚠️  jarsigner not found - skipping signature verification"
fi

echo ""
echo "🎉 APK validation completed!"
echo ""
echo "Installation instructions:"
echo "1. Enable 'Install from Unknown Sources' in Android settings"
echo "2. Transfer APK to Android device"
echo "3. Tap APK file to install"
echo "4. Or use: adb install $APK_FILE"

exit 0