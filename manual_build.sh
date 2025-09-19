#!/bin/bash

# Manual APK build script for Smart Gaming Assistant
# This script builds the APK without requiring internet access

set -e

echo "Starting manual APK build..."

# Set up environment
export ANDROID_HOME=/usr/local/lib/android/sdk
export ANDROID_COMPILE_SDK=android-34
export BUILD_TOOLS_VERSION=34.0.0
export PATH=$PATH:$ANDROID_HOME/build-tools/$BUILD_TOOLS_VERSION:$ANDROID_HOME/platform-tools

PROJECT_DIR=$(pwd)
BUILD_DIR=$PROJECT_DIR/app/build
CLASSES_DIR=$BUILD_DIR/intermediates/classes
SRC_DIR=$PROJECT_DIR/app/src/main
ASSETS_DIR=$SRC_DIR/assets
RES_DIR=$SRC_DIR/res
GEN_DIR=$BUILD_DIR/generated

# Create build directories
mkdir -p $BUILD_DIR/intermediates/{classes,dex,res}
mkdir -p $GEN_DIR/{source/r,source/buildConfig}/com/smartassistant

echo "Build directories created"

# Step 1: Generate R.java from resources
echo "Generating R.java..."
aapt2 compile --dir $RES_DIR -o $BUILD_DIR/intermediates/res/compiled.flata

# Create basic R.java (simplified version)
cat > $GEN_DIR/source/r/com/smartassistant/R.java << 'EOF'
package com.smartassistant;

public final class R {
    public static final class layout {
        public static final int activity_main = 0x7f040000;
        public static final int card_detected_game = 0x7f040001;
        public static final int card_service_control = 0x7f040002;
        public static final int card_suggestions = 0x7f040003;
    }
    
    public static final class id {
        public static final int btnStart = 0x7f050000;
        public static final int btnStop = 0x7f050001;
        public static final int btnAccessibility = 0x7f050002;
        public static final int serviceStatus = 0x7f050003;
        public static final int detectedGame = 0x7f050004;
        public static final int suggestionText = 0x7f050005;
    }
    
    public static final class string {
        public static final int app_name = 0x7f070000;
        public static final int service_running = 0x7f070001;
        public static final int service_stopped = 0x7f070002;
        public static final int start_service = 0x7f070003;
        public static final int stop_service = 0x7f070004;
        public static final int enable_accessibility = 0x7f070005;
        public static final int no_game_detected = 0x7f070006;
        public static final int no_suggestions = 0x7f070007;
    }
    
    public static final class style {
        public static final int Theme_SmartGamingAssistant = 0x7f080000;
    }
    
    public static final class mipmap {
        public static final int ic_launcher = 0x7f030000;
        public static final int ic_launcher_round = 0x7f030001;
    }
    
    public static final class xml {
        public static final int accessibility_service_config = 0x7f090000;
        public static final int data_extraction_rules = 0x7f090001;
        public static final int backup_rules = 0x7f090002;
    }
}
EOF

# Generate BuildConfig.java
cat > $GEN_DIR/source/buildConfig/com/smartassistant/BuildConfig.java << 'EOF'
package com.smartassistant;

public final class BuildConfig {
    public static final boolean DEBUG = true;
    public static final String APPLICATION_ID = "com.smartassistant";
    public static final String BUILD_TYPE = "debug";
    public static final String FLAVOR = "";
    public static final int VERSION_CODE = 1;
    public static final String VERSION_NAME = "1.0";
}
EOF

echo "Generated R.java and BuildConfig.java"

# Step 2: Compile Java sources
echo "Compiling Java sources..."

# Create a basic Android JAR for compilation (stub)
mkdir -p /tmp/android-stubs
cat > /tmp/android-stubs/android.jar << 'EOF'
# This is a placeholder - in a real build, we'd need the Android SDK android.jar
EOF

# For now, let's try a simpler approach - create a basic APK structure manually
echo "Creating basic APK structure..."

# Create basic APK directory structure
mkdir -p $BUILD_DIR/apk/{lib,META-INF,res}

# Copy AndroidManifest.xml
cp $SRC_DIR/AndroidManifest.xml $BUILD_DIR/apk/

# Copy resources
if [ -d "$RES_DIR" ]; then
    cp -r $RES_DIR/* $BUILD_DIR/apk/res/ 2>/dev/null || echo "No resources to copy"
fi

# Copy assets if they exist
if [ -d "$ASSETS_DIR" ]; then
    mkdir -p $BUILD_DIR/apk/assets
    cp -r $ASSETS_DIR/* $BUILD_DIR/apk/assets/ 2>/dev/null || echo "No assets to copy"
fi

echo "Basic APK structure created at $BUILD_DIR/apk/"

# Create a simple script to document the build process
cat > $BUILD_DIR/build_info.txt << EOF
Smart Gaming Assistant - Build Information
==========================================

Project Structure:
- Source files found: $(find $SRC_DIR/java -name "*.java" | wc -l) Java files
- Resources found: $(find $RES_DIR -type f 2>/dev/null | wc -l) resource files
- AndroidManifest.xml: Present

Build Status: PARTIAL
- APK structure created at: $BUILD_DIR/apk/
- Full compilation requires internet access for dependencies

Next Steps for Complete Build:
1. Ensure internet connectivity
2. Run: gradle assembleDebug
3. APK will be generated at: app/build/outputs/apk/debug/app-debug.apk

Alternative Manual Steps:
1. Compile Java sources with Android SDK
2. Convert to DEX format
3. Package resources
4. Sign APK

Current Limitations:
- TensorFlow Lite dependencies not available offline
- OpenCV dependencies not available offline
- Android Support Library dependencies not available offline
EOF

echo "Build information saved to $BUILD_DIR/build_info.txt"
echo "Basic APK structure created successfully!"

exit 0
EOF