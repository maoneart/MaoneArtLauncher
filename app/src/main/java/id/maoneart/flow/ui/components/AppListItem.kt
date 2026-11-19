package id.maoneart.flow.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowOrange
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import id.maoneart.flow.ui.util.rememberFlowHaptics
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: AppInfo,
    iconBitmap: ImageBitmap? = null,
    iconDrawable: Drawable? = null,
    showIcon: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    twoLineAppNames: Boolean = true,
    swipeGestureEnabled: Boolean = true,
    isGtaCompact: Boolean = false,
    onSwipeRight: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptics = rememberFlowHaptics()

    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "app_swipe_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Background reveal pill when dragging to right (Niagara style)
        if (animatedOffsetX > 3f) {
            val isCustomAction = !app.swipeActionPackage.isNullOrEmpty()
            val accentColor = if (isCustomAction) FlowOrange else FlowElectricBlue
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(accentColor.copy(alpha = 0.22f))
                    .padding(start = 22.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCustomAction) "➔" else "⚡",
                        fontSize = 17.sp,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (animatedOffsetX > 30f) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCustomAction) (app.swipeActionLabel ?: "Aplikasi") else "Pintasan",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Foreground sliding row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .then(
                    if (swipeGestureEnabled && onSwipeRight != null) {
                        Modifier.pointerInput(app.packageName) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    val threshold = 50.dp.toPx()
                                    if (dragOffsetX >= threshold) {
                                        haptics.heavyClick(view)
                                        onSwipeRight()
                                    }
                                    dragOffsetX = 0f
                                },
                                onDragCancel = {
                                    dragOffsetX = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    val newOffset = (dragOffsetX + dragAmount).coerceIn(0f, 95.dp.toPx())
                                    if (newOffset != dragOffsetX) {
                                        dragOffsetX = newOffset
                                        change.consume()
                                    }
                                }
                            )
                        }
                    } else Modifier
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .then(
                    if (isGtaCompact) {
                        Modifier
                            .height(53.dp)
                            .padding(horizontal = 24.dp, vertical = 2.dp)
                    } else {
                        Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
        if (showIcon) {
            val iconBoxSize = if (isGtaCompact) 38.dp else 44.dp
            val iconInnerSize = if (isGtaCompact) 34.dp else 40.dp
            Box(
                modifier = Modifier.size(iconBoxSize)
            ) {
                // App Icon (Direct hardware Canvas rendering for 120 FPS zero-jank scrolling)
                Box(
                    modifier = Modifier
                        .size(iconInnerSize)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(if (isGtaCompact) 8.dp else 10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(iconInnerSize),
                            contentScale = ContentScale.Fit
                        )
                    } else if (iconDrawable != null) {
                        val imageRequest = remember(iconDrawable) {
                            ImageRequest.Builder(context)
                                .data(iconDrawable)
                                .crossfade(false)
                                .build()
                        }
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = app.label,
                            modifier = Modifier.size(iconInnerSize),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(iconInnerSize)
                                .clip(RoundedCornerShape(if (isGtaCompact) 8.dp else 10.dp))
                                .background(Color(0x22FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.firstLetter.toString(),
                                fontSize = if (isGtaCompact) 15.sp else 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlowElectricBlue
                            )
                        }
                    }
                }

                // Notification Badge Counter at Top-Right Corner of App Icon
                if (app.notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                            .shadow(3.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFFFF334B))
                            .border(1.5.dp, Color(0xFF141416), CircleShape)
                            .padding(horizontal = if (app.notificationCount > 9) 5.dp else 4.dp, vertical = 1.dp)
                            .sizeIn(minWidth = 18.dp, minHeight = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (app.notificationCount > 99) "99+" else "${app.notificationCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(if (isGtaCompact) 12.dp else 16.dp))
        }

        // App Label + Niagara-style Inline Notification Preview (Strictly locked to left half so it never bleeds right)
        val hasInlinePreview = app.notificationPreview.isNotBlank()
        val textMaxWidth = if (showIcon) (if (isGtaCompact) 124.dp else 118.dp) else 172.dp

        androidx.compose.foundation.layout.Column(
            modifier = if (twoLineAppNames) {
                Modifier.widthIn(max = textMaxWidth)
            } else {
                Modifier.weight(1f)
            }
        ) {
            Text(
                text = app.label,
                fontSize = if (isGtaCompact) 15.sp else 16.5.sp,
                fontWeight = if (hasInlinePreview) FontWeight.SemiBold else FontWeight.Normal,
                color = FlowTextPrimary,
                maxLines = if (twoLineAppNames && !hasInlinePreview) 2 else 1,
                lineHeight = if (isGtaCompact) 18.sp else 20.sp,
                overflow = TextOverflow.Ellipsis
            )

            if (hasInlinePreview) {
                Text(
                    text = app.notificationPreview,
                    fontSize = if (isGtaCompact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    lineHeight = if (isGtaCompact) 12.sp else 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = if (isGtaCompact) 0.dp else 1.dp)
                )

                if (app.canReplyDirectly) {
                    Box(
                        modifier = Modifier
                            .padding(top = if (isGtaCompact) 1.5.dp else 3.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FlowElectricBlue.copy(alpha = 0.18f))
                            .border(0.8.dp, FlowElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .combinedClickable(
                                onClick = onLongClick,
                                onLongClick = onLongClick
                            )
                            .padding(horizontal = 5.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = "↩ Balas",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlowElectricBlue
                        )
                    }
                }

                // Assigned Swipe Action Badge Indicator (Compact & Wallpaper Safe)
                if (!app.swipeActionPackage.isNullOrEmpty() && app.swipeActionLabel != null) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FlowOrange.copy(alpha = 0.18f))
                            .border(0.6.dp, FlowOrange.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "➔ ${app.swipeActionLabel}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlowOrange,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Spacer to absorb remaining row space if twoLineAppNames is active and there's a fallback badge
        if (twoLineAppNames && !showIcon && app.notificationCount > 0) {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Notification Badge Counter fallback (only when app icon is hidden by user settings)
        if (!showIcon && app.notificationCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFF334B))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (app.notificationCount > 99) "99+" else "${app.notificationCount}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
}
