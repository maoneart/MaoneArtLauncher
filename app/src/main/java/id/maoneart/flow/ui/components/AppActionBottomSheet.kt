package id.maoneart.flow.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.ui.theme.FlowDarkBackground
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowOrange
import id.maoneart.flow.ui.theme.FlowRed
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActionBottomSheet(
    app: AppInfo,
    iconDrawable: Drawable?,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenInfo: () -> Unit,
    onUninstall: () -> Unit,
    onToggleHidden: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FlowDarkBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header: Icon + App Label + Package
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                if (iconDrawable != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(iconDrawable)
                            .crossfade(true)
                            .build(),
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowTextPrimary
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 12.sp,
                        color = FlowTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Launch
            ActionItem(
                icon = Icons.Rounded.PlayArrow,
                title = "Buka Aplikasi",
                iconTint = FlowElectricBlue,
                onClick = {
                    onDismiss()
                    onLaunch()
                }
            )

            // Action: Favorite Toggle
            ActionItem(
                icon = if (app.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                title = if (app.isFavorite) "Hapus dari Favorit" else "Jadikan Favorit",
                iconTint = FlowOrange,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            // Action: App Info
            ActionItem(
                icon = Icons.Rounded.Info,
                title = "Info Aplikasi",
                iconTint = FlowElectricBlue,
                onClick = {
                    onDismiss()
                    onOpenInfo()
                }
            )

            // Action: Hide
            ActionItem(
                icon = Icons.Rounded.VisibilityOff,
                title = "Sembunyikan dari Daftar",
                iconTint = FlowTextSecondary,
                onClick = {
                    onToggleHidden()
                    onDismiss()
                }
            )

            // Action: Uninstall
            ActionItem(
                icon = Icons.Rounded.Delete,
                title = "Hapus Instalasi (Uninstall)",
                iconTint = FlowRed,
                onClick = {
                    onDismiss()
                    onUninstall()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = FlowTextPrimary
        )
    }
}
