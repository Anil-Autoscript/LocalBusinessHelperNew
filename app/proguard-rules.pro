# ============================================================
# Local Business Helper - ProGuard Rules
# ============================================================

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ── Kotlin ───────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Lazy {
    <fields>;
    <methods>;
}

# ── Kotlin Coroutines ─────────────────────────────────────────
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ── AndroidX / Jetpack ───────────────────────────────────────
-keep class androidx.** { *; }
-dontwarn androidx.**

# ── ViewModel & LiveData ─────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ── Room Database ─────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverters class * { *; }
-keepclassmembers @androidx.room.TypeConverters class * { *; }
-dontwarn androidx.room.paging.**

# ── WorkManager ───────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── Navigation ────────────────────────────────────────────────
-keep class * extends androidx.fragment.app.Fragment { *; }
-keepnames class * extends androidx.fragment.app.Fragment

# ── App Data Classes & Enums ─────────────────────────────────
-keep class com.localbusiness.helper.** { *; }
-keepclassmembers class com.localbusiness.helper.** { *; }

# ── Retrofit ─────────────────────────────────────────────────
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# ── OkHttp ───────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ── Gson ─────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── ThreeTenABP ──────────────────────────────────────────────
-keep class org.threeten.** { *; }
-dontwarn org.threeten.**

# ── Enum classes (critical - Room uses enum names) ───────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}
