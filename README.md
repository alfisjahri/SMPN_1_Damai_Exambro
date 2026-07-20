# SMPN 1 Damai Exambro

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/Version-1.3-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Stable-success?style=flat-square)

**SMPN 1 Damai Exambro** ini aplikasi *Exam Browser* Android yang saya buat khusus untuk menjaga keamanan dan kenyamanan ujian digital anak-anak di sekolah (pakai Google Forms atau platform ujian lain). Dibangun murni pakai **Kotlin Native**, jadi aplikasinya ringan banget, responsif, dan aman dari kecurangan.

## 🔥 Fitur Unggulan (Update v1.3)

*   **Scan dari Galeri & Input Manual (BARU):** Kalau kamera HP anak-anak lagi bermasalah, sekarang bisa masukin soal ujian lewat tombol *Ketik Link* manual atau ambil gambar QR Code langsung dari *Galeri*.
*   **Tombol Keluar Dinamis / Auto-Hide (BARU):** Tombol keluar (gembok) bakal otomatis sembunyi waktu siswa lagi *scroll* baca soal, jadi gak menutupi layar dan lebih nyaman.
*   **Toleransi Anti-Cheat (BARU):** Sistem gak langsung ngunci/alarm di awal. Ada toleransi peringatan (maksimal 3 kali) kalau anak-anak nyoba *swipe* navigasi (tombol *Back*) sebelum akhirnya memicu pelanggaran maksimal.
*   **Simpan Sesi Login / Cookie Nempel (BARU):** Fitur hapus riwayat otomatis sekarang ditiadakan. Cookie Google sengaja dibiarkan nempel biar anak-anak gak usah repot *login* berulang kali kalau aplikasinya gak sengaja ketutup.
*   **Zero-Delay Loading:** Soal ujian langsung dimuat di *background*, jadi gak ada lagi layar putih nunggu *loading*.
*   **Kiosk Mode (Pinned App):** Mengunci aplikasi di layar depan. Siswa gak bisa buka aplikasi lain atau ngecek notifikasi.
*   **Anti-Screenshot & Screen Record:** Pakai sistem keamanan tingkat kernel (`FLAG_SECURE`), mustahil buat direkam atau di-screenshot.
*   **Smart Custom Status Bar:** Status bar bawaan HP ditimpa pakai indikator mandiri untuk nampilin Jam, Persentase Baterai, dan Sinyal Dinamis (sistem nge-ping otomatis ke `8.8.8.8` tiap 5 detik untuk ngecek kestabilan jaringan).

## 📖 Panduan Buat Guru / Panitia

Biar siswa bisa masuk ke soal, cukup sediakan **QR Code**. Aplikasi ini udah dilengkapi sistem **Password Keluar** yang ditempel langsung ke dalam link QR Code-nya.

### Cara Bikin Link QR Code:
Tambahkan parameter `?pwd=SANDIPILIHAN` di paling ujung link ujian.

**Contoh:**
Link asli Google Form: `https://docs.google.com/forms/d/e/1FAIpQLS/viewform`
Mau pakai sandi keluar **`lulus2026`**, maka ubah linknya jadi:
`https://docs.google.com/forms/d/e/1FAIpQLS/viewform?pwd=lulus2026`

> **PENTING:**
> Link yang sudah ditambahin parameter password tersebut baru diubah jadi QR Code. Kalau lupa nambahin parameter `?pwd=`, aplikasi bakal pakai sandi *default* dari sistem yaitu: **`guru123`**.

## 🚀 Cara Instalasi

1. Buka halaman [Releases](../../releases) di repository ini.
2. Download file APK versi terbaru (`SMPN1Damai-Exambro-v1.3.apk`).
3. Install di HP Android siswa atau Tablet Sekolah (pastikan *Install from Unknown Sources* sudah diaktifkan).
4. Kasih izin Akses Kamera waktu pertama kali dibuka.
5. Aplikasi siap dipakai buat scan QR Code ujian!

## 🛠 Tech Stack & Library

*   **Language:** Kotlin
*   **Minimum SDK:** API 24 (Android 7.0 Nougat)
*   **Target SDK:** API 37
*   **Scanner Engine:** Google ML Kit Barcode Scanning & CameraX
*   **Architecture:** Native Android (Empty Views Activity)
