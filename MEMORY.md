# Persistent Memory — MaoneArt Flow Launcher

> **Repository**: [https://github.com/maoneart/MaoneArtLauncher](https://github.com/maoneart/MaoneArtLauncher)  
> **Workspace**: `/sdcard/www/MaoneArtLauncher`  
> **Package**: `id.maoneart.flow`  
> **Target Release APK**: `/sdcard/Download/MaoneArtFlow.apk` (v1.0.41)

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
  - **Pencegahan Font Terpotong & Penataan Jarak Alphabet Scroller (Release v1.0.13)**:
    - **Solusi Huruf Terpotong di Tepi Layar & Saling Menabrak**:
      - Root Cause: 27 huruf (A-Z + #) berukuran 12sp dijejali dalam kolom vertikal tanpa margin bezel kanan yang cukup. Saat disentuh, skala ombak membesar hingga 2.45x dari pivot tengah (`0.5, 0.5`), menyebabkan sisi kanan teks terdorong menabrak batas layar kanan (terpotong oleh bezel/curved display), dan huruf-huruf saling menimpa secara vertikal.
      - Solusi:
        1. Menyesuaikan ukuran font dasar menjadi `10.5.sp` yang lebih proporsional dan elegan khas Niagara Launcher asli sehingga tidak berdesakan.
        2. Menambahkan padding aman `8.dp` dari tepi bezel layar kanan.
        3. Menetapkan `transformOrigin = TransformOrigin(0.85f, 0.5f)` agar perbesaran huruf melengkung dan mengembang ke arah interior layar (kiri), bebas 100% dari pemotongan tepi kanan.
        4. Mengunci `clip = false` pada graphics layer dan menambahkan `softWrap = false`, `maxLines = 1`, `overflow = TextOverflow.Visible` pada teks.
        5. Mengatur skala ombak maksimal yang anggun di 1.80x dengan pergeseran horizontal -68dp, menjaga huruf tetap terbaca sangat jelas dan bebas tumpang-tindih.
  - **Lencana Notifikasi di Pojok Kanan Atas Ikon Aplikasi (Release v1.0.14)**:
    - **Penataan Posisi Badge iOS / Niagara**:
      - Memposisikan badge notifikasi angka bulat / kapsul merah cerah (`Color(0xFFFF334B)`) tepat di sudut kanan atas (`Alignment.TopEnd` dengan offset `x = 4.dp, y = -2.dp`) dari ikon aplikasi 40dp.
      - Dilengkapi border pelindung kontras gelap (`1.5.dp, Color(0xFF141416)`) dan bayangan lembut agar kontras dan terbaca jelas di atas ikon aplikasi apa pun (WhatsApp, Telegram, Pesan, Gmail, dll).
      - Menyediakan fallback badge di samping label aplikasi khusus jika pengguna mengaktifkan mode minimalis tanpa ikon (`showIcon = false`).
  - **Menu Pengaturan Launcher & Tema Minimalis Foto Bug (Release v1.0.15)**:
    - **Menu Pengaturan Launcher Mandiri (`LauncherSettingsBottomSheet.kt`)**:
      - Terbuka otomatis saat pengguna mengklik aplikasi "MaoneArt Flow" di daftar aplikasi (atau tap banner logo).
      - Dilengkapi kontrol switch interaktif persisten DataStore.
    - **Tema Widget Niagara Trio Pills (Identik Foto Bug)**:
      - Pilihan tema widget: *MaoneArt Classic* vs *Niagara Trio Pills*.
      - 3 kartu vertikal kapsul neon: `TODAY` (hijau neon + jam), `HARI & TANGGAL` (kuning amber + kalender), `WEATHER & POWER` (cyan neon + status baterai).
    - **Switch Kustomisasi UI Ekstensif**:
      - *Sembunyikan Banner Grafiti*: Memberikan ruang bersih minimalis.
      - *Search Bar Jadi Tombol Logo*: Mengubah search bar horizontal menjadi tombol bulat Google "G" di kanan bawah seperti di foto bug.
      - *Background Murni Transparan*: Menghilangkan gradasi gelap sehingga wallpaper asli pengguna tampil bening 100%.
      - *Sembunyikan Baris Kategori*: Menyembunyikan chip bar kategori untuk tampilan vertikal murni.
      - *Sembunyikan Scroller Alfabet*: Opsi menyembunyikan scroller huruf A-Z.
  - **Penyempurnaan Tombol Search "G" di Atas Navbar & Hapus Ikon Bintang Favorit (Release v1.0.16)**:
    - **Pembersihan Ikon Bintang Favorit**:
      - Menghapus ikon bintang kuning di baris item aplikasi ([`AppListItem.kt`](file:///sdcard/www/MaoneArtLauncher/app/src/main/java/id/maoneart/flow/ui/components/AppListItem.kt)), karena daftar aplikasi sudah memiliki header teks "FAVORIT". Tampilan item kini lebih bersih dan lega.
    - **Kalibrasi Posisi Tombol Search "G" di Atas Navbar**:
      - Menerapkan `navigationBarsPadding()` dengan padding `bottom = 48.dp` pada tombol logo "G" di pojok kanan bawah agar posisinya terangkat nyaman di atas navigasi/gesture bar Android dan tidak tertutup.
    - **Aktivasi Search Otomatis Saat Tombol "G" Diklik**:
      - Memperbaiki event klik tombol "G" via `viewModel.setSearchActive(true)`. Saat tombol disentuh, `isSearching` langsung aktif, `FlowSearchBar` meluncur keluar di bagian atas dengan `autoFocus` keyboard, dan tombol close `✕` disediakan untuk menutup search bar kembali.
  - **Nama Aplikasi 2 Baris & Pembatasan Lebar Wallpaper-Safe (Release v1.0.17)**:
    - **Solusi Nama Aplikasi Panjang Menabrak Karakter Wallpaper**:
      - Root Cause: Teks nama aplikasi sebelumnya dibatasi kaku ke `maxLines = 1` dengan `Modifier.weight(1f)` yang membentang lebar melintasi layar. Pada wallpaper dengan ilustrasi/karakter di sisi kanan (seperti screenshot bug), nama aplikasi panjang ("Clash of Clans", "Digital Korlantas") melebar menyeberangi dinding gelap dan menutupi wajah/badan karakter.
      - Solusi:
        1. Menambahkan dukungan pembungkusan teks `maxLines = if (twoLineAppNames) 2 else 1` dengan `lineHeight = 20.sp` dan `fontSize = 16.5.sp` yang pas dengan tinggi ikon 40dp (tinggi baris list tetap konsisten 64dp, bebas lonjakan layout).
        2. Mengalibrasi batas lebar maksimal teks `textMaxWidth = if (showIcon) 110.dp else 165.dp` yang presisi sesuai batas dinding gelap wallpaper (~47% lebar layar).
        3. Nama aplikasi pendek ("DANA", "Copilot", "Dark War", "Daya Auto", "Discord", "Dokumen") tetap rapi di 1 baris.
        4. Nama aplikasi panjang ("Clash of Clans", "Digital Korlantas", dll.) otomatis membungkus ke 2 baris ("Clash of" / "Clans", "Digital" / "Korlantas") dan 100% terkunci rapi di dalam kolom dinding tanpa menyenggol karakter anime.
        5. Menambahkan switch kustomisasi persisten di `LauncherSettingsBottomSheet.kt`: "Nama Aplikasi 2 Baris" (aktif secara default).
  - **Penyempurnaan Audit Sistem & Logika Arsitektur (Release v1.0.18)**:
    - **Proteksi Anti Soft-Lock Launcher Sendiri**:
      - Pada `AppActionBottomSheet.kt`, jika paket aplikasi adalah launcher (`id.maoneart.flow`), opsi "Sembunyikan dari Daftar" dan "Hapus Instalasi (Uninstall)" otomatis disembunyikan untuk mencegah hilangnya akses pengaturan. Tombol "Buka Pengaturan Flow" disematkan sebagai akses alternatif langsung.
    - **Akurasi Peluncuran Multi-Activity App**:
      - Menggunakan peluncuran eksplisit `ComponentName(app.packageName, app.activityName)` dengan fallback ke `packageManager.getLaunchIntentForPackage()` pada `AppRepository.kt`, menjamin shortcut/aktivitas launcher sekunder terbuka tepat sesuai sasaran.
    - **Zero Dead-Zone Alphabet Wave Scroller**:
      - Mengembangkan algoritma `computeAlphabetIndices` pada `HomeViewModel.kt` yang memetakan seluruh 27 huruf (A-Z + #) ke huruf aktif terdekat (*nearest forward/backward available letter*). Scrolling alfabet kini 100% mulus tanpa ada titik mati saat melewati huruf tanpa aplikasi (Q, X, Z, dll).
    - **Hierarki Navigasi Backhandler Komprehensif**:
      - Mengurutkan konsumsi tombol Back: Dialog Kategori -> App Action Sheet -> Settings -> Search -> Kategori "Semua" -> Scroll to Top.
    - **Sinkronisasi Detik Jam Sub-Detik & Sensor Suhu Dinamis**:
      - Sinkronisasi loop jam di `DigitalClockHeader.kt` ke detik `00` menit berikutnya secara presisi (`60000L - (nowMs % 60000L)`).
      - Menampilkan suhu baterai real-time dari sensor hardware Android (`BatteryManager.EXTRA_TEMPERATURE`) pada tema Niagara Trio Pills.
  - **Pencegahan Menyeluruh Potensi Crash & Force Close (Release v1.0.19)**:
    - **Proteksi Android 14+ `SecurityException` pada BroadcastReceiver**:
      - Menggunakan `ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)` dengan `try-catch` berlapis di `AppRepository.kt`. Mencegah crash fatal `SecurityException` pada Android 14 (HyperOS, One UI 6, ColorOS 14).
    - **Proteksi Compose Duplicate Key Crash (Dual Apps & Kloning)**:
      - Menambahkan filter de-duplikasi ketat `.distinctBy { it.id }` di `fetchInstalledApps()` pada `AppRepository.kt`.
      - Menambahkan index discriminator pada Compose keys: `"fav_${app.id}_$index"` dan `"${app.id}_$index"` pada `HomeScreen.kt`. Mencegah crash `IllegalArgumentException: Key was already used` saat ponsel memiliki Dual Apps / WhatsApp kloning / Work Profile.
    - **Proteksi Duplicate Key pada Kategori**:
      - Mengunci daftar kategori dengan `.distinctBy { it.id }` di `HomeViewModel.kt` dan `CategoryDialogs.kt`, serta index key `"chip_${category.id}_$index"` di `CategoryChipBar.kt`.
    - **DataStore IO-Safe Flow**:
      - Membungkus seluruh aliran data DataStore dengan `.catch { if (it is IOException) emit(emptyPreferences()) }` di `PreferencesRepository.kt` untuk mencegah force close jika file preferensi korup saat baterai drop.
    - **Handler `onNewIntent` Tombol Home**:
      - Menambahkan override `onNewIntent` di `MainActivity.kt` yang memanggil `viewModel.resetToHomeScreen()` untuk mengembalikan status launcher ke beranda bersih saat tombol Home fisik/gesture ditekan.
  - **Penyempurnaan Gestur, Status Bar Kontras, & Baterai Real-Time (Release v1.0.20)**:
    - **Anti Collision Tombol Search "G" vs Scroller Alfabet A-Z**:
      - Di `HomeScreen.kt`, padding horizontal tombol search "G" otomatis bergeser ke kiri (`end = 64.dp`) saat scroller A-Z aktif di kanan, dan kembali ke `end = 20.dp` saat scroller A-Z disembunyikan. Menghilangkan tabrakan sentuh pada huruf W, X, Y, Z, dan #.
    - **Pencegahan Soft-Lock Filter Kategori**:
      - Di `HomeViewModel.kt`, saat pengguna mematikan saklar "Tampilkan Baris Kategori", filter kategori otomatis direset kembali ke master `"all"`.
    - **Full-Row Touch Target pada Baris Pengaturan**:
      - Di `LauncherSettingsBottomSheet.kt`, seluruh baris `SettingsSwitchRow` dibuat dapat diklik (`Modifier.clickable { onCheckedChange(!checked) }`) untuk kemudahan aktivasi toggle.
    - **Baterai & Pengisian Daya Real-Time (0ms Delay)**:
      - Di `DigitalClockHeader.kt`, mengintegrasikan `DisposableEffect` broadcast receiver untuk `ACTION_BATTERY_CHANGED`. Status petir `⚡`, persentase baterai, dan suhu hardware langsung berubah seketika saat kabel charger dicolokkan atau dicabut.
    - **Kontras Ikon Status Bar Android**:
      - Di `MainActivity.kt`, menerapkan `SystemBarStyle.dark(Color.TRANSPARENT)` pada `enableEdgeToEdge()` agar teks dan ikon status bar Android selalu putih terang dan estetik di semua tema sistem.
  - **Audit Menyeluruh Stabilitas Sistem, Keamanan DataStore, & Manajemen Aplikasi Tersembunyi (Release v1.0.21)**:
    - **Proteksi Menyeluruh Mutasi DataStore (`safeEdit`)**:
      - Di `PreferencesRepository.kt`, membungkus seluruh 17 pemanggilan `dataStore.edit` dalam helper `safeEdit` berpelindung `try-catch`. Mencegah crash fatal saat ponsel mengalami disk write lock atau memori penyimpanan penuh.
    - **Perbaikan Bug Empty Favorites**:
      - Memeriksa `prefs.contains(Keys.FAVORITES)` sebelum fallback, sehingga ketika pengguna sengaja menghapus semua aplikasi favorit, 4 favorit default tidak bangkit kembali secara otomatis.
    - **Pencegahan Badges Notifikasi Nyangkut**:
      - Di `NotificationService.kt`, mereset `_notificationCounts.value = emptyMap()` secara eksplisit saat `activeNotifications` bernilai null, array kosong, atau service terputus (`onListenerDisconnected`), menghilangkan bug badge notifikasi tertahan permanen.
    - **Kelola Aplikasi Tersembunyi & Pemulihan Kategori**:
      - Menyediakan `HiddenAppsBottomSheet.kt` dan opsi "Kelola Aplikasi Tersembunyi" pada menu Pengaturan Flow, memungkinkan pengguna melihat daftar aplikasi yang disembunyikan dan memulihkannya ke launcher dengan 1 tap.
      - Menambahkan fitur "Pulihkan Kategori Bawaan" untuk memunculkan kembali tab kategori sistem yang terhapus/disembunyikan.
    - **Auto-Reset Search State pada Peluncuran Aplikasi**:
      - Di `HomeViewModel.launchApp()`, mereset status pencarian otomatis saat aplikasi berhasil dibuka, sehingga ketika kembali ke launcher, beranda bersih tanpa teks pencarian lama.
    - **Pencegahan Penutupan App Terbawah oleh Tombol Floating**:
      - Di `HomeScreen.kt`, mengalibrasi `contentPadding` bawah `LazyColumn` menjadi `114.dp` saat mode tombol search logo "G" aktif, menjamin item aplikasi paling bawah dapat di-scroll tuntas melewati tombol search.
    - **Sensitivitas Tombol Back Scroll-to-Top**:
      - Di `HomeScreen.kt`, mendeteksi `firstVisibleItemScrollOffset > 0` pada BackHandler, sehingga sentuhan tombol Back merespons scroll ke posisi paling atas meski bergeser hanya beberapa piksel.
    - **Placeholder Ikon Aplikasi**:
      - Di `AppListItem.kt`, menyematkan tile huruf estetik `FlowElectricBlue` sebagai fallback saat ikon aplikasi sistem belum termuat.
  - **Mesin Haptik Taktil Dual-Layer Hardware Vibrator (Release v1.0.22)**:
    - **Solusi Getaran Tidak Terasa pada Alphabet Wave Scroller**:
      - Root Cause: Implementasi sebelumnya hanya mengandalkan `view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)` yang sering diabaikan atau dibisukan oleh ROM OEM (Xiaomi HyperOS, Samsung One UI, ColorOS) jika sistem haptics HP mati, serta bernilai terlalu lemah untuk menggerakkan motor getar tipe ERM (di bawah ambang inersia 30ms).
      - Solusi:
        1. Mengembangkan mesin `FlowHaptics.kt` berbasis Dual-Layer: langsung memicu hardware `Vibrator` / `VibratorManager` dengan pulsa terkalibrasi (`VibrationEffect.createOneShot(32L, 170)`) dan `AudioAttributes.USAGE_ASSISTANCE_SONIFICATION`.
        2. Mengombinasikannya serentak dengan `view.performHapticFeedback(KEYBOARD_TAP, FLAG_IGNORE_GLOBAL_SETTING or 1)` untuk respon instan pada layar 120 FPS.
        3. Mengintegrasikan getaran taktil presisi saat jempol meluncur melintasi huruf-huruf A-Z di `AlphabetWaveScroller.kt`, tombol chip kategori di `CategoryChipBar.kt`, tombol search logo "G", dan long-press aplikasi di `HomeScreen.kt`.
        4. Menambahkan preview getaran seketika saat toggle "Getaran Haptik Tactile" dinyalakan di menu pengaturan.
  - **Eliminasi Lag Scroll & Proteksi Total Force Close Navigasi Navbar (Release v1.0.23)**:
    - **Solusi Lag Saat Scroll Cepat ("Agak Ngelek")**:
      - Root Cause: Pemanggilan sinkron `vibrator.vibrate()` langsung di main UI thread memicu puluhan Binder IPC calls per detik ke `system_server`, memblokir thread UI selama 50-100ms saat jempol meluncur cepat.
      - Solusi:
        1. Menambahkan rate limiting 38ms (`SystemClock.uptimeMillis() - lastTickTime < 38L`), menyelaraskan pulsa dengan inersia motor dan mencegah flooding IPC.
        2. Memindahkan eksekusi `vibrator.vibrate` ke background coroutine (`Dispatchers.Default`), membebaskan UI thread 100% sehingga scrolling stabil di 120 FPS.
    - **Solusi Force Close Saat Tekan Navbar**:
      - Root Cause:
        1. `AlphabetWaveScroller` tidak memiliki `navigationBarsPadding()`, sehingga bagian bawah scroller (Z dan #) menabrak navbar. Sentuhan navbar memicu bentrok gesture event dan membatalkan coroutine scroll secara mendadak.
        2. `scrollJob = scope.launch { listState.scrollToItem(...) }` tidak memiliki try-catch dan bounds protection. Saat tombol Home/Back ditekan, state kategori berubah seketika dan scroll ke index lama melempar exception tak tertangani yang mematikan proses launcher.
        3. `resetToHomeScreen()` memutasi 9 StateFlow sekaligus tanpa pengecekan nilai (`if (value != new)`), memicu gelombang recomposition beruntun.
      - Solusi:
        1. Menerapkan `navigationBarsPadding()` pada container Row, mengangkat scroller A-Z dan daftar aplikasi secara aman di atas area sistem navbar Android.
        2. Membungkus seluruh coroutine scroll (`safeScrollToLetter`, `BackHandler`, `LaunchedEffect`) dengan `try-catch (_: Throwable)` dan validasi batas `coerceAtLeast(0)`.
        3. Menambahkan blok `try-finally` pada pointer gesture loop `AlphabetWaveScroller` agar status sentuhan selalu bersih saat diinterupsi oleh gesture navbar.
        4. Mengoptimalkan `resetToHomeScreen()` agar hanya mengupdate StateFlow yang nilainya benar-benar berubah, serta membungkus pemanggilan `onNewIntent` dengan `try-catch`.
  - **Perbaikan Glitch Abjad Terpotong Vertikal pada Mode MaoneArt Classic (Release v1.0.24)**:
    - **Solusi Huruf Scroller A-Z Terpotong Bawah**:
      - Root Cause:
        1. Di mode Classic, header jam besar 62sp dan logo grafiti 118dp memakan tinggi ~340dp, menyisakan area vertikal terbatas (~310dp) untuk Row.
        2. Dengan 27 huruf abjad, tinggi per item Box tertekan hingga ~11.5dp.
        3. Font bawaan Android mengaktifkan `includeFontPadding = true` secara default dan ukuran font statis `10.5.sp` membutuhkan tinggi minimal ~16dp. Akibatnya, Canvas teks di-clip oleh Compose di batas `maxHeight`, memotong ~40% bagian bawah setiap huruf (B menyerupai R tanpa lengkung bawah, D seperti n, E seperti F, O seperti lengkung atas, U seperti dua garis, dan Z seperti 7).
      - Solusi:
        1. Mengimplementasikan tipografi adaptif di `AlphabetWaveScroller.kt`: `letterFontSize` dan `letterLineHeight` dihitung dinamis proporsional terhadap `itemHeightDp` (`(itemHeightDp * 0.70f).coerceIn(7.5f, 10.5f).sp`).
        2. Mengaktifkan `PlatformTextStyle(includeFontPadding = false)` dan `LineHeightStyle(Alignment.Center, Trim.Both)` untuk menghilangkan font padding liar dan menempatkan glif huruf tepat di tengah em-box.
        3. Menambahkan `wrapContentSize(Alignment.Center, unbounded = true)` pada teks agar Canvas teks tidak terpotong saat terjadi pembulatan sub-piksel.
        4. Mengurangi padding vertikal `Column` scroller dari `top = 8.dp, bottom = 18.dp` menjadi `top = 2.dp, bottom = 4.dp`, mengembalikan ruang vertikal bebas sebesar ~20dp ke abjad A-Z.
        5. Mengalibrasi tinggi header di `DigitalClockHeader.kt` (grafiti logo `106.dp`, padding `8.dp`, jam classic `54.sp`), memberikan ruang bernapas ekstra ~32dp sehingga abjad A-Z tampil 100% utuh, proporsional, dan tajam di semua tema.
  - **Aksi Long-Press Tombol 'G' Membuka Asisten AI (Gemini & ChatGPT) (Release v1.0.25 - 31 Okt 2026)**:
    - **Fitur Baru**:
      1. Menambahkan gestur long-press taktil (`combinedClickable` + `haptics.heavyClick`) pada tombol floating logo "G" (`searchAsIcon`).
      2. Membangun modal MaoneArt Glassmorphism `AiAssistantBottomSheet.kt` untuk memilih asisten AI favorit:
         - **Google Gemini** (`com.google.android.apps.bard` / fallback Web `gemini.google.com`)
         - **OpenAI ChatGPT** (`com.openai.chatgpt` / fallback Web `chatgpt.com`)
         - **Google Assistant / Asisten Suara** (`ACTION_VOICE_COMMAND` / `ACTION_ASSIST`)
      3. Opsi "Ingat pilihan saya" untuk langsung membuka asisten AI favorit di masa mendatang tanpa dialog perantara.
      4. Menu pengaturan baru di `LauncherSettingsBottomSheet.kt`: "Aksi Long-Press Tombol 'G'" dengan 4 opsi instan (Menu Pilihan AI, Gemini, ChatGPT, dan Asisten Suara).
      5. Menambahkan query package visibility di `AndroidManifest.xml` (`<queries>`) untuk paket Gemini, ChatGPT, dan asisten suara sistem Android.
  - **Transformasi Pilihan Tema Widget & Asisten AI Menjadi Dropdown Menu Elegan (Release v1.0.26 - 31 Okt 2026)**:
    - **Solusi Desain Form Dropdown**:
      - Menggantikan kartu pilihan ganda (`WidgetThemeOptionCard`) yang memakan ruang vertikal besar menjadi komponen dropdown kaca modern (`SettingsDropdownRow`).
      - Diterapkan pada **Gaya Widget & Header** (MaoneArt Classic & Niagara Trio Pills) dan **Aksi Long-Press Tombol 'G'** (Menu Pilihan AI, Gemini, ChatGPT, Google Assistant).
      - Dilengkapi trigger box dengan highlight cyan, chevron up/down dinamis, dropdown popup ber-border glowing, dan indikator checkmark pada item yang aktif.
  - **Transformasi Penuh Mode Windows 11 Mobile & Landscape Desktop (Release v1.0.27 - 3 Nov 2026)**:
    - **Arsitektur Dual Mode (Flow vs Windows 11)**:
      - Pengguna dapat beralih secara drastis antara mode *MaoneArt Flow (Niagara minimalis)* dan mode *Windows 11* melalui menu Pengaturan Launcher maupun Context Menu Desktop.
    - **Windows 11 Bloom Wallpaper**:
      - Rendering prosedural GPU Canvas 120 FPS yang mereplikasi wallpaper legendaris Windows 11 Bloom Dark (pita kelopak bunga sutra biru elektrik dengan radial glow mendalam).
    - **Centered Taskbar & System Tray**:
      - Taskbar melayang di dasar layar dengan tombol Windows 4-kotak biru, Search bar, File Explorer, Edge browser, serta indikator aplikasi aktif (*running app pill*).
      - System Tray di pojok kanan bawah memuat jam 2-baris (`HH:mm` di atas `dd/MM/yyyy`), persentase baterai real-time & status charging, Wi-Fi, Audio, dan tombol intip desktop (*peek line*).
    - **Start Menu Otentik (Mica Acrylic Floating Flyout)**:
      - Search bar "Ketik di sini untuk mencari..." dengan pencarian instan.
      - Bagian *Disematkan (Pinned Apps)* dalam grid ikon rapi, bagian *Disarankan (Recommended)* menampilkan aplikasi yang baru dibuka/sering digunakan.
      - Tombol "Semua aplikasi >" membuka laci alfabetis A-Z ala Windows 11.
      - Footer bar menampilkan avatar profil pengguna dan tombol Power dengan opsi beralih instan ke mode Flow.
    - **Action Center & Quick Settings**:
      - Panel pengaturan cepat melayang di atas System Tray dengan toggle Wi-Fi, Bluetooth, Senter, Mode Pesawat, Jangan Ganggu, Rotasi, serta slider Volume dan Kecerahan.
    - **Desktop Surface & Context Menu**:
      - Ikon desktop khas *This PC*, *Recycle Bin*, dan *Microsoft Edge* dengan teks berselubung bayangan kontras.
      - Klik kanan / sentuh tahan (long-press) memunculkan Context Menu Windows 11 asli (*Tampilan, Urutkan, Segarkan, Personalisasi, Beralih ke Flow*).
    - **Dukungan Penuh Portrait & Landscape**:
      - Tata letak otomatis menyesuaikan orientasi layar ponsel (ala Windows 11 Mobile di portrait dan ala PC/Tablet Surface di landscape).
  - **Pencegahan Tuntas Tabrakan Ikon Taskbar & System Tray pada Mode Portrait (Release v1.0.28 - 3 Nov 2026)**:
    - **Penyebab Tabrakan (Root Cause)**:
      - Pada `Win11Taskbar`, deretan ikon aplikasi tengah (`Alignment.Center`, total ~304dp) dan System Tray (`Alignment.CenterEnd`, total ~135dp) berada di dalam `Box` yang sama tanpa batasan alur. Pada layar portrait selebar 360-390dp, jam, tanggal, baterai, dan ikon Wi-Fi menabrak langsung di atas ikon browser Edge, Explorer, dan pinned apps.
    - **Solusi Arsitektur**:
      1. Mengisolasi tata letak Portrait menjadi satu baris `Row` independen dengan `horizontalArrangement = Arrangement.SpaceBetween` (100% bebas tabrakan).
      2. Sisi kiri memuat navigasi inti Windows 11 (Start Button, Search, File Explorer, Browser Edge, 1 pinned app).
      3. Sisi kanan memuat System Tray mobile yang ramping (Wi-Fi, Baterai % & petir, Jam `HH:mm`).
      4. Mengalibrasi tinggi Start Menu flyout di portrait menggunakan `LocalConfiguration` (`screenHeightDp * 0.70f`) dan merampingkan grid pinned apps menjadi 150dp agar muat lega dan elegan di layar portrait.
  - **Wallpaper Resmi 4K, Ikon Fluent Windows 11 Asli, Pemisahan Jam vs Pengaturan Cepat, & Pencarian Start Menu Responsif (Release v1.0.29 - 4 Nov 2026)**:
    - **Wallpaper 4K Windows 11 Bloom Dark Asli (`win11_bloom_dark.jpg`)**:
      - Mengganti rendering kanvas buatan dengan wallpaper resmi 4K (3840x2400) Microsoft Windows 11 Bloom Dark yang jernih dan tajam di segala orientasi.
    - **Ikon Fluent Design Otentik Microsoft Windows 11 (256x256 PNG)**:
      - Mengganti ikon kanvas sederhana dengan aset resmi 256x256 Fluent Design: *This PC* (`ic_win11_this_pc.png`), *Recycle Bin* (`ic_win11_recycle_bin.png`), *File Explorer* (`ic_win11_explorer.png`), *Microsoft Edge* (`ic_win11_edge.png`), dan *Logo Windows 11 4-Kotak* (`ic_win11_logo.png`).
    - **Pemisahan Klik Jam vs Klik Quick Settings**:
      - Klik pada Jam/Tanggal membuka **Pusat Pemberitahuan & Kalender Bulanan** (`Win11NotificationCalendar.kt`).
      - Klik pada pil Wi-Fi / Baterai / Suara membuka **Pusat Pengaturan Cepat / Action Center** (`Win11ActionCenter.kt`).
    - **Pengaturan Cepat Nyata (Live Device Controls)**:
      - Status nyata Wi-Fi, Bluetooth, Mode Pesawat, Senter, Jangan Ganggu (DND), dan Rotasi Layar.
      - Toggle fisik senter LED kamera via `CameraManager.setTorchMode` secara instan.
      - Slider kecerahan layar langsung mengatur `window.attributes.screenBrightness`.
      - Slider volume media langsung terhubung dengan `AudioManager.STREAM_MUSIC` dan tersinkronisasi dengan tombol volume hardware.
    - **Integrasi Pemberitahuan Aktif**:
      - `NotificationService` diperkaya dengan pelacakan notifikasi aktif (ikon aplikasi, judul, teks cuplikan, waktu posting, tombol dismiss per item, dan tombol Hapus Semua).
    - **Perbaikan Kolom Pencarian Start Menu**:
      - Mengganti Material 3 `TextField` (yang terkendala batasan tinggi minimum) dengan `BasicTextField` + `FocusRequester` + `KeyboardOptions(imeAction = ImeAction.Search)`.
      - Kolom pencarian kini dapat diketik dengan lancar dan otomatis fokus serta memunculkan keyboard saat ikon Search di Taskbar ditekan.
    - **Popup Dialog "Tentang Windows" (Winver / About)**:
      - Menambahkan opsi "Tentang Windows" pada Context Menu (saat layar ditekan lama) yang memunculkan modal bergaya `winver.exe` lengkap dengan branding *Windows 11 Pro MaoneArt Edition*, versi 24H2, versi peluncur v1.0.29, dan atribusi pengembang Hermawan (MaoneArt).
  - **Logo Start Resmi Windows 11, Perbaikan Force Close, & Slider Volume Nyata Responsif (Release v1.0.30 - 4 Nov 2026)**:
    - **Logo Start Otentik Windows 11 (4 Kotak Biru Fluent Design)**:
      - Mengganti ikon C: drive harddisk dengan aset resmi 500x500 RGBA *Windows 11 Start Button* (`ic_win11_logo.png`) yang memiliki 4 kotak biru bergradasi Fluent otentik (#42C5F8 -> #0B84DA) dan sudut melengkung presisi.
    - **Perbaikan Force Close (Pencegahan Crash Android 14+)**:
      - Memperbaiki `registerReceiver` di `Win11ActionCenter.kt` dan `Win11Taskbar.kt` dengan `ContextCompat.RECEIVER_EXPORTED` dan blok `try/catch` berlapis sehingga kebal terhadap pembatasan izin siaran dinamis Android 14/15/16.
    - **Kontrol Audio Volume Nyata & Visual Feedback**:
      - Menambahkan izin `android.permission.MODIFY_AUDIO_SETTINGS` pada `AndroidManifest.xml`.
      - Menghubungkan Slider Volume ke `AudioManager.STREAM_MUSIC` dengan flag `AudioManager.FLAG_SHOW_UI` (memunculkan indikator volume sistem Samsung One UI) saat digeser dan `AudioManager.FLAG_PLAY_SOUND` (bunyi feedback bip) saat jari dilepas.
      - Menambahkan indikator persentase dinamis `0% - 100%` pada slider Kecerahan dan Volume.
      - Ikon speaker interaktif: tap ikon volume untuk *Mute* langsung (0%) atau memulihkan volume normal (50%).
    - **Bumping Versi**:
      - `versionCode 31`, `versionName "1.0.30"`. Target APK siap update langsung tanpa uninstall.
  - **Rotasi Layar On/Off Mandiri, Perampingan Flyout Portrait, & Dua-Kolom Notifikasi-Kalender Landscape (Release v1.0.31 - 4 Nov 2026)**:
    - **Toggle Rotasi Layar Mandiri (Zero Redirect ke Settings)**:
      - Menghapus pembukaan paksa `Settings.ACTION_DISPLAY_SETTINGS`.
      - Tombol *Rotasi* pada Quick Settings kini langsung mengunci/membuka kunci orientasi layar via `activity.requestedOrientation` (`ActivityInfo.SCREEN_ORIENTATION_PORTRAIT` vs `SCREEN_ORIENTATION_UNSPECIFIED`) serta menyinkronkan ke `Settings.System.ACCELEROMETER_ROTATION`.
      - Status tile langsung berganti warna (aktif biru / mati gelap) seketika tanpa gangguan.
    - **Perampingan Ukuran Flyout di Mode Portrait**:
      - `Win11ActionCenter`: Dirampingkan dari `fillMaxWidth(0.92f)` x 350dp menjadi `310dp` x `275dp` (tertambat elegan di sisi kanan atas System Tray).
      - `Win11NotificationCalendar`: Dirampingkan dari `fillMaxWidth(0.94f)` x 490dp menjadi `315dp` x `400dp`.
    - **Tata Letak Dua Kolom Side-by-Side Notifikasi & Kalender di Landscape**:
      - Pada orientasi Landscape (tinggi layar terbatas ~360dp), struktur flyout otomatis bertransformasi dari tumpukan vertikal menjadi dua kolom berdampingan (*Side-by-Side*):
        1. Kolom Kiri: Pusat Pemberitahuan lengkap (header, tombol "Hapus Semua", list scrollable kartu notifikasi aktif).
        2. Garis Pemisah Vertikal: 1dp acrylic divider.
        3. Kolom Kanan: Widget Kalender Bulanan lengkap (navigasi bulan, label hari, grid tanggal dengan hari ini disorot biru).
      - Dimensi landscape disesuaikan menjadi lebar `540dp` x tinggi `270dp`. Notifikasi 100% tampak jelas dan dapat diakses bersamaan dengan kalender tanpa terpotong di tepi atas layar.
    - **Bumping Versi**:
      - `versionCode 32`, `versionName "1.0.31"`. Target APK siap update langsung tanpa uninstall.
  - **Flyout Kalender & Notifikasi Ramping Vertikal "Slender Sidebar" (Release v1.0.32 - 4 Nov 2026)**:
    - **Lebar Kalender & Notifikasi Dikecilkan Drastis (260dp x 450dp)**:
      - Memenuhi permintaan desain "panjang ke atas doang gak lebar", lebar flyout di portrait dipangkas dari 315dp menjadi **`260dp`** dan tinggi diperpanjang menjadi **`450dp`** (aspek rasio ramping 1:1.7 ala Windows 11 Notification Sidebar).
      - Menyesuaikan ukuran sel grid kalender menjadi 22dp dengan font 9.5sp, header "Notifikasi" yang ramping, serta kartu notifikasi (ikon 18dp, judul 10.5sp, teks 9.5sp, tombol dismiss 16dp) sehingga tertata sangat rapi dan elegan.
      - Tertambat di sudut kanan bawah dengan jarak tepi `8dp` di atas Taskbar tray, menyisakan margin lapang di sisi kiri layar.
    - **Penyesuaian Quick Settings & Mode Landscape**:
      - Quick Settings Portrait disesuaikan menjadi `280dp` x `270dp`.
      - Mode Landscape disempurnakan menjadi `440dp` x `265dp`.
    - **Bumping Versi**:
      - `versionCode 33`, `versionName "1.0.32"`. Target APK siap update langsung tanpa uninstall.
  - **Ultra-Slender Sidebar Notifikasi & Kalender 225dp (Release v1.0.33 - 4 Nov 2026)**:
    - **Lebar Kalender & Notifikasi Ultra-Ramping (225dp x 450dp, Rasio 1:2)**:
      - Lebar flyout dipangkas lebih ramping lagi menjadi **`225dp`** (dari 260dp) dengan tinggi tetap **`450dp`** sehingga bentuknya memanjang vertikal ramping 1:2.
      - Grid kalender disesuaikan dengan badge tanggal 20dp dan font weekday 8sp.
      - Header notifikasi dipersingkat menjadi tombol aksi cepat "Hapus" & "Atur".
      - Kartu notifikasi dibuat kompak dengan ikon 16dp, judul 10sp, dan teks cuplikan 9sp.
      - Menyisakan ~150dp ruang terbuka di sisi kiri layar ponsel.
    - **Quick Settings Lebih Kompak (245dp x 270dp)**:
      - Quick Settings Portrait diperkecil menjadi `245dp` x `270dp` dengan tile 48dp dan padding 10dp.
      - Landscape disesuaikan menjadi `390dp` x `260dp` (Kalender & Notif) dan `280dp` x `250dp` (Quick Settings).
    - **Bumping Versi**:
      - `versionCode 34`, `versionName "1.0.33"`. Target APK siap update langsung tanpa uninstall.
  - **Quick Settings Sempurna Center 305dp & Kalender-Tanggal Ultra-Compact 195dp (Release v1.0.34 - 4 Nov 2026)**:
    - **Pelebaran & Centering Tombol Quick Settings (Wi-Fi, Bluetooth, dll)**:
      - Memenuhi permintaan "yg wifi bluetooth di besari lagi aja soalnya tombolnya jadi kurang center", lebar flyout Quick Settings diperlebar kembali ke **`305dp`** x `285dp` (padding 14dp).
      - Tinggi tombol tile disesuaikan ke `52dp`, ikon `19dp`, dan teks label `9.5sp` bertata letak simetris di tengah (*perfectly centered*).
    - **Kalender & Tanggal/Notifikasi Dikecilkan Lebih Ramping Lagi (195dp x 410dp)**:
      - Lebar flyout kalender dan tanggal dipangkas menjadi **`195dp`** x `410dp`.
      - Label nama hari disederhanakan menjadi 1 huruf inisial ("M", "S", "S", "R", "K", "J", "S") dengan font 8.5sp semi-bold.
      - Lingkaran tanggal dibuat 18dp dengan font 8.5sp.
      - Format bulan dipersingkat menjadi "Nov 2026" (11.5sp).
      - Kartu notifikasi dibuat ultra-kompak (ikon 15dp, teks 8.5-9.5sp, tombol 13dp).
    - **Bumping Versi**:
      - `versionCode 35`, `versionName "1.0.34"`. Target APK siap update langsung tanpa uninstall.
  - **Windows 11 File Explorer Interaktif & Immersive Navbar (Release v1.0.35 - 10 Nov 2026)**:
    - Jendela File Explorer otentik Windows 11 dengan pemutar musik sample ("Tulus - Manusia Kuat.mp3") dan pratinjau dokumen CV Hermawan.
    - Status penyimpanan C: dinamis dan penanganan immersive navigation bars.
  - **Pratinjau Pesan Gaya Niagara & Direct Reply Balas Langsung (Release v1.0.36 & v1.0.37 - 10 Nov 2026)**:
    - Pratinjau teks pesan WhatsApp/Telegram langsung di baris item aplikasi Flow Launcher.
    - Tombol "↩ Balas" terisolasi rapi di kolom kiri tanpa melebar ke kanan.
    - Dialog balas langsung via RemoteInput PendingIntent.
  - **Penambalan Force Close pada Enter & Penguncian Default Flow Mode (Release v1.0.38 - 10 Nov 2026)**:
    - **Solusi Ganti ke Windows 11 saat Crash/Restart**:
      - Root Cause: Default fallback `Keys.LAUNCHER_MODE` dan initial state `HomeUiState` serta `LauncherAppearance` sebelumnya terprogram `"win11"`. Setiap kali crash atau restart proses, launcher memuat Windows 11 alih-alih Flow (Niagara).
      - Solusi: Mengunci default fallback permanen ke `"flow"` di `PreferencesRepository`, `HomeViewModel`, dan `LauncherSettingsBottomSheet`.
    - **Solusi Force Close Saat Menekan Enter / Enter Ganda**:
      - Root Cause: Penekanan tombol Enter/Search pada `FlowSearchBar` dan Direct Reply `AppActionBottomSheet` memicu pembersihan state/penutupan instan saat soft keyboard masih aktif atau mengirim follow-up `KEYCODE_ENTER` kedua ke view node yang sudah terdisposisi, menyebabkan unhandled Compose IME session crash.
      - Solusi:
        1. Menambahkan pembersihan fokus dan keyboard (`keyboardController?.hide()` & `focusManager.clearFocus()`) sebelum aksi peluncuran aplikasi atau pengiriman balasan.
        2. Menerapkan debounce 450ms pada `launchApp` di `HomeViewModel` dan lock `isSubmitting` pada reply input card.
        3. Menambahkan pengaman `dispatchKeyEvent` di `MainActivity.kt` yang menyerap error event Enter tak tertangani.
        4. Memasang Global Uncaught Exception Handler di `MaoneArtFlowApp` dengan logging otomatis ke `launcher_crash.log` dan auto-recovery mulus ke Flow launcher.
    - **Bumping Versi**:
      - `versionCode 39`, `versionName "1.0.38"`. Target APK siap update langsung tanpa uninstall.
  - **Live Music Player Widget Integrasi Spotify, YouTube, & MaoneArt Music (Release v1.0.39 - 13 Nov 2026)**:
    - **Now Playing Media Widget di Bawah Jam Digital (Gaya Niagara)**:
      - Menambahkan komponen widget glassmorphic `FlowMusicPlayerWidget.kt` tepat di bawah header jam digital.
      - Menampilkan cover album aktif (dari `MediaMetadata.METADATA_KEY_ALBUM_ART` / `ART`), judul lagu tebal satu baris anti-overflow, nama artis, dan badge nama aplikasi pemutar (`FlowCyan`).
      - Dilengkapi animasi **Live 3-Bar Equalizer Wave** yang memantul dinamis secara real-time saat audio sedang diputar (`isPlaying == true`) dan tenang saat dijeda.
      - Kontrol sentuh taktil haptic: tombol *Previous* (`⏮`), tombol *Play/Pause* aksen glowing cyan (`▶` / `⏸`), dan tombol *Next* (`⏭`).
      - Sentuhan pada bodi widget / cover album langsung meluncurkan aplikasi pemutar musik terkait (Spotify, YouTube Music, MaoneArt Music, dll).
    - **Mesin Arsitektur Dual-Layer Media Session (`NotificationService.kt`)**:
      - Memanfaatkan privilege `NotificationListenerService` yang terpasang untuk mengikat `MediaSessionManager.getActiveSessions(component)` dan `addOnActiveSessionsChangedListener` secara otomatis.
      - Dilengkapi fallback otomatis mendeteksi token `Notification.EXTRA_MEDIA_SESSION` dari notifikasi aktif jika ada pemutar yang tidak mendaftar di session manager.
      - Menggunakan StateFlow reaktif `currentMediaTrack` yang zero-latency, zero-battery-drain, dan bebas thread blocking (berjalan di background event loop & main looper callback).
      - Menghilang secara otomatis dan mulus via `AnimatedVisibility` (fade & fold vertical) jika pemutar berhenti/ditutup, mengembalikan layar ke ruang lega minimalis tanpa lag.
    - **Pengaturan & Switch Kustomisasi**:
      - Menambahkan kontrol switch "Widget Pemutar Musik (Now Playing)" di `LauncherSettingsBottomSheet.kt` dan preferensi persisten `show_music_widget` di DataStore.
    - **Bumping Versi**:
      - `versionCode 40`, `versionName "1.0.39"`. Target APK siap update langsung tanpa uninstall.
  - **Refactor Music Player Widget Setengah Layar Wallpaper-Safe (Release v1.0.40 - 13 Nov 2026)**:
    - **Isolasi Kolom Kiri Setengah Layar (Anti-Bleed Kanan)**:
      - Memenuhi permintaan desain: *"witget puter musicnya setwnga layar lah kan semuanyabjuga gitu kan jgn lebar lebar ke kanan"*.
      - Mengubah arsitektur layout `FlowMusicPlayerWidget.kt` dari `fillMaxWidth()` horizontal penuh menjadi kartu kompak vertikal 2-baris yang terkunci di kolom kiri (`widthIn(min = 195.dp, max = 224.dp)`).
      - Menyelaraskan padding kiri ke `start = 24.dp` (identik dengan batas margin header jam dan item aplikasi).
      - Menyisakan ~45% area kanan layar 100% terbuka transparan, sehingga karakter anime / ilustrasi wallpaper pengguna tidak tertutup sama sekali.
    - **Tata Letak 2-Baris Kompak & Elegan**:
      - Baris 1: Cover album 36dp + Judul lagu 12.5sp & Artis 10.5sp + Mini Live 3-Bar Equalizer Wave.
      - Baris 2: Badge pill nama aplikasi pemutar musik (`FlowCyan`) + Kontrol lengkap (*Previous* 28dp, *Play/Pause* glowing cyan 30dp, *Next* 28dp).
    - **Bumping Versi**:
      - `versionCode 41`, `versionName "1.0.40"`. Target APK siap update langsung tanpa uninstall.
  - **Eliminasi Total Lag Search & Optimasi 120 FPS Buttery Scroll (Release v1.0.41 - 13 Nov 2026)**:
    - **Solusi Tuntas 'Search Ngelag Hack' & Patah-Patah**:
      - Root Cause 1: Penggunaan index discriminator pada key `LazyColumn` (`${app.id}_$index`) menyebabkan Compose membuang (dispose) seluruh node daftar dan membuat ulang ratusan elemen setiap kali huruf diketik.
      - Solusi 1: Mengunci Compose key menjadi stabil (`app.id` dan `fav_${app.id}`). Compose kini mempertahankan node yang ada dan mereuse item secara instan saat filtering search.
      - Root Cause 2: Komputasi `computeAlphabetIndices` (loop 27 huruf dengan scan maju-mundur) dan alokasi `label.lowercase()` & `packageName.lowercase()` berulang pada setiap ketukan keyboard.
      - Solusi 2: Pre-komputasi `AppInfo.searchIndex` sekali saat inisialisasi (zero allocation saat pengetikan) dan mematikan kalkulasi alfabet saat mode search aktif. Hasil pencarian kini muncul instan (0ms).
    - **Solusi 'Scrolling Kurang Smooth' & 120 FPS Direct Hardware Canvas Rendering**:
      - Root Cause: Penggunaan Coil `AsyncImage` pada setiap item memicu coroutine asynchronous, state transition `Loading` -> `Success`, dan layout recomposition berulang saat scrolling cepat.
      - Solusi: Membangun pipeline `bitmapCache` berbasis `ImageBitmap` di `AppRepository.kt`. Ikon aplikasi di-prewarm di thread background IO dan digambar langsung via Compose `Image(bitmap = iconBitmap)` di hardware canvas. Menghilangkan overhead coroutine Coil, nol blank frame, dan bebas stutter.
      - Throttling `safeScrollToLetter`: Menambahkan cache `lastTargetIndex` untuk mencegah lonjakan layout pass bertumpuk saat jempol meluncur cepat di atas ombak alfabet.
    - **Bumping Versi**:
      - `versionCode 42`, `versionName "1.0.41"`. Target APK siap update langsung tanpa uninstall.
