# Preserve metadata used by Firebase / Kotlin serialization reflection.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# Firestore / Realtime Database model mapping uses reflection on fields and constructors.
# Keeping model classes prevents runtime breakage when R8 obfuscates member names.
-keep class com.drape.data.model.** { *; }
-keepclassmembers enum com.drape.data.model.** { *; }

# Keep type-safe navigation routes and generated serializers.
-keep @kotlinx.serialization.Serializable class com.drape.navigation.** { *; }
-keepclassmembers class com.drape.navigation.** {
    *** Companion;
}
-keepclassmembers class com.drape.navigation.**$Companion { *; }
-keep class com.drape.navigation.**$$serializer { *; }

# Keep generated serializer accessors that may be resolved reflectively.
-keepclassmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
