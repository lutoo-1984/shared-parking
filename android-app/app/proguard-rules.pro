# 添加项目特定的 ProGuard 规则
# 默认情况下，此文件位于模块的根目录中

# 保留 Retrofit 和 Gson 的类
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.sharedparking.android.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class com.google.gson.** { *; }

# Retrofit 特定规则
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp 规则
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson 规则
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# 协程规则
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# 视图绑定和数据绑定
-keep class * extends androidx.viewbinding.ViewBinding { *; }

# 保留自定义 View
-keep class * extends android.view.View { *; }

# 保留自定义 Application
-keep class com.sharedparking.android.application.** { *; }

# 保留 Parcelable 实现类
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 保留 Serializable 实现类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留本地方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留 Activity 等组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# 保留资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 移除日志
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}