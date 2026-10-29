# Persistent Memory — MaoneArt Flow Launcher

> **Repository**: [https://github.com/maoneart/MaoneArtLauncher](https://github.com/maoneart/MaoneArtLauncher)  
> **Workspace**: `/sdcard/www/MaoneArtLauncher`  
> **Package**: `id.maoneart.flow`  
> **Target Release APK**: `/sdcard/Download/MaoneArtFlow.apk` (v1.0.0)

---

## 📌 Architecture & Technical Decisions
1. **Niagara Interaction Paradigm**:
   - Ergonomi 1-tangan: tata letak daftar vertikal minimalis yang dapat diakses penuh hanya dengan jempol.
   - **Alphabet Wave Scroller**: Scroller huruf A-Z vertikal di tepi layar yang melengkung keluar secara dinamis (wave projection `scale` 1.0x -> 1.75x dan `offsetX` -24dp) dengan tactile haptic feedback per perpindahan huruf.
2. **Permanent Signing Keystore (`keystore/release.jks`)**:
   - Sertifikat RSA 2048-bit mandiri (`SHA384withRSA`) berlaku hingga tahun 2054.
   - Update APK tidak memerlukan uninstall.
3. **Optimasi Performa 120 FPS**:
   - `ConcurrentHashMap` icon cache di `AppRepository` untuk menghindari query Binder IPC berulang saat scrolling.
   - Flow-based reactive architecture dengan DataStore Preferences.
4. **CI/CD Build Pipeline**:
   - GitHub Actions (`.github/workflows/build_apk.yml`) otomatis build release APK dan download ke `/sdcard/Download/MaoneArtFlow.apk`.

---

## 🚀 Milestones
- **29 Okt 2026**:
  - Inisialisasi arsitektur proyek MaoneArt Flow (Release v1.0.0).
  - Implementasi Android Home Launcher Intent (`ACTION_MAIN` & `CATEGORY_HOME`).
  - Implementasi `AlphabetWaveScroller`, `DigitalClockHeader`, `FlowSearchBar`, `AppListItem`, dan `AppActionBottomSheet`.
  - Integrasi DataStore Preferences untuk pengelolaan Favorit dan Aplikasi Tersembunyi.
  - Pipeline CI/CD GitHub Actions berhasil mengompilasi release APK bertanda tangan `release.jks` permanen dan diunduh ke `/sdcard/Download/MaoneArtFlow.apk`.
  - **Perbaikan Keyboard Auto-Hide pada Pencarian Aplikasi (Release v1.0.1)**:
    - Root Cause: Search bar sebelumnya berada di dalam percabangan `LazyColumn` dengan dua key berbeda (`header_search` dan `search_active_bar`). Saat mengetik huruf pertama, `isSearching` berubah menjadi true sehingga view search bar pertama di-dispose oleh Compose dan memicu sistem Android menutup soft keyboard secara mendadak.
    - Solusi: `FlowSearchBar` diposisikan secara permanen di luar `LazyColumn` dengan fokus stabil, `adjustResize` pada window soft input mode, dan aksi search langsung meluncurkan aplikasi hasil teratas. Keyboard tetap terbuka mulus selama pengetikan.
  - **Animasi Alphabet Wave Niagara & Header Jam Elegan Widget MaoneArt (Release v1.0.2)**:
    - **Alphabet Wave Scroller Otentik Niagara**:
      - Ditambahkan fisika pegas elastis (`waveIntensity` dengan `animateFloatAsState` spring damping 0.58) yang meregangkan gelombang saat disentuh dan memantul halus saat dilepas.
      - Radius pengaruh gelombang diperluas hingga 110dp (~5-6 huruf) dengan kurva proyeksi kosinus mulus (`scale` membesar hingga 2.15x dan `offsetX` melengkung keluar hingga -48dp).
      - Balon indikator huruf melayang (*floating active balloon*) 62dp dengan bayangan radial electric blue yang meluncur mengikuti jari secara real-time.
    - **Header Jam Elegan & Widget Teks Eksklusif MaoneArt**:
      - Widget teks branding eksklusif `"M A O N E A R T"` dengan font sans-serif modern, letter-spacing lebar 4.5sp, dan gradasi warna elektrik (Cyan -> Electric Blue -> Violet), dilengkapi pill frosted glass `"FLOW"`.
      - Desain jam digital tipografi modern 62sp dengan bobot font ramping (*light weight*) yang mewah dan bersih.
      - Tombol interaktif langsung: tap jam membuka Alarm/Clock, tap tanggal membuka Kalender, tap pil baterai membuka Pengaturan Penggunaan Daya Baterai.
