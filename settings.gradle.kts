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

        // mavenCentral يكفي لمعظم الاعتمادات (تقدر تشيل content لو ما تحتاجه)
        mavenCentral {
            // إذا فعلاً تستخدم group هذا، احتفظ به. وإلا احذف كتلة content بالكامل.
            content {
                includeGroup("com.googlecode.tesseract.android")
            }
        }

        // أضف JitPack فقط إذا عندك dependecies من JitPack
        // احذف هذا البلوك إن ما تحتاجه
        maven("https://jitpack.io")

        // مستودع Vosk الرسمي
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
