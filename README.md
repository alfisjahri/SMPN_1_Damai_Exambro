# 🛡️ SMPN 1 Damai Exambro

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/Version-1.0.0-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Stable-success?style=flat-square)

**SMPN 1 Damai Exambro** adalah aplikasi *Exam Browser* berbasis Android yang dirancang khusus untuk memastikan integritas, keamanan, dan kenyamanan siswa saat melaksanakan ujian digital (menggunakan Google Forms atau platform web ujian lainnya). 

Dibangun menggunakan **Kotlin Native**, aplikasi ini sangat ringan, responsif (Zero Delay), dan sangat aman dari kecurangan.

---

## ✨ Fitur Unggulan

* 🚀 **Zero-Delay Loading:** Soal ujian dimuat secara *asynchronous* di latar belakang bersamaan dengan otorisasi sistem, menghilangkan waktu tunggu layar kosong/putih.
* 🔒 **Kiosk Mode (Pinned App):** Memaksa perangkat mengunci aplikasi di layar depan. Siswa tidak dapat membuka aplikasi lain, melihat notifikasi, atau kembali ke *Home*.
* 📸 **Anti-Screenshot & Screen Record:** Menggunakan pengamanan tingkat kernel (`FLAG_SECURE`) yang memblokir semua upaya perekaman layar atau tangkapan layar.
* 🚨 **Sistem Anti-Cheat & Alarm:** Alarm sirine akan otomatis berbunyi maksimal jika siswa mencoba menolak mode kunci (*Screen Pinning*) atau mencoba melakukan *swipe* navigasi (tombol *Back*).
* 📊 **Smart Custom Status Bar:** Menggantikan status bar bawaan HP dengan indikator mandiri yang menampilkan **Jam, Sinyal Dinamis (WiFi/Seluler), dan Persentase Baterai** secara *real-time*.
* 🧹 **Auto-Clear Session:** Sangat cocok untuk **tablet sekolah yang digunakan bergantian**. Aplikasi secara otomatis menghapus *cookies*, riwayat, dan sesi login Google saat aplikasi ditutup.

---

## 🛠️ Panduan Penggunaan Bagi Guru / Panitia

Untuk masuk ke soal, siswa cukup memindai **QR Code** yang telah disiapkan oleh panitia. Aplikasi ini dilengkapi dengan sistem keamanan **Password Keluar** yang disematkan langsung ke dalam QR Code.

### Cara Membuat Link QR Code:
Tambahkan parameter `?pwd=SANDI_PILIHAN` di ujung link soal ujian Anda.

**Contoh:**
Jika link soal Google Form Anda adalah:
`https://docs.google.com/forms/d/e/1FAIpQLS/viewform`

Dan Anda ingin sandi keluarnya adalah **`lulus2026`**, maka ubah link tersebut menjadi:
`https://docs.google.com/forms/d/e/1FAIpQLS/viewform?pwd=lulus2026`

> **PENTING:** > Jadikan link yang sudah ditambah password tersebut menjadi QR Code. Jika Anda tidak menambahkan parameter `?pwd=` di URL, maka aplikasi akan menggunakan sandi *default* yaitu: **`guru123`**.

---

## 📥 Cara Instalasi

1. Buka halaman [Releases](../../releases) di repository ini.
2. Unduh file APK terbaru (contoh: `SMPN1Damai-Exambro-v1.0.0.apk`).
3. Pindahkan ke HP Android siswa atau Tablet Sekolah.
4. Buka file APK tersebut dan klik **Install** (pastikan izin *Install from Unknown Sources* sudah diaktifkan di HP).
5. Berikan izin Akses Kamera saat pertama kali dibuka.
6. Aplikasi siap digunakan untuk memindai QR Code ujian!

---

## 💻 Tech Stack & Library

Aplikasi ini dikembangkan di **Android Studio** dengan spesifikasi:
* **Language:** Kotlin
* **Minimum SDK:** API 24 (Android 7.0 Nougat)
* **Scanner Engine:** [Google ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning) & CameraX
* **Architecture:** Native Android (Empty Views Activity)

---

## 👨‍💻 Kontribusi
Proyek ini dibuat untuk mendukung digitalisasi pendidikan di SMPN 1 Damai. Jika Anda menemukan *bug* atau memiliki saran fitur tambahan, silakan buat *Issue* atau kirimkan *Pull Request*.

<p align="center">
  <i>"Membangun Integritas Pendidikan Melalui Teknologi"</i>
</p>
