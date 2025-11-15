# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/youruser/Library/Android/sdk/tools/proguard/proguard-android-optimize.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with HTML files, uncomment the following
# line:
# -keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
# }

# If you employ Java serialization, add the following lines:
# -keepnames class * implements java.io.Serializable
# -keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    !static !transient <fields>;
#    !private <fields>;
#    !private <methods>;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
# }