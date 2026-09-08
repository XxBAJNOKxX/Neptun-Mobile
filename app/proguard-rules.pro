# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.example.**$$serializer { *; }
-keepclassmembers class com.example.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# --- Glance / AppWidget ---
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# --- Biometric ---
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# --- EncryptedSharedPreferences (reflection alapú prefs) ---
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# --- WorkManager ---
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.Worker { *; }

# --- Domain modellek (konstruktor reflexió különböző könyvtárakból) ---
-keep class com.example.domain.model.** { *; }
-keep class com.example.data.local.entity.** { *; }
