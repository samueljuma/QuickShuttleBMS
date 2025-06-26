
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Keep Kotlinx Serialization annotations and metadata
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Keep classes annotated with @Serializable
-keepclasseswithmembers class ** {
    @kotlinx.serialization.Serializable <methods>;
}

# Keep serial names and serializers
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
    public static ** serializer(...);
}

# Keep companion objects and serializer methods
-keepclassmembers class ** {
    public static ** Companion;
}

# Keep all of kotlinx.serialization library internals
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

-keep class com.buupass.quickshuttle.data.models.** { *; }
