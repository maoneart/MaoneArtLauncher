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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
    var totalHeight by remember { mutableFloatStateOf(0f) }

    // Elastic spring physics for wave expansion & collapse (Niagara fluid rubber physics)
    val waveIntensity by animateFloatAsState(
        targetValue = if (isTouching) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.52f, // Bouncy & rubbery wave elastic feel
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
        if (totalHeight <= 0f) return
        val clampedY = y.coerceIn(0f, totalHeight)
        touchY = clampedY

        val itemHeight = totalHeight / alphabet.size
        val index = (clampedY / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
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
            .width(58.dp)
            .onGloballyPositioned { coordinates ->
                totalHeight = coordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isTouching = true
                        handleTouchAt(offset.y)
                        tryAwaitRelease()
                        isTouching = false
                        activeLetter = null
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isTouching = true
                        handleTouchAt(offset.y)
                    },
                    onDragEnd = {
                        isTouching = false
                        activeLetter = null
                    },
                    onDragCancel = {
                        isTouching = false
                        activeLetter = null
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        handleTouchAt(change.position.y)
                    }
                )
            },
        contentAlignment = if (isRightSide) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        // Floating Active Letter Balloon / Bubble at the crest of the wave
        AnimatedVisibility(
            visible = isTouching && activeLetter != null,
            enter = fadeIn() + scaleIn(spring(dampingRatio = 0.55f)),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(if (isRightSide) Alignment.TopEnd else Alignment.TopStart)
                .offset {
                    val offsetY = (animatedTouchY - with(density) { 33.dp.toPx() }).roundToInt()
                    // Position balloon comfortably to the left of the wave apex
                    val offsetX = if (isRightSide) with(density) { (-118).dp.roundToPx() } else with(density) { 118.dp.roundToPx() }
                    IntOffset(offsetX, offsetY.coerceIn(0, (totalHeight - 120).toInt().coerceAtLeast(0)))
                }
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .shadow(20.dp, CircleShape, spotColor = FlowElectricBlue)
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

        // Vertical Alphabet List with Authentic Wide Niagara Wave Curve
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 20.dp),
            horizontalAlignment = if (isRightSide) Alignment.End else Alignment.Start
        ) {
            // Authentic Niagara wave influence radius: ~190dp (~9-11 letters influenced)
            val waveRadiusPx = with(density) { 190.dp.toPx() }
            val maxOutwardDisplacementPx = with(density) { (if (isRightSide) -82.dp else 82.dp).toPx() }
            val itemHeight = if (totalHeight > 0f) totalHeight / alphabet.size else 22f

            alphabet.forEachIndexed { i, letter ->
                val letterCenterY = (i + 0.5f) * itemHeight
                val hasApps = availableLetters.contains(letter)

                // 120 FPS GPU-accelerated graphicsLayer without recomposition overhead!
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(42.dp)
                        .graphicsLayer {
                            if (waveIntensity > 0.005f) {
                                val dist = abs(animatedTouchY - letterCenterY)
                                if (dist < waveRadiusPx) {
                                    val normalized = dist / waveRadiusPx
                                    // Cosine bell curve with power shaping for dramatic wave crest
                                    val rawFactor = ((1f + cos(normalized * PI.toFloat())) / 2f)
                                    val waveFactor = rawFactor.toDouble().pow(1.25).toFloat() * waveIntensity

                                    val scale = 1.0f + (1.35f * waveFactor) // Expands up to 2.35x
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = maxOutwardDisplacementPx * waveFactor
                                } else {
                                    scaleX = 1.0f
                                    scaleY = 1.0f
                                    translationX = 0f
                                }
                            } else {
                                scaleX = 1.0f
                                scaleY = 1.0f
                                translationX = 0f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val isApex = isTouching && activeLetter == letter

                    Text(
                        text = letter.toString(),
                        fontSize = if (isApex) 15.sp else 11.sp,
                        fontWeight = when {
                            isApex -> FontWeight.ExtraBold
                            hasApps -> FontWeight.SemiBold
                            else -> FontWeight.Normal
                        },
                        color = when {
                            isApex -> FlowElectricBlue
                            hasApps -> FlowTextPrimary
                            else -> FlowTextSecondary.copy(alpha = 0.30f)
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
