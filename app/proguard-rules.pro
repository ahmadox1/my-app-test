# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.ahmadox.smartcoach.**$$serializer { *; }
-keepclassmembers class com.ahmadox.smartcoach.** {
    *** Companion;
}
-keepclasseswithmembers class com.ahmadox.smartcoach.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep accessibility service
-keep class com.ahmadox.smartcoach.service.SmartCoachAccessibilityService { *; }

# Keep data classes
-keep class com.ahmadox.smartcoach.data.model.** { *; }