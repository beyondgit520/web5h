# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\ProgramFiles\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

#--------------- BEGIN: Gson防混淆 ----------
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.idea.fifaalarmclock.entity.***
-keep class com.google.gson.stream.** { *; }
#--------------- END ----------

#--------------- BEGIN: 返回到页面的自定义Java对象防混淆 ----------
#-keepclassmembers class cn.pedant.SafeWebViewBridge.sample.HostJsScope$RetJavaObj{ *; }
#--------------- END ----------

#--------------- BEGIN: 注入到页面的接口类防混淆 ----------
-keepclassmembers class cn.com.husan.cs.AppJs{ *; }
#--------------- END ----------

#okhttp start
-dontwarn okhttp3.**
-keep class okhttp3.** { *;}
-dontwarn okio.**
#okhttp end
