package id.maoneart.flow.ui.win11

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.maoneart.flow.ui.util.rememberFlowHaptics

/**
 * Authentic Windows 11 "winver" About Dialog
 * Displays Windows 11 edition, version, build, and MaoneArt developer attribution.
 */
@Composable
fun Win11AboutDialog(
    onDismiss: () -> Unit
) {
    val haptics = rememberFlowHaptics()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .shadow(24.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xF21C222E)) // Windows 11 Mica Acrylic
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Win11Icons.WindowsLogo(size = 16.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tentang Windows",
                            color = Color(0xFFF1F5F9),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            haptics.letterTick()
                            onDismiss()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Windows 11 Branding Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Win11Icons.WindowsLogo(size = 46.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Windows 11 Pro",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MaoneArt Edition",
                            color = Color(0xFF4CC2FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // System & Developer Information
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoRow(label = "Versi", value = "24H2 (OS Build 26100.1)")
                    InfoRow(label = "Peluncur", value = "MaoneArt Flow Launcher v1.0.34")
                    InfoRow(label = "Pengembang", value = "Hermawan (MaoneArt)")
                    InfoRow(label = "Mesin", value = "Android (Niagara & Win11 Hybrid Engine)")
                    InfoRow(label = "Perangkat", value = android.os.Build.MODEL)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Copyright notice
                Text(
                    text = "Hak Cipta © 2026 MaoneArt. Seluruh hak cipta dilindungi undang-undang.",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // OK Action Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = {
                            haptics.letterTick()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0078D7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .width(90.dp)
                            .height(34.dp)
                    ) {
                        Text(
                            text = "OK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = Color(0xFFE2E8F0),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
