package id.maoneart.flow.ui.components

import android.view.HapticFeedbackConstants
import id.maoneart.flow.ui.util.rememberFlowHaptics
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val haptics = rememberFlowHaptics()

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
                haptics.tick(view)
            }
            onLetterSelected(letter)
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(54.dp)
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
                    val offsetX = if (isRightSide) with(density) { (-108).dp.roundToPx() } else with(density) { 108.dp.roundToPx() }
                    IntOffset(offsetX, offsetY.coerceIn(0, (containerHeight - 120).toInt().coerceAtLeast(0)))
                }
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
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
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // Vertical Alphabet List with Authentic Niagara Parabolic Wave Curve Projection
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    top = 8.dp,
                    bottom = 18.dp,
                    start = if (isRightSide) 0.dp else 8.dp,
                    end = if (isRightSide) 8.dp else 0.dp
                )
                .onGloballyPositioned { coordinates ->
                    columnHeight = coordinates.size.height.toFloat()
                    columnTopY = coordinates.positionInParent().y
                },
            horizontalAlignment = if (isRightSide) Alignment.End else Alignment.Start
        ) {
            // Authentic Niagara wave influence radius: ~180dp (~7-9 letters influenced simultaneously)
            val waveRadiusPx = with(density) { 180.dp.toPx() }
            val maxDisplacementPx = with(density) { (if (isRightSide) -68.dp else 68.dp).toPx() }
            val itemHeight = if (columnHeight > 0f) columnHeight / alphabet.size else 22f

            alphabet.forEachIndexed { i, letter ->
                val letterCenterY = columnTopY + (i + 0.5f) * itemHeight
                val hasApps = availableLetters.contains(letter)

                // 120 FPS GPU-accelerated graphicsLayer: expands safely without screen-edge clipping
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(36.dp)
                        .graphicsLayer {
                            clip = false
                            // Pivot toward screen interior to eliminate right-edge font clipping
                            transformOrigin = TransformOrigin(if (isRightSide) 0.85f else 0.15f, 0.5f)

                            if (waveIntensity > 0.005f) {
                                val dist = abs(animatedTouchY - letterCenterY)
                                if (dist < waveRadiusPx) {
                                    val normalized = dist / waveRadiusPx
                                    // Smooth cosine bell curve
                                    val rawFactor = ((1f + cos(normalized * PI.toFloat())) / 2f)
                                    val waveFactor = rawFactor.toDouble().pow(1.35).toFloat() * waveIntensity

                                    val scale = 1.0f + (0.80f * waveFactor) // Expands up to 1.80x cleanly
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = maxDisplacementPx * waveFactor
                                    alpha = if (hasApps) 1.0f else (0.35f + 0.65f * waveFactor)
                                } else {
                                    scaleX = 1.0f
                                    scaleY = 1.0f
                                    translationX = 0f
                                    alpha = if (hasApps) 0.85f else 0.28f
                                }
                            } else {
                                scaleX = 1.0f
                                scaleY = 1.0f
                                translationX = 0f
                                alpha = if (hasApps) 0.85f else 0.28f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val isApex = isTouching && activeLetter == letter

                    Text(
                        text = letter.toString(),
                        fontSize = 10.5.sp,
                        fontWeight = when {
                            isApex -> FontWeight.ExtraBold
                            hasApps -> FontWeight.Bold
                            else -> FontWeight.Medium
                        },
                        color = when {
                            isApex -> FlowElectricBlue
                            hasApps -> FlowTextPrimary
                            else -> FlowTextSecondary
                        },
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
