# Build Instructions - Smart Gaming Assistant

## Overview

This document provides comprehensive instructions for building the Smart Gaming Assistant app locally and through GitHub Actions.

## Prerequisites

### Local Development
- **Java 17 or higher** - Required for Android development
- **Android SDK** - Android API 24 (Android 7.0) to API 34 (Android 14)
- **Gradle** - Will be handled automatically by the wrapper
- **Git** - For version control

### Optional Tools
- **Android Studio** - For development and debugging
- **ADB (Android Debug Bridge)** - For device installation

## Project Structure

```
Smart Gaming Assistant/
├── .github/workflows/          # GitHub Actions workflows
│   ├── build-apk.yml          # Main build workflow
│   └── release-apk.yml        # Release workflow
├── app/                       # Android app module
│   ├── src/main/              # Main source code
│   │   ├── java/              # Java source files
│   │   ├── res/               # Android resources
│   │   └── AndroidManifest.xml
│   ├── build.gradle           # App-level build configuration
│   └── proguard-rules.pro     # ProGuard rules
├── gradle/wrapper/            # Gradle wrapper files
├── build.gradle               # Project-level build configuration
├── settings.gradle            # Gradle settings
├── gradlew                    # Gradle wrapper script (Unix)
├── gradlew.bat               # Gradle wrapper script (Windows)
├── local_build.sh            # Interactive local build script
├── build_apk.sh              # Custom APK build script
└── README.md                 # Project documentation
```

## Local Build Instructions

### Method 1: Interactive Build Script (Recommended)

```bash
# Run the interactive build script
./local_build.sh

# Or with specific commands:
./local_build.sh build     # Build APK only
./local_build.sh install   # Install existing APK
./local_build.sh clean     # Clean build artifacts
```

### Method 2: Using Gradle Directly

```bash
# Make gradlew executable
chmod +x ./gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install to connected device
./gradlew installDebug
```

### Method 3: Custom Build Script

```bash
# Use the custom build script (offline-capable)
chmod +x ./build_apk.sh
./build_apk.sh
```

## GitHub Actions Build

### Automatic Builds

The project is configured with GitHub Actions workflows:

1. **build-apk.yml** - Triggers on:
   - Push to `main` or `develop` branches
   - Pull requests to `main`
   - Manual workflow dispatch

2. **release-apk.yml** - Triggers on:
   - Git tags starting with `v*`
   - Manual workflow dispatch

### Build Process

1. **Environment Setup**
   - Java 17 (Temurin distribution)
   - Android SDK (latest)
   - Gradle caching for faster builds

2. **Build Steps**
   - Try Gradle build first
   - Fallback to custom build script if Gradle fails
   - Generate APK artifacts

3. **Artifact Upload**
   - Debug APK uploaded as build artifact
   - Release APK attached to GitHub releases

## Output Files

### Successful Build Locations

- **Gradle build**: `app/build/outputs/apk/debug/app-debug.apk`
- **Custom build**: `app/build/outputs/apk/debug/app-debug.apk`
- **Root copy**: `./app-debug.apk` (created by local script)

### APK Information

- **Package**: `com.smartassistant`
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Size**: ~36KB (highly optimized)
- **Signing**: Debug keystore (development)

## Installation

### Via ADB (Development)

```bash
# Install to connected device
adb install -r app-debug.apk

# Install with USB debugging
adb devices  # Check connected devices
adb install app-debug.apk
```

### Manual Installation

1. Transfer APK to Android device
2. Enable "Install from Unknown Sources" in Settings
3. Tap the APK file to install
4. Grant required permissions

## Troubleshooting

### Common Issues

#### Gradle Build Fails
**Solution**: Use the custom build script
```bash
./build_apk.sh
```

#### Missing Android SDK
**Solution**: Set ANDROID_HOME environment variable
```bash
export ANDROID_HOME=/path/to/android/sdk
```

#### Gradle Wrapper Missing
**Solution**: Regenerate wrapper
```bash
gradle wrapper --gradle-version=8.4
```

#### Internet Connectivity Issues
**Solution**: The custom build script works offline

#### Permission Denied
**Solution**: Make scripts executable
```bash
chmod +x ./gradlew ./local_build.sh ./build_apk.sh
```

### Build Environment Variables

```bash
# Android SDK location
export ANDROID_HOME=/path/to/android/sdk

# Java home (if needed)
export JAVA_HOME=/path/to/java17

# Add Android tools to PATH
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools
```

## Development Workflow

### Local Development

1. Clone repository
2. Run `./local_build.sh`
3. Select "Build and Install APK"
4. Test on device/emulator

### Contributing Changes

1. Make code changes
2. Test locally with `./local_build.sh`
3. Commit and push changes
4. GitHub Actions will automatically build and test
5. Create pull request

### Release Process

1. Update version in `app/build.gradle`
2. Test thoroughly
3. Create and push version tag: `git tag v1.0.0 && git push origin v1.0.0`
4. GitHub Actions will create release with APK

## Performance Optimization

### Build Speed

- **Gradle caching**: Enabled in GitHub Actions
- **Incremental builds**: Supported by Gradle
- **Parallel builds**: Enabled in gradle.properties

### APK Size

- **ProGuard**: Disabled for debugging (can be enabled)
- **Resource optimization**: Automatic
- **Unused code removal**: Via build tools

## Security Considerations

### Debug vs Release

- **Debug builds**: Use debug keystore, debugging enabled
- **Release builds**: Should use production keystore (not included)

### Keystore Management

For production releases, you'll need to:
1. Generate production keystore
2. Store keystore securely
3. Configure signing in `app/build.gradle`
4. Add keystore secrets to GitHub Actions

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/studio/build)
- [Gradle Build Tool](https://gradle.org/guides/)
- [GitHub Actions for Android](https://github.com/actions/setup-java)

## Support

For build issues:
1. Check this documentation
2. Review GitHub Actions logs
3. Run local build with verbose output: `./gradlew assembleDebug --info`
4. Open an issue with build logs