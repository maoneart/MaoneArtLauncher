package id.maoneart.flow.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.ui.theme.FlowCyan
import id.maoneart.flow.ui.theme.FlowDarkBackground
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowGreen
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import id.maoneart.flow.ui.theme.FlowTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantBottomSheet(
    isGeminiInstalled: Boolean,
    isChatGptInstalled: Boolean,
    onSelectGemini: (rememberAsDefault: Boolean) -> Unit,
    onSelectChatGpt: (rememberAsDefault: Boolean) -> Unit,
    onSelectAssistant: (rememberAsDefault: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rememberChoice by remember { mutableStateOf(false) }

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
            // Header with AI gradient glow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Asisten AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowTextPrimary
                    )
                    Text(
                        text = "Pilih asisten cerdas untuk dibuka",
                        fontSize = 12.sp,
                        color = FlowTextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = FlowTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Option 1: Google Gemini
            AiAssistantCard(
                title = "Google Gemini",
                description = "Model AI multimodal cerdas generasi terbaru Google",
                icon = Icons.Rounded.AutoAwesome,
                iconGradient = listOf(Color(0xFF00E5FF), Color(0xFF2979FF)),
                badgeText = if (isGeminiInstalled) "Aplikasi" else "Web Gemini",
                badgeColor = if (isGeminiInstalled) FlowCyan else Color(0x995AC8FA),
                onClick = { onSelectGemini(rememberChoice) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: ChatGPT (OpenAI)
            AiAssistantCard(
                title = "ChatGPT",
                description = "Asisten percakapan cerdas & produktif dari OpenAI",
                icon = Icons.Rounded.Chat,
                iconGradient = listOf(Color(0xFF10A37F), Color(0xFF00E5FF)),
                badgeText = if (isChatGptInstalled) "Aplikasi" else "Web ChatGPT",
                badgeColor = if (isChatGptInstalled) FlowGreen else Color(0x9930D158),
                onClick = { onSelectChatGpt(rememberChoice) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 3: Google Assistant / Voice
            AiAssistantCard(
                title = "Google Assistant",
                description = "Perintah suara cepat & kontrol sistem bawaan Android",
                icon = Icons.Rounded.Mic,
                iconGradient = listOf(Color(0xFF2979FF), Color(0xFF7C4DFF)),
                badgeText = "Sistem HP",
                badgeColor = FlowElectricBlue,
                onClick = { onSelectAssistant(rememberChoice) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Checkbox: Remember choice as default
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { rememberChoice = !rememberChoice }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberChoice,
                    onCheckedChange = { rememberChoice = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = FlowElectricBlue,
                        uncheckedColor = Color(0x66FFFFFF),
                        checkmarkColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Ingat pilihan saya",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FlowTextPrimary
                    )
                    Text(
                        text = "Buka langsung saat tombol 'G' ditekan lama berikutnya (bisa diatur di Pengaturan)",
                        fontSize = 11.sp,
                        color = FlowTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AiAssistantCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconGradient: List<Color>,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AFFFFFF))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(iconGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowTextPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = FlowTextSecondary,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                tint = FlowTextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
