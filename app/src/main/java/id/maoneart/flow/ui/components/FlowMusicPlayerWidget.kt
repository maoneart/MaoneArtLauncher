package id.maoneart.flow.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.service.MediaTrackInfo
import id.maoneart.flow.ui.theme.FlowCyan
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowGreen
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import id.maoneart.flow.ui.util.rememberFlowHaptics

/**
 * 🎵 Live Music Player Widget (MaoneArt Flow / Niagara style)
 * Terkunci di kolom kiri (setengah layar / ~55% lebar layar) agar wallpaper di sisi kanan tidak terhalang.
 */
@Composable
fun FlowMusicPlayerWidget(
    track: MediaTrackInfo,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val haptics = rememberFlowHaptics()

    Box(
        modifier = modifier
            .padding(start = 24.dp, end = 8.dp, top = 2.dp, bottom = 6.dp)
            .widthIn(min = 195.dp, max = 224.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xEA141926),
                        Color(0xD0182035)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x555AC8FA),
                        Color(0x220A84FF)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                haptics.click(view)
                onOpenApp()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row 1: Artwork + Title & Artist + Equalizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Album Artwork / Fallback Music Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0x3325334D)),
                    contentAlignment = Alignment.Center
                ) {
                    if (track.artwork != null) {
                        Image(
                            bitmap = track.artwork.asImageBitmap(),
                            contentDescription = "Cover Album",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            FlowElectricBlue.copy(alpha = 0.5f),
                                            FlowCyan.copy(alpha = 0.35f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = "Musik",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Playing indicator dot
                    if (track.isPlaying) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(FlowGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 2. Track Title & Artist Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track.title.ifEmpty { "Memutar Musik" },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FlowTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val displayArtist = track.artist.ifEmpty { track.appName }
                    Text(
                        text = displayArtist,
                        fontSize = 10.5.sp,
                        color = FlowTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Mini live equalizer wave bars
                LiveEqualizerWave(
                    isPlaying = track.isPlaying,
                    modifier = Modifier
                        .height(13.dp)
                        .padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: App Name Badge + Playback Controls (Prev, Play/Pause, Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x225AC8FA))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = track.appName.ifEmpty { "Musik" },
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = FlowCyan,
                        maxLines = 1
                    )
                }

                // Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Previous Button
                    IconButton(
                        onClick = {
                            haptics.click(view)
                            onPrevious()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Sebelumnya",
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Play / Pause Button with glowing circular accent
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(FlowCyan.copy(alpha = 0.22f))
                            .border(1.dp, FlowCyan.copy(alpha = 0.65f), CircleShape)
                            .clickable {
                                haptics.heavyClick(view)
                                onPlayPause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (track.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (track.isPlaying) "Jeda" else "Putar",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Next Button
                    IconButton(
                        onClick = {
                            haptics.click(view)
                            onNext()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Berikutnya",
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated 3-bar rhythm equalizer that pulses while music is playing
 */
@Composable
private fun LiveEqualizerWave(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = FlowCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = if (isPlaying) 12f else 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 440, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val h2 by infiniteTransition.animateFloat(
        initialValue = 13f,
        targetValue = if (isPlaying) 4f else 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 370, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val h3 by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = if (isPlaying) 11f else 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 510, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(2.2.dp)
                .height(h1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(2.2.dp)
                .height(h2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(2.2.dp)
                .height(h3.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
    }
}
