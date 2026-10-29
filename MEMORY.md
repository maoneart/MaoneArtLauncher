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
