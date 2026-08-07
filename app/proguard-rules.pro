# ---------------------------------------------------------------------------
# Room
# Room generates its implementations at compile time, but the entity is read
# reflectively by the type converters.
# ---------------------------------------------------------------------------
-keep class com.example.booktracker.data.Book { *; }

# ---------------------------------------------------------------------------
# kotlinx.serialization
# The compiler plugin emits a $$serializer for every @Serializable class and
# reaches it through the Companion. R8 cannot see those links, so shrinking a
# release build would otherwise fail only at runtime, when a response is parsed.
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisible*

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.example.booktracker.data.remote.**$$serializer { *; }
-keepclassmembers class com.example.booktracker.data.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.booktracker.data.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------------
# Retrofit
# Interface methods are invoked through a dynamic proxy, and suspend functions
# carry their return type only in the generic signature.
# ---------------------------------------------------------------------------
-keep,allowobfuscation interface com.example.booktracker.data.remote.OpenLibraryApi
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# OkHttp references these optional platform classes; they are absent on Android.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
