package com.smpn1damai.exambro

import android.app.ActivityManager
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.smpn1damai.exambro.databinding.ActivityExamBinding
import java.net.InetSocketAddress
import java.net.Socket

class ExamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExamBinding
    private var exitPassword = ""
    private lateinit var batteryReceiver: BroadcastReceiver
    private var cheatCount = 0
    private var lastToast: Toast? = null
    private var isFirstLaunch = true
    private var isFabHidden = false
    private var isExiting = false
    private var lastUiRevealWarningMs = 0L

    private val networkHandler = Handler(Looper.getMainLooper())
    private val pingRunnable = object : Runnable {
        override fun run() {
            checkActiveInternetAndPing()
            networkHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        binding = ActivityExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tanpa sandi dari QR, ujian tidak dimulai (fail-closed):
        // sandi default publik tidak lagi dipakai.
        exitPassword = intent.getStringExtra("EXIT_PWD") ?: ""
        if (exitPassword.isBlank()) {
            Toast.makeText(this, "QR tidak berisi sandi pengawas. Lapor ke pengawas!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupWebView(intent.getStringExtra("TARGET_URL") ?: "https://google.com")

        startLockTask()
        setupBatteryAndNetwork()
        networkHandler.post(pingRunnable)

        // Peringatan saat user swipe dari tepi layar (memunculkan navigation bar):
        // di mode immersive, swipe itu sendiri tidak memicu callback lain,
        // jadi dengarkan perubahan visibilitas system UI.
        @Suppress("DEPRECATION")
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val navVisible = (visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
            if (navVisible && !isExiting) {
                val now = System.currentTimeMillis()
                if (now - lastUiRevealWarningMs > 3000) {
                    lastUiRevealWarningMs = now
                    triggerAlarm()
                }
            }
        }

        // HIDE FAB SAAT SCROLL
        binding.webView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && !isFabHidden) {
                isFabHidden = true
                binding.btnKeluarFloat.animate().translationY(300f).setDuration(250).start()
            } else if (scrollY < oldScrollY && isFabHidden) {
                isFabHidden = false
                binding.btnKeluarFloat.animate().translationY(0f).setDuration(250).start()
            }
        }

        binding.btnKeluarFloat.setOnClickListener { view ->
            view.animate().scaleX(0.85f).scaleY(0.85f).setDuration(100).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                binding.exitOverlay.visibility = View.VISIBLE
                binding.etPassword.requestFocus()
            }.start()
        }

        binding.btnBatalKeluar.setOnClickListener {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
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
                    triggerMaxAlarm()
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
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                if (isFirstLaunch) { isFirstLaunch = false }
                else {
                    binding.exitOverlay.visibility = View.VISIBLE
                    binding.etPassword.requestFocus()
                    triggerMaxAlarm()
                }
            } else { isFirstLaunch = false }
        } else { isFirstLaunch = false }
    }

    // Tombol navigasi (Home/Recents) juga memicu peringatan, bukan cuma swipe/back.
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isExiting) triggerAlarm()
    }

    private fun checkActiveInternetAndPing() {
        Thread {
            var pingResult = -1L
            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress("8.8.8.8", 53), 2500)
                pingResult = System.currentTimeMillis() - startTime
                socket.close()
            } catch (e: Exception) { pingResult = -1L }
            runOnUiThread { updateSignalUI(pingResult) }
        }.start()
    }

    private fun updateSignalUI(ping: Long) {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cap = cm.getNetworkCapabilities(cm.activeNetwork)
        if (cap != null && ping != -1L) {
            when {
                ping < 100 -> {
                    binding.dotOnline.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#22C55E"))
                }
                ping in 100..300 -> {
                    binding.dotOnline.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFC107"))
                    lastToast?.cancel()
                    lastToast = Toast.makeText(this, "Jaringan kurang stabil (Ping: ${ping}ms)", Toast.LENGTH_SHORT).apply { show() }
                }
                else -> {
                    binding.dotOnline.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF5252"))
                    lastToast?.cancel()
                    lastToast = Toast.makeText(this, "Koneksi buruk! Disarankan ganti jaringan.", Toast.LENGTH_SHORT).apply { show() }
                }
            }
        } else {
            binding.dotOnline.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF5252"))
            lastToast?.cancel()
            lastToast = Toast.makeText(this, "Tidak ada koneksi internet!", Toast.LENGTH_SHORT).apply { show() }
        }
    }

    private fun triggerMaxAlarm() {
        // Popup-nya SAMA dengan yang muncul saat klik gembok (exitOverlay),
        // bukan dialog AlertDialog yang berbeda.
        binding.exitOverlay.visibility = View.VISIBLE
        binding.etPassword.requestFocus()
        lastToast?.cancel()
        lastToast = Toast.makeText(this, "PELANGGARAN: PINNING DITOLAK!", Toast.LENGTH_LONG).apply { show() }
    }

    private fun triggerAlarm() {
        cheatCount++
        lastToast?.cancel()
        if (cheatCount < 3) {
            lastToast = Toast.makeText(this, "Dilarang Swipe! ($cheatCount/3)", Toast.LENGTH_SHORT).apply { show() }
        } else {
            triggerMaxAlarm()
        }
    }

    private fun setupWebView(url: String) {
        val settings = binding.webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        // Ini penting banget biar Google ngenalin webview kita dan ngizinin login
        settings.userAgentString = settings.userAgentString.replace("; wv", "")

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
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
                    val pct = level * 100 / scale
                    binding.tvBattery.text = "$pct%"
                }
            }
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun exitApp() {
        // HAPUS KODE COOKIE DI SINI. Dibiarkan kosong biar riwayat login nempel.
        isExiting = true
        networkHandler.removeCallbacks(pingRunnable)
        stopLockTask()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(batteryReceiver) } catch (e: Exception) {}
        networkHandler.removeCallbacks(pingRunnable)
    }
}