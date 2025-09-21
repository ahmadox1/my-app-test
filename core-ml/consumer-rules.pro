# Keep JNI bridge classes
-keep class ai.screentalk.ml.llm.** { *; }
-keepclassmembers class ai.screentalk.ml.llm.LlamaCppEngine {
    native <methods>;
}
