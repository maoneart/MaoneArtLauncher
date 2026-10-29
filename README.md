# 🌊 MaoneArt Flow — Minimalist Android Launcher

A lightweight, distraction-free Android launcher inspired by the single-hand ergonomics of Niagara Launcher. Built with Kotlin, Jetpack Compose, Material 3, and Coroutines.

## ✨ Features
- **Alphabet Wave Scroller**: Dynamic 3D curved alphabet scroller along the screen edge with elastic wave projection and tactile haptic feedback.
- **Single-Hand Ergonomics**: Vertical app list sorted alphabetically with instant jump navigation.
- **Minimalist Header**: Large digital clock (12/24h), Indonesian date formatting, and live battery status.
- **Favorites Section**: Quick-access hub for your most essential everyday apps.
- **Instant Search**: Real-time app filtering with query clear action.
- **App Management**: Long-press bottom sheet to view app info, toggle favorite, hide apps, or uninstall.
- **Ultra-Smooth 120 FPS**: Zero-lag list scrolling with in-memory app icon caching and tiny RAM footprint (< 35 MB).
- **Permanent Keystore**: Signed with permanent 2054 keystore for continuous APK updates without uninstallation.

## 🛠️ Tech Stack
- **Language**: Kotlin 1.9.24
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Repository Pattern
- **Persistence**: DataStore Preferences
- **Async**: Coroutines & StateFlow
- **Image Loading**: Coil
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
