#!/bin/bash

# Smart Gaming Assistant - Local Development Build Script
# This script provides multiple build methods for local development

set -e

echo "🎮 Smart Gaming Assistant - Local Build Script"
echo "=============================================="

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check build environment
check_environment() {
    print_status "Checking build environment..."
    
    # Check Java
    if command -v java >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2 | cut -d '.' -f 1)
        if [ "$JAVA_VERSION" -ge "17" ]; then
            print_success "Java $JAVA_VERSION detected"
        else
            print_error "Java 17+ required, found Java $JAVA_VERSION"
            exit 1
        fi
    else
        print_error "Java not found. Please install Java 17+"
        exit 1
    fi
    
    # Check Gradle wrapper
    if [ -f "./gradlew" ] && [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
        print_success "Gradle wrapper found"
        GRADLE_AVAILABLE=true
    else
        print_warning "Gradle wrapper not found or incomplete"
        GRADLE_AVAILABLE=false
    fi
    
    # Check Android SDK
    if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
        print_success "Android SDK found at $ANDROID_HOME"
        ANDROID_SDK_AVAILABLE=true
    elif command -v aapt2 >/dev/null 2>&1; then
        print_success "Android build tools available in PATH"
        ANDROID_SDK_AVAILABLE=true
    else
        print_warning "Android SDK not found. Custom build may not work."
        ANDROID_SDK_AVAILABLE=false
    fi
}

# Build method 1: Gradle (preferred)
build_with_gradle() {
    print_status "Building with Gradle..."
    
    if [ "$GRADLE_AVAILABLE" != "true" ]; then
        print_error "Gradle wrapper not available"
        return 1
    fi
    
    # Make sure gradlew is executable
    chmod +x ./gradlew
    
    # Try building with Gradle
    if ./gradlew assembleDebug --stacktrace; then
        print_success "Gradle build completed successfully"
        
        # Find the generated APK
        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
        if [ -f "$APK_PATH" ]; then
            print_success "APK generated at: $APK_PATH"
            APK_SIZE=$(stat -c%s "$APK_PATH")
            print_status "APK size: $APK_SIZE bytes ($(numfmt --to=iec --suffix=B $APK_SIZE))"
            
            # Copy to root for easy access
            cp "$APK_PATH" "./app-debug-gradle.apk"
            print_success "APK copied to ./app-debug-gradle.apk for easy access"
            return 0
        else
            print_error "Gradle build completed but APK not found at expected location"
            return 1
        fi
    else
        print_error "Gradle build failed"
        return 1
    fi
}

# Build method 2: Custom build script (fallback)
build_with_custom_script() {
    print_status "Building with custom script..."
    
    if [ ! -f "./build_apk.sh" ]; then
        print_error "Custom build script (build_apk.sh) not found"
        return 1
    fi
    
    if [ "$ANDROID_SDK_AVAILABLE" != "true" ]; then
        print_error "Android SDK required for custom build"
        return 1
    fi
    
    # Make script executable and run
    chmod +x ./build_apk.sh
    if ./build_apk.sh; then
        print_success "Custom build completed successfully"
        
        # Find the generated APK
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
            cp "app/build/outputs/apk/debug/app-debug.apk" "./app-debug-custom.apk"
            print_success "APK copied to ./app-debug-custom.apk"
            return 0
        else
            print_error "Custom build completed but APK not found"
            return 1
        fi
    else
        print_error "Custom build failed"
        return 1
    fi
}

# Clean build artifacts
clean_build() {
    print_status "Cleaning build artifacts..."
    
    if [ "$GRADLE_AVAILABLE" = "true" ]; then
        ./gradlew clean || print_warning "Gradle clean failed"
    fi
    
    # Remove build directories
    rm -rf app/build/
    rm -f app-debug*.apk
    rm -f build_report.txt
    
    print_success "Build artifacts cleaned"
}

# Show help
show_help() {
    cat << EOF
Smart Gaming Assistant - Local Build Script

Usage: $0 [OPTIONS]

OPTIONS:
    clean           Clean all build artifacts
    gradle          Build using Gradle only
    custom          Build using custom script only  
    auto            Auto-detect best build method (default)
    check           Check build environment only
    help            Show this help message

EXAMPLES:
    $0              # Auto build (tries Gradle first, then custom)
    $0 gradle       # Force Gradle build
    $0 custom       # Force custom build
    $0 clean        # Clean build artifacts
    $0 check        # Check environment without building

EOF
}

# Parse command line arguments
case "${1:-auto}" in
    "clean")
        check_environment
        clean_build
        ;;
    "gradle")
        check_environment
        if ! build_with_gradle; then
            print_error "Gradle build failed"
            exit 1
        fi
        ;;
    "custom")
        check_environment
        if ! build_with_custom_script; then
            print_error "Custom build failed"
            exit 1
        fi
        ;;
    "auto")
        check_environment
        print_status "Auto-detecting best build method..."
        
        BUILD_SUCCESS=false
        
        # Try Gradle first if available
        if [ "$GRADLE_AVAILABLE" = "true" ]; then
            print_status "Trying Gradle build..."
            if build_with_gradle; then
                BUILD_SUCCESS=true
                print_success "✅ Build completed successfully with Gradle"
            else
                print_warning "Gradle build failed, trying custom script..."
            fi
        fi
        
        # Try custom script as fallback
        if [ "$BUILD_SUCCESS" != "true" ] && [ "$ANDROID_SDK_AVAILABLE" = "true" ]; then
            print_status "Trying custom build script..."
            if build_with_custom_script; then
                BUILD_SUCCESS=true
                print_success "✅ Build completed successfully with custom script"
            fi
        fi
        
        if [ "$BUILD_SUCCESS" != "true" ]; then
            print_error "❌ All build methods failed"
            exit 1
        fi
        ;;
    "check")
        check_environment
        print_success "Environment check completed"
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        print_error "Unknown option: $1"
        show_help
        exit 1
        ;;
esac

# Final status
if [ "${1:-auto}" != "clean" ] && [ "${1:-auto}" != "check" ] && [ "${1:-auto}" != "help" ]; then
    echo ""
    print_success "🎉 Build process completed!"
    echo ""
    
    # Show available APKs
    print_status "Generated APK files:"
    for apk in app-debug*.apk; do
        if [ -f "$apk" ]; then
            SIZE=$(stat -c%s "$apk" 2>/dev/null || echo "unknown")
            SIZE_HUMAN=$(numfmt --to=iec --suffix=B "$SIZE" 2>/dev/null || echo "unknown")
            echo "  📦 $apk ($SIZE_HUMAN)"
        fi
    done
    
    echo ""
    print_status "📱 To install the APK:"
    echo "  1. Enable 'Unknown Sources' in Android settings"
    echo "  2. Copy APK to your device"
    echo "  3. Install: adb install app-debug*.apk"
    echo "     OR tap the APK file on your device"
    echo ""
fi

exit 0