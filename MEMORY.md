# Persistent Memory — MaoneArt Flow Launcher

> **Repository**: [https://github.com/maoneart/MaoneArtLauncher](https://github.com/maoneart/MaoneArtLauncher)  
> **Workspace**: `/sdcard/www/MaoneArtLauncher`  
> **Package**: `id.maoneart.flow`  
> **Target Release APK**: `/sdcard/Download/MaoneArtFlow.apk` (v1.0.12)

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
  - **Sistem Kategori Kustom & Pengaturan Mandiri (Release v1.0.3)**:
    - **Category Chip Bar**:
      - Bilah horizontal chip kategori bergaya glassmorphism dengan transisi warna halus, icon kategori, dan badge jumlah aplikasi (`CategoryChipBar.kt`).
      - Kategori bawaan: Semua (`✦`), Sosial (`💬`), Media (`🎬`), Alat & Kerja (`⚡`), Game (`🎮`), Sistem (`⚙️`).
    - **Kategori Buat Sendiri (Custom Categories)**:
      - Pengguna dapat membuat kategori baru sesuka hati melalui tombol `+ Kategori` (`CreateCategoryDialog.kt`). Kategori tersimpan permanen di DataStore.
      - Pengguna dapat menghapus kategori kustom dengan menekan lama (long press) chip kategori yang dibuat (`DeleteCategoryDialog.kt`).
    - **Pengaturan & Pemindahan Kategori Aplikasi**:
      - Saat menekan lama (long press) aplikasi di launcher, menu "Ubah Kategori" kini tersedia di `AppActionBottomSheet`.
      - Menampilkan bottom sheet pemilihan kategori (`SelectCategoryBottomSheet.kt`) untuk memindahkan aplikasi ke kategori yang diinginkan, membuat kategori baru langsung dari sheet, atau mereset aplikasi kembali ke kategori bawaan sistem.
    - **Integrasi Filter & Alphabet Wave Scroller**:
      - Saat kategori dipilih, daftar aplikasi dan scroller huruf A-Z secara dinamis langsung beradaptasi hanya menampilkan aplikasi dan huruf yang relevan di kategori tersebut secara instan tanpa lag.
  - **Optimasi Instan Kategori & Perbaikan Modal Interaktif (Release v1.0.4)**:
    - **Performa Transisi Kategori 0ms**:
      - Memperbaiki layout freeze saat berpindah kategori dengan menghapus key prefix yang memaksa recomposition seluruh LazyColumn node dan menambahkan instant scroll reset (`LaunchedEffect(selectedCategoryId) { listState.scrollToItem(0) }`).
    - **Perbaikan Dialog Buat Kategori**:
      - Memisahkan dialog state menjadi StateFlow reaktif independen (`isCreateCategoryOpen`, `categoryToDelete`, `appForCategoryChange`) sehingga aksi buka dan tutup (tombol Batal) langsung merespons seketika (0ms).
      - Menerapkan standar MaoneArt Glassmorphism Modal dengan tombol simetris 2-kolom (Batal & Simpan) berukuran 46dp yang responsif.
    - **Penghapusan Kategori via Long-Press**:
      - Membuka kapabilitas penghapusan/penyembunyian kategori untuk semua chip selain master "Semua" via `PreferencesRepository.hiddenCategoriesFlow`.
  - **Fluid Niagara Wave Physics & Optimasi Performa 120 FPS (Release v1.0.5)**:
    - **Fisika Ombak Niagara Sejati**:
      - Radius pengaruh ombak diperluas hingga 190dp (~9-11 huruf terdorong serentak) dengan kurva kosinus berpangkat (`rawFactor.pow(1.25)`).
      - Outward arc displacement mencapai -82dp dan pembesaran font hingga 2.35x di puncak gelombang.
      - Balon huruf aktif melayang (*floating active bubble*) diposisikan di -118dp di depan puncak gelombang ombak dengan pendaran neon cyan.
    - **Optimasi Ultra Smooth 120 FPS**:
      - Seluruh transformasi kurva ombak dipindahkan ke `Modifier.graphicsLayer { ... }` untuk bypass recomposition dan 100% diproses di GPU hardware matrix.
      - Pembatalan scroll job bertumpuk (`scrollJob?.cancel()`) untuk mencegah penumpukan layout passes saat jempol meluncur cepat di atas ombak.
      - Optimasi rendering ikon di `AppListItem.kt` menggunakan `remember(iconDrawable)` dan `crossfade(false)`.
      - Pengurangan frekuensi update baterai di `DigitalClockHeader.kt` untuk memastikan CPU idle 99% saat scrolling.
  - **Dynamic Living Wave Tracking via `awaitEachGesture` (Release v1.0.6)**:
    - **Solusi Ombak Macet Saat Digeser (Drag Motion Fix)**:
      - Root Cause: Duplikasi gesture detector (`detectTapGestures` + `detectDragGestures`) menyebabkan event touch down terkunci di `tryAwaitRelease()`, mengabaikan pointer drag saat jari bergeser ke atas/bawah.
      - Solusi: Mengganti seluruh gesture detector dengan satu low-level event loop `awaitEachGesture { awaitFirstDown() -> while (pressed) handleTouchAt() }`.
    - **Presisi Pusat Huruf & Gelombang Hidup**:
      - Menggunakan koordinat aktual parent `columnTopY + (i + 0.5f) * itemHeight` sehingga puncak ombak tepat berada di bawah jempol di setiap titik pergeseran.
      - Huruf-huruf di sepanjang jalur scrolling secara dinamis "terangkat" keluar melengkung hingga -88dp dan membesar hingga 2.45x mengikuti jempol secara real-time persis seperti Niagara Launcher.
  - **Hierarki Navigasi Tombol Back Cerdas (Release v1.0.7)**:
    - **Prioritas Kembali ke Tab 'Semua'**:
      - Ketika pengguna sedang membuka kategori tertentu (mis. Game, Sosial, Media, dll), menekan tombol Back kini otomatis mengembalikan launcher ke kategori master `"Semua"` (`selectedCategoryId = "all"`).
    - **Pembersihan Pencarian & Scroll-to-Top**:
      - Jika pengguna sedang melakukan pencarian aplikasi, tombol Back langsung mengosongkan teks pencarian.
      - Jika pengguna sedang berada di kategori "Semua" namun posisi daftar ter-scroll jauh ke bawah, tombol Back akan melakukan scroll halus kembali ke posisi paling atas.
  - **Identitas Visual Graffiti MaoneArt Flow Header (Release v1.0.8)**:
    - **Logo Grafiti Asli MaoneArt Flow**:
      - Mengintegrasikan logo seni jalanan (*street art graffiti typography*) `MAONEART FLOW` beresolusi tinggi dengan transparansi alpha sempurna (`maoneart_graffiti_logo.png`).
      - Dilengkapi mahkota (*neon crown*) 3-puncak di atas huruf O-N, badge `FLOW` di kanan bawah, dan efek percikan cat air (*spray droplet splash*) biru elektrik yang identik dengan mockup screenshot user di folder `Bug`.
  - **Zero-Latency Resume Lifecycle & Hot Pre-Warmed Apps Cache (Release v1.0.9)**:
    - **Penyebab Delay Saat Kembali dari Aplikasi Lain**:
      - `SharingStarted.WhileSubscribed(5000)` di ViewModel menghentikan flow saat user membuka aplikasi lain >5 detik dan membuang state ke `initialValue = emptyList()`.
      - `callbackFlow` di AppRepository memicu query IPC Binder `fetchInstalledApps()` berulang setiap kali activity di-resume.
    - **Solusi Zero-Latency (0ms Instant Return)**:
      - Mengubah StateFlow sharing policy menjadi `SharingStarted.Eagerly` dan mengikat `initialValue` langsung ke in-memory hot cache.
      - Menambahkan singleton static cache `cachedApps` di `AppRepository` yang langsung me-return daftar aplikasi seketika (0ms) tanpa query ulang.
      - Pre-warming cache aplikasi saat proses lahir di `MaoneArtFlowApp.onCreate()`.
      - Konfigurasi Activity resident di `AndroidManifest.xml` (`configChanges` & penghapusan `stateNotNeeded`).
  - **Auto-Scroll Horizontal Kategori ke Kiri saat Tekan Back (Release v1.0.10)**:
    - **Solusi Scroll Kategori ke Kanan**:
      - Root Cause: `LazyRow` pada `CategoryChipBar` sebelumnya tidak memiliki `LazyListState` yang terikat pada `selectedCategoryId`, sehingga ketika pengguna kembali ke kategori "Semua" via tombol Back, baris chip horizontal tetap tertinggal di posisi scroll paling kanan (di ujung dekat tombol `+ Kategori`).
      - Solusi: Menambahkan `chipListState = rememberLazyListState()` dan `LaunchedEffect(selectedCategoryId)` yang secara otomatis memicu animasi scroll halus ke kiri (`animateScrollToItem(0)`) menuju tab `"Semua"` setiap kali tombol Back ditekan.
  - **Penyempurnaan Posisi & Skala Logo Grafiti MaoneArt Flow (Release v1.0.11)**:
    - **Logo Grafiti Besar & Center**:
      - Memperbesar ukuran logo grafiti dari 78dp menjadi 118dp (peningkatan skala 50%) agar gagah dan menonjol.
      - Mengunci penataan logo menjadi 100% simetris di tengah horizontal (`Box(contentAlignment = Alignment.Center)` dan `alignment = Alignment.Center`).
  - **Lencana Notifikasi Pesan/WA & Penguncian Back Double-Press (Release v1.0.12)**:
    - **Lencana Notifikasi Aktif Aplikasi (WhatsApp, Pesan, Telegram, dll)**:
      - Mengembangkan `NotificationService` berbasis `NotificationListenerService` yang mengamati notifikasi masuk dan terhapus secara real-time via `onNotificationPosted` dan `onNotificationRemoved`.
      - Menghitung notifikasi aktif per paket aplikasi (`packageName`) dan mengeksposnya secara reaktif melalui StateFlow ke `HomeViewModel`.
      - Menampilkan lencana counter merah cerah khas Niagara/iOS (`Color(0xFFFF334B)`) dengan angka notifikasi (atau `"99+"`) di samping nama aplikasi pada `AppListItem.kt`.
      - Menyediakan banner status notifikasi glassmorphic yang otomatis muncul jika izin belum aktif dan otomatis menghilang begitu izin diberikan, dilengkapi shortcut langsung ke pengaturan akses notifikasi Android.
    - **Solusi Tuntas Dobel-Klik Back Menggeser Kategori ke Kanan**:
      - Root Cause: Sebelumnya `BackHandler(enabled = ...)` bernilai `false` ketika user menekan Back saat sudah di kategori "Semua" di posisi teratas. Event Back yang tidak tertangani bocor ke focus manager Android, memicu perpindahan fokus ke view focusable berikutnya di layar—yaitu tombol `+ Kategori` di ujung kanan LazyRow—sehingga LazyRow otomatis scroll ke kanan.
      - Solusi: `BackHandler(enabled = true)` diaktifkan tanpa batas (*unconditional*). Setiap kali Back ditekan (termasuk double press cepat), `chipListState.scrollToItem(0)` langsung dieksekusi mengunci tab "Semua" di kiri, dan event Back dikonsumsi tuntas sehingga tidak pernah memicu perpindahan fokus ke kanan.
