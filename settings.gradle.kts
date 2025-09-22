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
