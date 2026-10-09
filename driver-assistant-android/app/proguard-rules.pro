# kotlinx.serialization: keep generated serializers for models and DTOs.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.freighttiger.driverassistant.** {
    *** Companion;
}
-keepclasseswithmembers class com.freighttiger.driverassistant.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.freighttiger.driverassistant.**$$serializer { *; }

# Retrofit service interface and suspend functions.
-keep,allowobfuscation,allowshrinking interface com.freighttiger.driverassistant.core.network.remote.FreightTigerAssistantApi
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
