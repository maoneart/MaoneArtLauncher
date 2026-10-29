package id.maoneart.flow.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.ui.theme.FlowCyan
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun AlphabetWaveScroller(
    availableLetters: Set<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
    isRightSide: Boolean = true,
    hapticsEnabled: Boolean = true
) {
    val alphabet = remember { ('A'..'Z').toList() + listOf('#') }
    val view = LocalView.current
    val density = LocalDensity.current

    var isTouching by remember { mutableStateOf(false) }
    var touchY by remember { mutableFloatStateOf(0f) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }

    var containerHeight by remember { mutableFloatStateOf(0f) }
    var columnTopY by remember { mutableFloatStateOf(0f) }
    var columnHeight by remember { mutableFloatStateOf(0f) }

    // Elastic spring physics for wave expansion & collapse (Niagara rubber fluid feel)
    val waveIntensity by animateFloatAsState(
        targetValue = if (isTouching) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.50f, // Bouncy elastic wave expansion & bounce back
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "wave_spring"
    )

    // Smooth liquid interpolation for tracking finger position at 120 FPS
    val animatedTouchY by animateFloatAsState(
        targetValue = touchY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "touch_tracker"
    )

    fun handleTouchAt(y: Float) {
        if (columnHeight <= 0f) return
        val clampedY = y.coerceIn(0f, containerHeight)
        touchY = clampedY

        val relativeY = (clampedY - columnTopY).coerceIn(0f, columnHeight)
        val itemHeight = columnHeight / alphabet.size
        val index = (relativeY / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
        val letter = alphabet[index]

        if (letter != activeLetter) {
            activeLetter = letter
            if (hapticsEnabled) {
                view.performHapticFeedback(
                    HapticFeedbackConstants.CLOCK_TICK,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
            onLetterSelected(letter)
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(62.dp)
            .onGloballyPositioned { coordinates ->
                containerHeight = coordinates.size.height.toFloat()
            }
            // Low-level pointer loop: tracks every tap AND every drag movement without dropping
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isTouching = true
                    handleTouchAt(down.position.y)

                    // Track drag / scroll continuously across the entire scroller
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointer = event.changes.firstOrNull() ?: break
                        if (!pointer.pressed) {
                            break
                        }
                        pointer.consume()
                        handleTouchAt(pointer.position.y)
                    }

                    isTouching = false
                    activeLetter = null
                }
            },
        contentAlignment = if (isRightSide) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        // Floating Active Letter Balloon / Bubble at the apex of the wave
        AnimatedVisibility(
            visible = isTouching && activeLetter != null,
            enter = fadeIn() + scaleIn(spring(dampingRatio = 0.55f)),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(if (isRightSide) Alignment.TopEnd else Alignment.TopStart)
                .offset {
                    val offsetY = (animatedTouchY - with(density) { 33.dp.toPx() }).roundToInt()
                    // Position balloon comfortably in front of the wave crest
                    val offsetX = if (isRightSide) with(density) { (-124).dp.roundToPx() } else with(density) { 124.dp.roundToPx() }
                    IntOffset(offsetX, offsetY.coerceIn(0, (containerHeight - 120).toInt().coerceAtLeast(0)))
                }
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .shadow(22.dp, CircleShape, spotColor = FlowElectricBlue)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(FlowCyan, FlowElectricBlue)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter?.toString() ?: "",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Vertical Alphabet List with Authentic Niagara Parabolic Wave Curve Projection
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp)
                .onGloballyPositioned { coordinates ->
                    columnHeight = coordinates.size.height.toFloat()
                    columnTopY = coordinates.positionInParent().y
                },
            horizontalAlignment = if (isRightSide) Alignment.End else Alignment.Start
        ) {
            // Authentic Niagara wave influence radius: ~200dp (~9-11 letters influenced simultaneously)
            val waveRadiusPx = with(density) { 200.dp.toPx() }
            val maxDisplacementPx = with(density) { (if (isRightSide) -88.dp else 88.dp).toPx() }
            val itemHeight = if (columnHeight > 0f) columnHeight / alphabet.size else 22f

            alphabet.forEachIndexed { i, letter ->
                val letterCenterY = columnTopY + (i + 0.5f) * itemHeight
                val hasApps = availableLetters.contains(letter)

                // 120 FPS GPU-accelerated graphicsLayer: live wave dynamics follow finger continuously!
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(44.dp)
                        .graphicsLayer {
                            if (waveIntensity > 0.005f) {
                                val dist = abs(animatedTouchY - letterCenterY)
                                if (dist < waveRadiusPx) {
                                    val normalized = dist / waveRadiusPx
                                    // Cosine bell curve with power shaping for dramatic, fluid wave crest
                                    val rawFactor = ((1f + cos(normalized * PI.toFloat())) / 2f)
                                    val waveFactor = rawFactor.toDouble().pow(1.35).toFloat() * waveIntensity

                                    val scale = 1.0f + (1.45f * waveFactor) // Expands up to 2.45x
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = maxDisplacementPx * waveFactor
                                    alpha = if (hasApps) 1.0f else (0.35f + 0.65f * waveFactor)
                                } else {
                                    scaleX = 1.0f
                                    scaleY = 1.0f
                                    translationX = 0f
                                    alpha = if (hasApps) 0.85f else 0.30f
                                }
                            } else {
                                scaleX = 1.0f
                                scaleY = 1.0f
                                translationX = 0f
                                alpha = if (hasApps) 0.85f else 0.30f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val isApex = isTouching && activeLetter == letter

                    Text(
                        text = letter.toString(),
                        fontSize = 12.sp,
                        fontWeight = when {
                            isApex -> FontWeight.ExtraBold
                            hasApps -> FontWeight.Bold
                            else -> FontWeight.Normal
                        },
                        color = when {
                            isApex -> FlowElectricBlue
                            hasApps -> FlowTextPrimary
                            else -> FlowTextSecondary
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
