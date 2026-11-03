package id.maoneart.flow.ui.win11

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Transform
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.ui.util.rememberFlowHaptics

/**
 * Authentic Windows 11 Right-Click / Long-Press Desktop Context Menu
 */
@Composable
fun Win11ContextMenu(
    onRefresh: () -> Unit,
    onPersonalize: () -> Unit,
    onSwitchToFlow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberFlowHaptics()

    Box(
        modifier = modifier
            .width(220.dp)
            .shadow(16.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xF21E232B)) // Win11 Dark Mica
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ContextMenuItem(
                icon = Icons.Rounded.GridView,
                title = "Tampilan",
                onClick = {
                    haptics.letterTick()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Rounded.Sort,
                title = "Urutkan menurut",
                onClick = {
                    haptics.letterTick()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Rounded.Refresh,
                title = "Segarkan",
                onClick = {
                    haptics.letterTick()
                    onRefresh()
                    onDismiss()
                }
            )

            HorizontalDivider(
                color = Color(0x22FFFFFF),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            ContextMenuItem(
                icon = Icons.Rounded.Transform,
                title = "Beralih ke Flow (Niagara)",
                textColor = Color(0xFF60A5FA),
                onClick = {
                    haptics.heavyClick()
                    onSwitchToFlow()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Rounded.Settings,
                title = "Personalisasi",
                onClick = {
                    haptics.letterTick()
                    onPersonalize()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = Color(0xFFF1F5F9),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 11.5.sp
        )
    }
}
