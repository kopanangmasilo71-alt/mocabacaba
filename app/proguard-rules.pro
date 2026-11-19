# Consolidate attributes
-keepattributes Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable,*Annotation*,JavascriptInterface

# Konfetti specific rules
-keep class nl.dionsegijn.konfetti.** { *; }

# General Android rules
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Keep the data classes
-keep class com.masilo.morabarabacollection.GameData { *; }

# AdMob & Google Play Services
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.**

# User Messaging Platform (UMP)
-keep class com.google.android.ump.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Additional Library Support
-keep class de.hdodenhof.circleimageview.** { *; }
-keep class androidx.cardview.widget.** { *; }

# AdMob Identifier Safety
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { *; }

# --- WORKMANAGER & ROOM FIX (Resolves "Failed to create instance" Crash) ---

# Ignore analysis warnings for internal components
-dontwarn androidx.room.**
-dontwarn androidx.work.**
-dontwarn androidx.sqlite.**

# Preserves the database and its generated implementation
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.impl.** { *; }
-keep class androidx.startup.InitializationProvider { *; }
-keep class androidx.work.WorkManagerInitializer { *; }

# Avoid stripping background services
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class androidx.work.impl.background.systemalarm.SystemAlarmService { *; }

# SQLite & Lifecycle safety
-keep class androidx.sqlite.** { *; }
-keep class androidx.lifecycle.** { *; }

# Comprehensive WorkManager keep rules
-keep class androidx.work.** { *; }
-keep class androidx.room.** { *; }
