-optimizationpasses 3
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-repackageclasses ''
-allowaccessmodification
-dontnote

-keepattributes Signature
-keepattributes Exceptions

-keep class com.kodabots.sdk.core.UserProfile { *; }
-keep class com.kodabots.sdk.core.GetUnreadCountResponse { *; }
-keep class com.kodabots.sdk.core.GetUnreadCountResponseData { *; }
