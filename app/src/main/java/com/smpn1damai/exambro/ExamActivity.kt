package com.smpn1damai.exambro

import android.app.ActivityManager
import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.smpn1damai.exambro.databinding.ActivityExamBinding

class ExamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExamBinding
    private var exitPassword = ""
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var batteryReceiver: BroadcastReceiver
    private var cheatCount = 0
    private var lastToast: Toast? = null
    private var isFirstLaunch = true // Deteksi awal buka aplikasi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        binding = ActivityExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exitPassword = intent.getStringExtra("EXIT_PWD") ?: "guru123"

        setupWebView(intent.getStringExtra("TARGET_URL") ?: "https://google.com")

        startLockTask() // Langsung hajar minta pin
        setupBatteryAndNetwork()

        binding.btnKeluar.setOnClickListener {
            binding.exitOverlay.visibility = View.VISIBLE
            binding.etPassword.requestFocus()
        }

        binding.btnBatalKeluar.setOnClickListener {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                // Kalo lagi dihukum karena nolak pin, kagak boleh klik Batal!
                Toast.makeText(this, "MASUKKAN SANDI! ANDA MELANGGAR ATURAN!", Toast.LENGTH_SHORT).show()
            } else {
                binding.exitOverlay.visibility = View.GONE
            }
        }

        binding.btnKonfirmasiKeluar.setOnClickListener {
            if (binding.etPassword.text.toString() == exitPassword) { exitApp() }
            else {
                binding.etPassword.error = "Sandi Salah!"
                triggerAlarm()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                    triggerMaxAlarm() // Kalo mau kabur pas dihukum, hajar lagi alarmnya
                } else if (binding.exitOverlay.visibility == View.VISIBLE) {
                    binding.exitOverlay.visibility = View.GONE
                } else {
                    triggerAlarm()
                }
            }
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                if (isFirstLaunch) {
                    isFirstLaunch = false // Abaikan kedipan fokus pertama saat dialog sistem muncul
                } else {
                    // JIKA SISWA KLIK "NO/TOLAK" PINNING -> LANGSUNG HUKUM!
                    binding.exitOverlay.visibility = View.VISIBLE
                    binding.etPassword.requestFocus()
                    triggerMaxAlarm()
                }
            } else {
                isFirstLaunch = false // Kalau dia klik OK, ya aman
            }
        } else {
            isFirstLaunch = false
        }
    }

    // Fungsi Alarm Brutal Langsung Bunyi (Bypass Peringatan 3x)
    private fun triggerMaxAlarm() {
        if (mediaPlayer == null) {
            try {
                val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer.create(this, uri)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e: Exception) {}
        }
        lastToast?.cancel()
        lastToast = Toast.makeText(this, "PELANGGARAN: PINNING DITOLAK!", Toast.LENGTH_LONG)
        lastToast?.show()
    }

    // Fungsi Alarm Biasa buat yang iseng nge-swipe layar
    private fun triggerAlarm() {
        cheatCount++
        lastToast?.cancel()

        if (cheatCount < 3) {
            lastToast = Toast.makeText(this, "Dilarang Swipe! ($cheatCount/3)", Toast.LENGTH_SHORT)
            lastToast?.show()
        } else {
            triggerMaxAlarm()
        }
    }

    private fun setupWebView(url: String) {
        val settings = binding.webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.userAgentString = settings.userAgentString.replace("; wv", "")

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val currentUrl = request?.url.toString()
                return !currentUrl.contains("google.com") && !currentUrl.contains("gstatic.com")
            }
        }
        binding.webView.loadUrl(url)
    }

    private fun setupBatteryAndNetwork() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level != -1 && scale != -1) {
                    binding.tvBattery.text = "${(level * 100 / scale)}%"
                }
            }
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cap = cm.getNetworkCapabilities(cm.activeNetwork)
        if (cap != null) {
            if (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                binding.iconSignal.setImageResource(R.drawable.ic_wifi)
            } else if (cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                binding.iconSignal.setImageResource(R.drawable.ic_signal_cellular)
            }
        }
    }

    private fun exitApp() {
        CookieManager.getInstance().removeAllCookies(null)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopLockTask()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(batteryReceiver) } catch (e: Exception) {}
        mediaPlayer?.release()
    }
}