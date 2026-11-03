package id.maoneart.flow.ui.win11

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Authentic Windows 11 Bloom Dark Wallpaper procedural rendering
 * High performance hardware accelerated GPU canvas that scales perfectly
 * to both Portrait and Landscape orientations without distortion.
 */
@Composable
fun Win11BloomWallpaper(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Deep space dark navy base gradient
        val bgBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF090D14),
                Color(0xFF0F1726),
                Color(0xFF080C14)
            ),
            startY = 0f,
            endY = h
        )
        drawRect(brush = bgBrush)

        // Center coordinates of the bloom
        val cx = w * 0.5f
        val cy = h * 0.52f
        val bloomRadius = minOf(w, h) * 0.55f

        // 2. Central ambient bloom glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x660078D7),
                    Color(0x33005A9E),
                    Color(0x00000000)
                ),
                center = Offset(cx, cy),
                radius = bloomRadius * 1.3f
            ),
            radius = bloomRadius * 1.3f,
            center = Offset(cx, cy)
        )

        // 3. Left deep fold petal
        val leftFold = Path().apply {
            moveTo(cx - bloomRadius * 0.85f, cy + bloomRadius * 0.35f)
            cubicTo(
                cx - bloomRadius * 0.95f, cy - bloomRadius * 0.40f,
                cx - bloomRadius * 0.20f, cy - bloomRadius * 0.85f,
                cx - bloomRadius * 0.05f, cy - bloomRadius * 0.30f
            )
            cubicTo(
                cx - bloomRadius * 0.15f, cy + bloomRadius * 0.10f,
                cx - bloomRadius * 0.45f, cy + bloomRadius * 0.60f,
                cx - bloomRadius * 0.85f, cy + bloomRadius * 0.35f
            )
            close()
        }
        drawPath(
            path = leftFold,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF003D75), Color(0xFF0066B8), Color(0xFF001F3F)),
                start = Offset(cx - bloomRadius, cy - bloomRadius * 0.5f),
                end = Offset(cx, cy + bloomRadius * 0.5f)
            )
        )

        // 4. Right expansive petal
        val rightFold = Path().apply {
            moveTo(cx + bloomRadius * 0.85f, cy + bloomRadius * 0.30f)
            cubicTo(
                cx + bloomRadius * 0.90f, cy - bloomRadius * 0.45f,
                cx + bloomRadius * 0.25f, cy - bloomRadius * 0.80f,
                cx + bloomRadius * 0.05f, cy - bloomRadius * 0.20f
            )
            cubicTo(
                cx + bloomRadius * 0.10f, cy + bloomRadius * 0.25f,
                cx + bloomRadius * 0.40f, cy + bloomRadius * 0.65f,
                cx + bloomRadius * 0.85f, cy + bloomRadius * 0.30f
            )
            close()
        }
        drawPath(
            path = rightFold,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0078D7), Color(0xFF004578), Color(0xFF001E3D)),
                start = Offset(cx + bloomRadius, cy - bloomRadius * 0.5f),
                end = Offset(cx, cy + bloomRadius * 0.5f)
            )
        )

        // 5. Central glowing ribbon petal (The signature vibrant cyan Windows 11 fold)
        val centerRibbon = Path().apply {
            moveTo(cx - bloomRadius * 0.35f, cy + bloomRadius * 0.55f)
            cubicTo(
                cx - bloomRadius * 0.45f, cy - bloomRadius * 0.20f,
                cx - bloomRadius * 0.10f, cy - bloomRadius * 0.75f,
                cx, cy - bloomRadius * 0.55f
            )
            cubicTo(
                cx + bloomRadius * 0.25f, cy - bloomRadius * 0.85f,
                cx + bloomRadius * 0.45f, cy - bloomRadius * 0.15f,
                cx + bloomRadius * 0.35f, cy + bloomRadius * 0.55f
            )
            cubicTo(
                cx + bloomRadius * 0.15f, cy + bloomRadius * 0.25f,
                cx - bloomRadius * 0.15f, cy + bloomRadius * 0.25f,
                cx - bloomRadius * 0.35f, cy + bloomRadius * 0.55f
            )
            close()
        }
        drawPath(
            path = centerRibbon,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF50E6FF),
                    Color(0xFF00ADEF),
                    Color(0xFF0078D7),
                    Color(0xFF004578)
                ),
                center = Offset(cx, cy - bloomRadius * 0.15f),
                radius = bloomRadius * 0.70f
            )
        )

        // 6. Highlight sheen on ribbon fold
        val highlightSheen = Path().apply {
            moveTo(cx - bloomRadius * 0.18f, cy - bloomRadius * 0.50f)
            cubicTo(
                cx - bloomRadius * 0.05f, cy - bloomRadius * 0.65f,
                cx + bloomRadius * 0.12f, cy - bloomRadius * 0.60f,
                cx + bloomRadius * 0.22f, cy - bloomRadius * 0.40f
            )
        }
        drawPath(
            path = highlightSheen,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xCCBAE6FD), Color(0x00BAE6FD))
            ),
            style = Stroke(width = bloomRadius * 0.035f)
        )

        // 7. Bottom shadow depth
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x99000000), Color(0x00000000)),
                center = Offset(cx, cy + bloomRadius * 0.5f),
                radius = bloomRadius * 0.5f
            ),
            radius = bloomRadius * 0.5f,
            center = Offset(cx, cy + bloomRadius * 0.5f)
        )
    }
}
