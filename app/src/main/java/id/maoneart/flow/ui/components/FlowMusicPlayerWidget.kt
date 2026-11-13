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
 * Integrasi kontrol musik Spotify, YouTube Music, Apple Music, & MaoneArt Music langsung di bawah jam.
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
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xE6141926),
                        Color(0xCC182035)
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
                shape = RoundedCornerShape(18.dp)
            )
            .clickable {
                haptics.click(view)
                onOpenApp()
            }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Album Artwork / Fallback Music Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0x3325334D)),
                contentAlignment = Alignment.Center
            ) {
                if (track.artwork != null) {
                    Image(
                        bitmap = track.artwork.asImageBitmap(),
                        contentDescription = "Cover Album",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(46.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Playing indicator dot
                if (track.isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(3.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(FlowGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.width(11.dp))

            // 2. Track Title & Artist Info + Live Equalizer
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = track.title.ifEmpty { "Memutar Musik" },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FlowTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Mini live equalizer wave bars
                    LiveEqualizerWave(
                        isPlaying = track.isPlaying,
                        modifier = Modifier.height(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayArtist = track.artist.ifEmpty { track.appName }
                    Text(
                        text = displayArtist,
                        fontSize = 11.5.sp,
                        color = FlowTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (track.appName.isNotEmpty() && !displayArtist.equals(track.appName, ignoreCase = true)) {
                        Text(
                            text = " • ",
                            fontSize = 10.sp,
                            color = FlowTextSecondary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = track.appName,
                            fontSize = 10.5.sp,
                            color = FlowCyan,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 3. Playback Controls (Prev, Play/Pause, Next)
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
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Sebelumnya",
                        tint = Color(0xCCFFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Button with glowing circular accent
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        modifier = Modifier.size(21.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = {
                        haptics.click(view)
                        onNext()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Berikutnya",
                        tint = Color(0xCCFFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
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
        initialValue = 4f,
        targetValue = if (isPlaying) 13f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 440, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val h2 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = if (isPlaying) 5f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 370, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 12f else 4f,
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
                .width(2.5.dp)
                .height(h1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h3.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(barColor)
        )
    }
}
