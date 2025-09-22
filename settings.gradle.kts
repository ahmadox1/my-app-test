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

        // مستودع Vosk الرسمي فقط عند الحاجة له
        maven("https://alphacephei.com/maven") {
            content { includeGroup("ai.vosk") }
        }
        // إذا كنت تستخدم tess-two من com.rmtheis وتبي تقييد المحتوى:
        // mavenCentral { content { includeGroup("com.rmtheis") } }
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
