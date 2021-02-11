-optimizationpasses 3
-dontusemixedcaseclassnames
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-repackageclasses ''
-allowaccessmodification
-dontnote

-keep class com.kodabots.sdk.core.* { *; }
-keep class com.kodabots.sdk.core.** { *; }
-keep class com.kodabots.sdk.core.*$* { *; }
-keep class kotlin.reflect.** { *; }
-keep class org.jetbrains.** { *; }

-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

-dontnote kotlinx.serialization.AnnotationsKt
-dontwarn kotlin.reflect.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.kodabots.sdk.core.**$$serializer { *; }
-keepclassmembers class com.kodabots.sdk.core.** {
    *** Companion;
    *** Default;
}
-keepclasseswithmembers class com.kodabots.sdk.core.** {
    kotlinx.serialization.KSerializer serializer(...);
}
