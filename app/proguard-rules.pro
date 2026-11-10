# ---- MediaPipe / task graph ----
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# MediaPipe loads its native bridge reflectively.
-keepclasseswithmembernames class * {
    native <methods>;
}

# ---- Kotlin metadata used by reflection ----
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontwarn org.jetbrains.annotations.**

# ---- Coroutines ----
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**
