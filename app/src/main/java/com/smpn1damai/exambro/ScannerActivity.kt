package com.smpn1damai.exambro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView

    // PENANGKAP GAMBAR DARI GALERI
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val image = InputImage.fromFilePath(this, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            goToExam(barcodes[0].rawValue ?: "")
                        } else {
                            Toast.makeText(this, "QR Code tidak ditemukan pada gambar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal membaca gambar", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) { startCamera() }
        else { Toast.makeText(this, "Izin kamera ditolak!", Toast.LENGTH_SHORT).show() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        viewFinder = findViewById(R.id.viewFinder)
        cameraExecutor = Executors.newSingleThreadExecutor()

        findViewById<Button>(R.id.btnGaleri).setOnClickListener { galleryLauncher.launch("image/*") }
        findViewById<Button>(R.id.btnManual).setOnClickListener { showManualInputDialog() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy -> processImageProxy(imageProxy) }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val url = barcodes[0].rawValue ?: ""
                        if (goToExam(url)) cameraExecutor.shutdown()
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun showManualInputDialog() {
        val input = EditText(this)
        input.hint = "Contoh: https://ujian.com/?pwd=sandi"
        input.setPadding(40, 40, 40, 40)

        AlertDialog.Builder(this)
            .setTitle("Masukkan Link Ujian")
            .setView(input)
            .setPositiveButton("MULAI") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) goToExam(url)
            }
            .setNegativeButton("BATAL", null)
            .show()
    }

    private fun goToExam(rawUrl: String): Boolean {
        val scannedUrl = normalizeUrl(rawUrl)
        val pwd = extractPwd(scannedUrl)
        if (pwd.isNullOrBlank()) {
            // Gagal di sini saja: tetap di scanner supaya bisa coba lagi,
            // jangan pindah ke ExamActivity yang langsung finish (mental ke home).
            Toast.makeText(this, "Link tidak berisi sandi pengawas (?pwd=). Cek lagi linknya!", Toast.LENGTH_LONG).show()
            return false
        }
        val intent = Intent(this, ExamActivity::class.java)
        intent.putExtra("TARGET_URL", scannedUrl)
        intent.putExtra("EXIT_PWD", pwd)
        startActivity(intent)
        finish()
        return true
    }

    // Normalisasi: "https:example.com/..." (tanpa //, gampang kejadian saat ketik
    // cepat di HP) -> "https://example.com/..." agar WebView + pembaca parameter jalan normal.
    private fun normalizeUrl(raw: String): String {
        val url = raw.trim()
        val match = Regex("^(https?:)(?!//)(.+)$").find(url)
        return if (match != null) "${match.groupValues[1]}//${match.groupValues[2]}" else url
    }

    // Ambil ?pwd= dengan dua cara: Uri standar, lalu cari mentah di string.
    // Uri Android mengembalikan null untuk URL tanpa // (bukan hierarchical),
    // jadi link "https:domain.com/?pwd=12345" sempat lolos padahal sandinya ada.
    private fun extractPwd(url: String): String? {
        try {
            val p = android.net.Uri.parse(url).getQueryParameter("pwd")
            if (!p.isNullOrBlank()) return p
        } catch (e: Exception) {}
        val idx = url.indexOf("pwd=")
        if (idx >= 0) {
            val rest = url.substring(idx + 4).substringBefore("&").substringBefore("#")
            if (rest.isNotBlank()) return rest
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!cameraExecutor.isShutdown) cameraExecutor.shutdown()
    }
}