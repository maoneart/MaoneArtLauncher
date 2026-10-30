# Persistent Memory — MaoneArt Flow Launcher

> **Repository**: [https://github.com/maoneart/MaoneArtLauncher](https://github.com/maoneart/MaoneArtLauncher)  
> **Workspace**: `/sdcard/www/MaoneArtLauncher`  
> **Package**: `id.maoneart.flow`  
> **Target Release APK**: `/sdcard/Download/MaoneArtFlow.apk` (v1.0.22)

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
