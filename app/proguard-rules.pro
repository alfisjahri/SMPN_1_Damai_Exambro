# --- SURAT SAKTI EXAMBRO ---

# 1. Lindungi Kamera (CameraX) biar gak dihapus
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# 2. Lindungi Scanner Barcode (ML Kit)
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# 3. Lindungi fungsi WebView
-keepclassmembers class * extends android.webkit.WebViewClient { *; }
-keepclassmembers class * extends android.webkit.WebChromeClient { *; }

# 4. Lindungi fungsi dasar AndroidX
-keep class androidx.lifecycle.** { *; }
-keep class androidx.annotation.** { *; }