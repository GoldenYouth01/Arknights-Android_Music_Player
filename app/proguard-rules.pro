# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowshrinking,allowoptimization,allowobfuscation @interface retrofit2.http.*

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.example.musicsiren.**$$serializer { *; }
-keepclassmembers class com.example.musicsiren.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.musicsiren.** {
    kotlinx.serialization.KSerializer serializer(...);
}
