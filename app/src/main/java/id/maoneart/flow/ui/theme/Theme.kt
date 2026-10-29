package id.maoneart.flow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FlowElectricBlue,
    secondary = FlowCyan,
    tertiary = FlowGreen,
    background = Color.Transparent,
    surface = FlowDarkSurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = FlowTextPrimary,
    onSurface = FlowTextPrimary,
    surfaceVariant = FlowDarkSurface,
    onSurfaceVariant = FlowTextSecondary
)

@Composable
fun MaoneArtFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
