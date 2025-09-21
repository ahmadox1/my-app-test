# Preserve service entry points
-keep class ai.screentalk.overlay.ChatHeadService
-keep class ai.screentalk.screen.ScreenCaptureService
-keep class ai.screentalk.screen.accessibility.ScreenReaderService

# Preserve JNI bridges
-keep class ai.screentalk.ml.llm.** { *; }

# Keep ML Kit and tess-two classes
-keep class com.google.mlkit.** { *; }
-keep class com.googlecode.tesseract.android.** { *; }
