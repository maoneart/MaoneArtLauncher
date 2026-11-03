package id.maoneart.flow.ui.win11

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Authentic Windows 11 UI Vector Icons drawn with high performance Compose Canvas
 */
object Win11Icons {

    val Win11BlueStart = Color(0xFF00ADEF)
    val Win11BlueEnd = Color(0xFF0078D7)
    val Win11DarkBlue = Color(0xFF005A9E)
    val Win11Accent = Color(0xFF4CC2FF)

    /**
     * Iconic 4-Square Windows 11 Logo with soft rounded corners and subtle gap
     */
    @Composable
    fun WindowsLogo(
        modifier: Modifier = Modifier,
        size: Dp = 24.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val totalSize = this.size.minDimension
            val gap = totalSize * 0.08f
            val tileSize = (totalSize - gap) / 2f
            val cornerRadius = CornerRadius(tileSize * 0.18f, tileSize * 0.18f)

            val gradientBrush = Brush.linearGradient(
                colors = listOf(Win11BlueStart, Win11BlueEnd),
                start = Offset(0f, 0f),
                end = Offset(totalSize, totalSize)
            )

            // Top-Left tile
            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(0f, 0f),
                size = Size(tileSize, tileSize),
                cornerRadius = cornerRadius
            )

            // Top-Right tile
            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(tileSize + gap, 0f),
                size = Size(tileSize, tileSize),
                cornerRadius = cornerRadius
            )

            // Bottom-Left tile
            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(0f, tileSize + gap),
                size = Size(tileSize, tileSize),
                cornerRadius = cornerRadius
            )

            // Bottom-Right tile
            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(tileSize + gap, tileSize + gap),
                size = Size(tileSize, tileSize),
                cornerRadius = cornerRadius
            )
        }
    }

    /**
     * Iconic Windows 11 "This PC" / Computer Desktop Icon
     */
    @Composable
    fun ThisPcIcon(
        modifier: Modifier = Modifier,
        size: Dp = 44.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Monitor body
            val monitorWidth = w * 0.88f
            val monitorHeight = h * 0.62f
            val monitorLeft = (w - monitorWidth) / 2f
            val monitorTop = h * 0.10f

            // Screen outer frame
            drawRoundRect(
                color = Color(0xFF2C323D),
                topLeft = Offset(monitorLeft, monitorTop),
                size = Size(monitorWidth, monitorHeight),
                cornerRadius = CornerRadius(w * 0.08f)
            )

            // Screen display (Windows blue glow)
            val screenMargin = w * 0.05f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0078D7), Color(0xFF002B49))
                ),
                topLeft = Offset(monitorLeft + screenMargin, monitorTop + screenMargin),
                size = Size(monitorWidth - screenMargin * 2, monitorHeight - screenMargin * 2),
                cornerRadius = CornerRadius(w * 0.04f)
            )

            // Mini Windows logo inside screen
            val miniTile = w * 0.06f
            val miniGap = w * 0.015f
            val cx = w / 2f
            val cy = monitorTop + monitorHeight / 2f
            val miniBrush = Brush.linearGradient(listOf(Color(0xFF5CD5FF), Color(0xFF0078D7)))

            drawRect(miniBrush, Offset(cx - miniTile - miniGap / 2, cy - miniTile - miniGap / 2), Size(miniTile, miniTile))
            drawRect(miniBrush, Offset(cx + miniGap / 2, cy - miniTile - miniGap / 2), Size(miniTile, miniTile))
            drawRect(miniBrush, Offset(cx - miniTile - miniGap / 2, cy + miniGap / 2), Size(miniTile, miniTile))
            drawRect(miniBrush, Offset(cx + miniGap / 2, cy + miniGap / 2), Size(miniTile, miniTile))

            // Stand neck
            val standWidth = w * 0.16f
            val standHeight = h * 0.16f
            drawRect(
                color = Color(0xFF4A5568),
                topLeft = Offset((w - standWidth) / 2f, monitorTop + monitorHeight - 2f),
                size = Size(standWidth, standHeight)
            )

            // Stand base
            val baseWidth = w * 0.44f
            val baseHeight = h * 0.06f
            drawRoundRect(
                color = Color(0xFF718096),
                topLeft = Offset((w - baseWidth) / 2f, monitorTop + monitorHeight + standHeight - 4f),
                size = Size(baseWidth, baseHeight),
                cornerRadius = CornerRadius(baseHeight / 2)
            )
        }
    }

    /**
     * Windows 11 Recycle Bin Icon
     */
    @Composable
    fun RecycleBinIcon(
        modifier: Modifier = Modifier,
        size: Dp = 44.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Bin Body Path (Trapezoid)
            val binPath = Path().apply {
                moveTo(w * 0.22f, h * 0.28f)
                lineTo(w * 0.78f, h * 0.28f)
                lineTo(w * 0.72f, h * 0.88f)
                lineTo(w * 0.28f, h * 0.88f)
                close()
            }

            // Glass/acrylic translucent body
            drawPath(
                path = binPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xD0E2E8F0), Color(0x90A0AEC0))
                )
            )
            drawPath(
                path = binPath,
                color = Color(0xFFCBD5E0),
                style = Stroke(width = w * 0.035f)
            )

            // Bin Rim
            drawRoundRect(
                color = Color(0xFFCBD5E0),
                topLeft = Offset(w * 0.18f, h * 0.22f),
                size = Size(w * 0.64f, h * 0.08f),
                cornerRadius = CornerRadius(h * 0.04f)
            )

            // Recycling 3-Rib lines in center
            val ribColor = Color(0xAA4A5568)
            val ribWidth = w * 0.03f
            drawLine(ribColor, Offset(w * 0.38f, h * 0.42f), Offset(w * 0.40f, h * 0.76f), strokeWidth = ribWidth)
            drawLine(ribColor, Offset(w * 0.50f, h * 0.42f), Offset(w * 0.50f, h * 0.76f), strokeWidth = ribWidth)
            drawLine(ribColor, Offset(w * 0.62f, h * 0.42f), Offset(w * 0.60f, h * 0.76f), strokeWidth = ribWidth)
        }
    }

    /**
     * Windows 11 File Explorer Icon (Manila/Yellow Folder with blue accent)
     */
    @Composable
    fun FileExplorerIcon(
        modifier: Modifier = Modifier,
        size: Dp = 32.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Back tab
            val tabPath = Path().apply {
                moveTo(w * 0.12f, h * 0.30f)
                lineTo(w * 0.44f, h * 0.30f)
                lineTo(w * 0.52f, h * 0.42f)
                lineTo(w * 0.88f, h * 0.42f)
                lineTo(w * 0.88f, h * 0.82f)
                lineTo(w * 0.12f, h * 0.82f)
                close()
            }
            drawPath(
                path = tabPath,
                color = Color(0xFF0078D7) // Blue back binder
            )

            // Front Yellow/Cyan Folder
            val frontFolder = Path().apply {
                moveTo(w * 0.10f, h * 0.42f)
                lineTo(w * 0.90f, h * 0.42f)
                lineTo(w * 0.85f, h * 0.88f)
                lineTo(w * 0.15f, h * 0.88f)
                close()
            }
            drawPath(
                path = frontFolder,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD43B), Color(0xFFF59F00))
                )
            )

            // Folder handle clip
            drawRoundRect(
                color = Color(0xFF00ADEF),
                topLeft = Offset(w * 0.34f, h * 0.20f),
                size = Size(w * 0.32f, h * 0.12f),
                cornerRadius = CornerRadius(h * 0.05f)
            )
        }
    }

    /**
     * Windows 11 Microsoft Edge Swirl Wave Icon
     */
    @Composable
    fun EdgeBrowserIcon(
        modifier: Modifier = Modifier,
        size: Dp = 32.dp
    ) {
        Canvas(modifier = modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outer wave gradient (Cyan -> Blue -> Green)
            val brush = Brush.sweepGradient(
                listOf(
                    Color(0xFF0078D7),
                    Color(0xFF00C7FF),
                    Color(0xFF26DE81),
                    Color(0xFF0078D7)
                ),
                center = Offset(w * 0.5f, h * 0.5f)
            )

            drawCircle(
                brush = brush,
                radius = w * 0.42f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            // Inner cutout swirl
            drawCircle(
                color = Color(0xFF0E1A2C),
                radius = w * 0.24f,
                center = Offset(w * 0.54f, h * 0.46f)
            )

            // Wave crest
            drawCircle(
                color = Color(0xFF50E6FF),
                radius = w * 0.16f,
                center = Offset(w * 0.62f, h * 0.38f)
            )
        }
    }
}
