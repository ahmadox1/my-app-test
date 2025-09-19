#!/bin/bash

# Local development build script for Smart Gaming Assistant
# This script provides an easy way to build the app locally

set -e

echo "🚀 Smart Gaming Assistant - Local Build Script"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check for required tools
check_requirements() {
    print_status "Checking build requirements..."
    
    # Check if Java is available
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    print_success "Java version: $JAVA_VERSION"
    
    # Check if Android SDK is available via ANDROID_HOME or common paths
    if [ -z "$ANDROID_HOME" ]; then
        # Common Android SDK paths
        POSSIBLE_PATHS=(
            "$HOME/Android/Sdk"
            "$HOME/Library/Android/sdk"
            "/usr/local/lib/android/sdk"
            "/opt/android-sdk"
        )
        
        for path in "${POSSIBLE_PATHS[@]}"; do
            if [ -d "$path" ]; then
                export ANDROID_HOME="$path"
                print_success "Found Android SDK at: $ANDROID_HOME"
                break
            fi
        done
        
        if [ -z "$ANDROID_HOME" ]; then
            print_warning "Android SDK not found. Will try to build anyway..."
            print_warning "Please set ANDROID_HOME environment variable if build fails."
        fi
    else
        print_success "Android SDK: $ANDROID_HOME"
    fi
}

# Setup Gradle wrapper if needed
setup_gradle() {
    print_status "Setting up Gradle wrapper..."
    
    if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
        print_warning "Gradle wrapper jar not found. Attempting to create it..."
        if command -v gradle &> /dev/null; then
            gradle wrapper --gradle-version=8.4
            print_success "Gradle wrapper created"
        else
            print_error "Gradle not found. Please install Gradle or download the wrapper jar manually."
            print_status "You can download it from: https://services.gradle.org/distributions/gradle-8.4-bin.zip"
            return 1
        fi
    fi
    
    chmod +x ./gradlew
    print_success "Gradle wrapper is ready"
}

# Build function
build_apk() {
    print_status "Building APK..."
    
    # Try Gradle build first
    if ./gradlew clean assembleDebug --stacktrace; then
        print_success "Gradle build successful!"
        
        # Find the generated APK
        APK_PATH=$(find app/build/outputs/apk -name "*.apk" -type f | head -1)
        if [ -f "$APK_PATH" ]; then
            APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
            print_success "APK generated: $APK_PATH"
            print_success "APK size: $APK_SIZE"
            
            # Copy to root directory for easy access
            cp "$APK_PATH" ./app-debug.apk
            print_success "APK copied to ./app-debug.apk"
            return 0
        fi
    else
        print_warning "Gradle build failed. Trying custom build script..."
        
        if [ -f "./build_apk.sh" ]; then
            chmod +x ./build_apk.sh
            if ./build_apk.sh; then
                print_success "Custom build script successful!"
                return 0
            else
                print_error "Both Gradle and custom build failed."
                return 1
            fi
        else
            print_error "No fallback build script available."
            return 1
        fi
    fi
}

# Install function
install_apk() {
    if [ -f "./app-debug.apk" ]; then
        print_status "Installing APK to connected device..."
        
        if command -v adb &> /dev/null; then
            if adb devices | grep -q device; then
                adb install -r ./app-debug.apk
                print_success "APK installed successfully!"
            else
                print_warning "No Android device connected via ADB."
                print_status "Please connect your device and enable USB debugging."
            fi
        else
            print_warning "ADB not found. Please install Android SDK tools."
            print_status "You can manually install the APK: ./app-debug.apk"
        fi
    else
        print_error "No APK file found. Please build first."
    fi
}

# Main execution
main() {
    echo "Select an option:"
    echo "1. Build APK"
    echo "2. Build and Install APK"
    echo "3. Install existing APK"
    echo "4. Clean build"
    echo ""
    
    read -p "Enter your choice (1-4): " choice
    
    case $choice in
        1)
            check_requirements
            setup_gradle
            build_apk
            ;;
        2)
            check_requirements
            setup_gradle
            build_apk && install_apk
            ;;
        3)
            install_apk
            ;;
        4)
            print_status "Cleaning build artifacts..."
            rm -rf app/build/
            rm -f ./app-debug.apk
            if [ -f "./gradlew" ]; then
                ./gradlew clean
            fi
            print_success "Clean completed"
            ;;
        *)
            print_error "Invalid choice. Please select 1-4."
            exit 1
            ;;
    esac
}

# Show usage if --help is passed
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo "Smart Gaming Assistant - Local Build Script"
    echo ""
    echo "Usage:"
    echo "  ./local_build.sh           - Interactive mode"
    echo "  ./local_build.sh build     - Build APK only"
    echo "  ./local_build.sh install   - Install existing APK"
    echo "  ./local_build.sh clean     - Clean build artifacts"
    echo ""
    echo "Requirements:"
    echo "  - Java 17 or higher"
    echo "  - Android SDK (ANDROID_HOME environment variable)"
    echo "  - Gradle (optional, will use wrapper)"
    echo ""
    exit 0
fi

# Handle direct commands
case "$1" in
    "build")
        check_requirements
        setup_gradle
        build_apk
        ;;
    "install")
        install_apk
        ;;
    "clean")
        print_status "Cleaning build artifacts..."
        rm -rf app/build/
        rm -f ./app-debug.apk
        if [ -f "./gradlew" ]; then
            ./gradlew clean
        fi
        print_success "Clean completed"
        ;;
    *)
        main
        ;;
esac

print_success "Script completed!"