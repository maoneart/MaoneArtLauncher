package id.maoneart.flow.ui.win11

import android.content.Intent
import android.media.MediaPlayer
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Minimize
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.R
import id.maoneart.flow.ui.util.rememberFlowHaptics
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Authentic Windows 11 File Explorer Window with:
 * - Tab bar ("This PC", "Music", "CV & Docs") + Minimize, Maximize, Close controls
 * - Navigation & Breadcrumb Address Bar
 * - Real-time Local Disk (C:) & Storage capacity bars from Android StatFs
 * - Interactive files:
 *   1) 1 Lagu ("Tulus - Manusia Kuat.mp3") with built-in Windows 11 Media Player
 *   2) 1 CV ("CV_Hermawan_MaoneArt.pdf") with built-in Windows 11 Document / CV Viewer
 */
@Composable
fun Win11FileExplorer(
    isLandscape: Boolean,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberFlowHaptics()

    var isMaximized by remember { mutableStateOf(!isLandscape) }
    var currentSection by remember { mutableStateOf("this_pc") } // "this_pc", "music", "documents"
    var isCvViewerOpen by remember { mutableStateOf(false) }

    // Media Player State for built-in Song
    var isPlayerVisible by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(1) }

    val mediaPlayer = remember {
        try {
            MediaPlayer.create(context, R.raw.win11_sample_song)?.apply {
                isLooping = false
                durationMs = duration.coerceAtLeast(1)
                setOnCompletionListener {
                    isPlaying = false
                    currentPosMs = 0
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (_: Throwable) {}
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying && mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying) {
                    currentPosMs = mediaPlayer.currentPosition
                    durationMs = mediaPlayer.duration.coerceAtLeast(1)
                }
            } catch (_: Throwable) {}
            delay(250L)
        }
    }

    fun startOrToggleSong() {
        isPlayerVisible = true
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    isPlaying = false
                } else {
                    mp.start()
                    isPlaying = true
                }
            }
        } catch (_: Throwable) {}
    }

    fun stopSong() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) mp.pause()
                mp.seekTo(0)
            }
        } catch (_: Throwable) {}
        isPlaying = false
        currentPosMs = 0
        isPlayerVisible = false
    }

    // Real Android Device Storage Info for Local Disk (C:)
    val storageStats = remember {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val freeBytes = stat.availableBytes
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val totalGb = totalBytes / (1024f * 1024f * 1024f)
            val freeGb = freeBytes / (1024f * 1024f * 1024f)
            val ratio = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0.5f
            Triple(totalGb, freeGb, ratio)
        } catch (_: Throwable) {
            Triple(128f, 64f, 0.5f)
        }
    }

    val windowMod = if (isMaximized) {
        modifier
            .fillMaxSize()
            .padding(top = 28.dp, bottom = 50.dp)
    } else {
        modifier
            .fillMaxWidth(if (isLandscape) 0.82f else 0.96f)
            .fillMaxHeight(if (isLandscape) 0.80f else 0.78f)
            .shadow(28.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    }

    Box(
        modifier = windowMod
            .background(Color(0xF5191D24)) // Windows 11 Dark Explorer Mica
            .border(
                width = 1.dp,
                color = Color(0x33FFFFFF),
                shape = if (isMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(10.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- 1. WINDOWS 11 TAB & CAPTION TITLE BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(0xFF11141A))
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Active Explorer Tab
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(Color(0xFF1E232C))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Win11Icons.FileExplorerIcon(size = 15.dp)
                        Text(
                            text = when (currentSection) {
                                "music" -> "Music"
                                "documents" -> "Documents (CV)"
                                else -> "This PC"
                            },
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                // Minimize / Maximize / Close Caption Buttons
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(42.dp)
                            .clickable {
                                haptics.letterTick()
                                onMinimize()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Minimize,
                            contentDescription = "Minimize",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Maximize / Restore
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(42.dp)
                            .clickable {
                                haptics.letterTick()
                                isMaximized = !isMaximized
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CropSquare,
                            contentDescription = "Maximize",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Close
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(44.dp)
                            .background(Color(0x00E81123))
                            .clickable {
                                haptics.heavyClick()
                                stopSong()
                                onClose()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFF8FAFC),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // --- 2. ADDRESS BAR & NAVIGATION TOOLBAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(Color(0xFF1E232C))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Nav Arrows
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = if (currentSection != "this_pc" || isCvViewerOpen) Color.White else Color(0xFF64748B),
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            haptics.letterTick()
                            if (isCvViewerOpen) {
                                isCvViewerOpen = false
                            } else {
                                currentSection = "this_pc"
                            }
                        }
                        .padding(4.dp)
                )

                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = "Up",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            haptics.letterTick()
                            isCvViewerOpen = false
                            currentSection = "this_pc"
                        }
                        .padding(4.dp)
                )

                // Breadcrumb Path Box
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF141820))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Win11Icons.ThisPcIcon(size = 14.dp)
                    Text(
                        text = when {
                            isCvViewerOpen -> "This PC  ›  Documents  ›  CV_Hermawan_MaoneArt.pdf"
                            currentSection == "music" -> "This PC  ›  Music"
                            currentSection == "documents" -> "This PC  ›  Documents"
                            else -> "This PC"
                        },
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Open Native Android Storage Button
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF262C38))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(6.dp))
                        .clickable {
                            haptics.letterTick()
                            try {
                                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Throwable) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                                } catch (_: Throwable) {}
                            }
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = "Memori HP",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "File HP",
                            color = Color(0xFFE2E8F0),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0x1FFFFFFF), thickness = 1.dp)

            // --- 3. QUICK NAVIGATION PILLS (Mobile + Desktop Friendly) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF171B22))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExplorerNavChip(
                    title = "This PC",
                    icon = Icons.Rounded.Home,
                    selected = currentSection == "this_pc" && !isCvViewerOpen,
                    onClick = {
                        haptics.letterTick()
                        isCvViewerOpen = false
                        currentSection = "this_pc"
                    }
                )
                ExplorerNavChip(
                    title = "Music (1 Lagu)",
                    icon = Icons.Rounded.LibraryMusic,
                    selected = currentSection == "music" && !isCvViewerOpen,
                    onClick = {
                        haptics.letterTick()
                        isCvViewerOpen = false
                        currentSection = "music"
                    }
                )
                ExplorerNavChip(
                    title = "Documents (1 CV)",
                    icon = Icons.Rounded.Description,
                    selected = currentSection == "documents" || isCvViewerOpen,
                    onClick = {
                        haptics.letterTick()
                        isCvViewerOpen = false
                        currentSection = "documents"
                    }
                )
            }

            HorizontalDivider(color = Color(0x16FFFFFF), thickness = 1.dp)

            // --- 4. MAIN EXPLORER CONTENT AREA ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isCvViewerOpen) {
                    // BUILT-IN WINDOWS 11 CV / PORTFOLIO DOCUMENT VIEWER
                    Win11CvDocumentViewer(
                        onBack = {
                            haptics.letterTick()
                            isCvViewerOpen = false
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // SECTION A: FOLDERS & DEVICES (When in "This PC")
                        if (currentSection == "this_pc") {
                            Text(
                                text = " ▾  Devices and drives (2)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Local Disk (C:)
                                DriveStorageCard(
                                    driveName = "Local Disk (C:)",
                                    subtitle = String.format(
                                        Locale.US,
                                        "%.1f GB free of %.1f GB",
                                        storageStats.second,
                                        storageStats.first
                                    ),
                                    usedRatio = storageStats.third,
                                    accentColor = Color(0xFF38BDF8),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        haptics.letterTick()
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                                        } catch (_: Throwable) {}
                                    }
                                )

                                // MaoneArt Drive (D:)
                                DriveStorageCard(
                                    driveName = "MaoneArt Workspace (D:)",
                                    subtitle = "1 Lagu MP3 • 1 CV PDF Ready",
                                    usedRatio = 0.38f,
                                    accentColor = Color(0xFF4ADE80),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        haptics.letterTick()
                                        currentSection = "documents"
                                    }
                                )
                            }
                        }

                        // SECTION B: PINNED / QUICK ACCESS FILES (1 LAGU & 1 CV)
                        Text(
                            text = when (currentSection) {
                                "music" -> " ▾  Audio & Lagu Tersimpan (1 file)"
                                "documents" -> " ▾  Dokumen & Curriculum Vitae (1 file)"
                                else -> " ▾  File Unggulan di This PC (1 Lagu & 1 CV)"
                            },
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // FILE 1: 1 LAGU (MP3 AUDIO)
                        if (currentSection == "this_pc" || currentSection == "music") {
                            ExplorerFileRowCard(
                                icon = Icons.Rounded.AudioFile,
                                iconBgColor = Color(0xFF7C3AED),
                                title = "Tulus - Manusia Kuat (Official Audio).mp3",
                                subtitle = if (isPlaying) "▶ Sedang Diputar di Windows Media Player • 48 kHz Stereo"
                                else "MP3 Audio File • 5.9 MB • Klik untuk putar lagu langsung",
                                badgeText = if (isPlaying) "PLAYING" else "PUTAR LAGU",
                                badgeColor = if (isPlaying) Color(0xFF22C55E) else Color(0xFF8B5CF6),
                                onClick = {
                                    haptics.letterTick()
                                    startOrToggleSong()
                                }
                            )
                        }

                        // FILE 2: 1 CV (CURRICULUM VITAE & PORTFOLIO PDF)
                        if (currentSection == "this_pc" || currentSection == "documents") {
                            ExplorerFileRowCard(
                                icon = Icons.Rounded.PictureAsPdf,
                                iconBgColor = Color(0xFFEF4444),
                                title = "CV_Hermawan_MaoneArt_2026.pdf",
                                subtitle = "PDF Document • Curriculum Vitae & Portofolio Developer • 1.2 MB",
                                badgeText = "BUKA CV",
                                badgeColor = Color(0xFF0EA5E9),
                                onClick = {
                                    haptics.letterTick()
                                    isCvViewerOpen = true
                                }
                            )
                        }

                        // QUICK FOLDERS IN THIS PC
                        if (currentSection == "this_pc") {
                            Text(
                                text = " ▾  Folders (3)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickFolderTile(
                                    name = "Music",
                                    count = "1 Lagu",
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        haptics.letterTick()
                                        currentSection = "music"
                                    }
                                )
                                QuickFolderTile(
                                    name = "Documents",
                                    count = "1 CV",
                                    color = Color(0xFF3B82F6),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        haptics.letterTick()
                                        currentSection = "documents"
                                    }
                                )
                                QuickFolderTile(
                                    name = "Memori HP",
                                    count = "Semua File",
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        haptics.letterTick()
                                        try {
                                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                                type = "*/*"
                                                addCategory(Intent.CATEGORY_OPENABLE)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Throwable) {}
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- 5. BUILT-IN WINDOWS 11 MINI MEDIA PLAYER BAR (ACTIVE WHEN SONG IS CLICKED) ---
            AnimatedVisibility(
                visible = isPlayerVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF172554), Color(0xFF0F172A))
                            )
                        )
                        .border(1.dp, Color(0x3360A5FA))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF7C3AED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = "Song",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tulus - Manusia Kuat.mp3",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${formatTimeMs(currentPosMs)} / ${formatTimeMs(durationMs)} • Win11 Media Player",
                                    color = Color(0xFF93C5FD),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Playback Controls (-10s, Play/Pause, +10s, Stop)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        haptics.letterTick()
                                        try {
                                            mediaPlayer?.let {
                                                val target = (it.currentPosition - 10000).coerceAtLeast(0)
                                                it.seekTo(target)
                                                currentPosMs = target
                                            }
                                        } catch (_: Throwable) {}
                                    }
                                    .padding(4.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                                    .clickable {
                                        haptics.letterTick()
                                        startOrToggleSong()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.Rounded.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        haptics.letterTick()
                                        try {
                                            mediaPlayer?.let {
                                                val target = (it.currentPosition + 10000).coerceAtMost(durationMs)
                                                it.seekTo(target)
                                                currentPosMs = target
                                            }
                                        } catch (_: Throwable) {}
                                    }
                                    .padding(4.dp)
                            )

                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = "Stop",
                                tint = Color(0xFFF87171),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        haptics.letterTick()
                                        stopSong()
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    // Seek Slider
                    Slider(
                        value = (currentPosMs.toFloat() / durationMs.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
                        onValueChange = { frac ->
                            val target = (frac * durationMs).toInt()
                            currentPosMs = target
                            try {
                                mediaPlayer?.seekTo(target)
                            } catch (_: Throwable) {}
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF60A5FA),
                            activeTrackColor = Color(0xFF3B82F6),
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                    )
                }
            }

            // --- 6. WINDOWS 11 EXPLORER STATUS BAR FOOTER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color(0xFF11141A))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCvViewerOpen) "1 dokumen dibuka (CV Hermawan MaoneArt)" else "2 item utama (1 Lagu MP3, 1 CV PDF)",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
                Text(
                    text = "Windows 11 Explorer • MaoneArt",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ExplorerNavChip(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0xFF0078D7) else Color(0xFF222732))
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF60A5FA) else Color(0x1FFFFFFF),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DriveStorageCard(
    driveName: String,
    subtitle: String,
    usedRatio: Float,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF222731))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Storage,
            contentDescription = driveName,
            tint = accentColor,
            modifier = Modifier.size(28.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = driveName,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { usedRatio },
                color = accentColor,
                trackColor = Color(0xFF0F172A),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 9.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExplorerFileRowCard(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF232834))
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.2f))
                .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeText,
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickFolderTile(
    name: String,
    count: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF202530))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Folder,
            contentDescription = name,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = count,
                color = Color(0xFF94A3B8),
                fontSize = 9.5.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Built-in Windows 11 Document / PDF Viewer displaying Hermawan (MaoneArt) CV
 */
@Composable
private fun Win11CvDocumentViewer(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CV Header Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )
                .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(10.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "HERMAWAN (MAONEART)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF334155))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "← Kembali ke Explorer",
                        color = Color(0xFF93C5FD),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "Full-Stack Web, Android Native (Kotlin/Compose & Flutter) & AI Systems Developer",
                color = Color(0xFF38BDF8),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "🌐 maoneart.my.id  •  💻 github.com/maoneart  •  📍 Bekasi, Indonesia",
                color = Color(0xFFCBD5E1),
                fontSize = 10.5.sp
            )
        }

        // Summary
        CvSectionBox(
            title = "RINGKASAN PROFESIONAL",
            items = listOf(
                "Software Engineer & Creator ekosistem MaoneArt dengan spesialisasi pengembangan aplikasi Android Native berkinerja tinggi (Jetpack Compose, Custom IME, USB OTG Driver), Flutter, serta portal web Laravel.",
                "Pencipta distro kustom Termux-OS V-Edition & integrasi Local AI Gateway (9Router & Antigravity AI CLI)."
            )
        )

        // Core Skills
        CvSectionBox(
            title = "KEAHLIAN TEKNIS UTAMA",
            items = listOf(
                "• Android & Mobile: Kotlin, Jetpack Compose Material 3, Flutter, Custom InputMethodService, Direct USB OTG ESC/P-R.",
                "• Web & Backend: PHP 8.5, Laravel, Node.js, Python, MySQL/MariaDB, Tailwind CSS, Glassmorphic UI Design.",
                "• DevOps & Security: GitHub Actions CI/CD Auto-APK, Linux PRoot/X11, Cloudflare Security Audit, Rclone Cloud."
            )
        )

        // Featured Projects
        CvSectionBox(
            title = "PORTOFOLIO KARYA UNGGULAN (MAONEART ECOSYSTEM)",
            items = listOf(
                "1. MaoneArt Flow Launcher (Niagara & Windows 11 Hybrid Android Launcher).",
                "2. MaoneArt iOS 18 Keyboard (Native Android IME 120 FPS Zero-IPC, Live Translate & Taptic Engine).",
                "3. MaoneArt Music (Flutter Glassmorphic Streaming Player dengan Dynamic Token Engine).",
                "4. MaoneArt Print (Studio Cetak USB OTG Epson ESC/P-R, ESC/P2 & Thermal ESC/POS).",
                "5. Portal Web & Toko Digital Resmi (https://maoneart.my.id) & Sistem Keuangan Terpadu."
            )
        )
    }
}

@Composable
private fun CvSectionBox(
    title: String,
    items: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF202531))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF60A5FA),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        items.forEach { line ->
            Text(
                text = line,
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

private fun formatTimeMs(ms: Int): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", min, sec)
}
