pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            content {
                includeGroup("com.googlecode.tesseract.android")
            }
        }
        maven("https://s01.oss.sonatype.org/content/repositories/releases/") {
            content {
                includeGroup("com.googlecode.tesseract.android")
            }
        }
        maven("https://alphacephei.com/maven") {
            content {
                includeGroup("ai.vosk")
            }
        }
    }
}

rootProject.name = "ScreenTalk"
include(
    ":app",
    ":overlay",
    ":screen",
    ":core-ml",
    ":common"
)
