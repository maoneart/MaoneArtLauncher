package id.maoneart.flow.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowOrange
import id.maoneart.flow.ui.theme.FlowTextPrimary

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
    twoLineAppNames: Boolean = true
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            Box(
                modifier = Modifier.size(44.dp)
            ) {
                // App Icon (Direct hardware Canvas rendering for 120 FPS zero-jank scrolling)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(40.dp),
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
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x22FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.firstLetter.toString(),
                                fontSize = 17.sp,
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
            Spacer(modifier = Modifier.width(16.dp))
        }

        // App Label + Niagara-style Inline Notification Preview (Strictly locked to left half so it never bleeds right)
        val hasInlinePreview = app.notificationPreview.isNotBlank()
        val textMaxWidth = if (showIcon) 118.dp else 172.dp

        androidx.compose.foundation.layout.Column(
            modifier = if (twoLineAppNames) {
                Modifier.widthIn(max = textMaxWidth)
            } else {
                Modifier.weight(1f)
            }
        ) {
            Text(
                text = app.label,
                fontSize = 16.5.sp,
                fontWeight = if (hasInlinePreview) FontWeight.SemiBold else FontWeight.Normal,
                color = FlowTextPrimary,
                maxLines = if (twoLineAppNames && !hasInlinePreview) 2 else 1,
                lineHeight = 20.sp,
                overflow = TextOverflow.Ellipsis
            )

            if (hasInlinePreview) {
                Text(
                    text = app.notificationPreview,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp)
                )

                if (app.canReplyDirectly) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(FlowElectricBlue.copy(alpha = 0.18f))
                            .border(0.8.dp, FlowElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                            .combinedClickable(
                                onClick = onLongClick,
                                onLongClick = onLongClick
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "↩ Balas",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlowElectricBlue
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
