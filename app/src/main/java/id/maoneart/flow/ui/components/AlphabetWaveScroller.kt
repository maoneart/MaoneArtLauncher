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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    // Elastic spring physics for wave expansion & collapse
    val waveIntensity by animateFloatAsState(
        targetValue = if (isTouching) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "wave_spring"
    )

    // Smooth liquid interpolation for tracking finger position
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
            .width(54.dp)
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
        // Niagara Floating Active Letter Balloon / Bubble with Spring Motion
        AnimatedVisibility(
            visible = isTouching && activeLetter != null,
            enter = fadeIn() + scaleIn(spring(dampingRatio = 0.6f)),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(if (isRightSide) Alignment.TopEnd else Alignment.TopStart)
                .offset {
                    val offsetY = (animatedTouchY - with(density) { 32.dp.toPx() }).roundToInt()
                    val offsetX = if (isRightSide) with(density) { (-78).dp.roundToPx() } else with(density) { 78.dp.roundToPx() }
                    IntOffset(offsetX, offsetY.coerceIn(0, (totalHeight - 110).toInt().coerceAtLeast(0)))
                }
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .shadow(16.dp, CircleShape, spotColor = FlowElectricBlue)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(FlowElectricBlue, FlowCyan)
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

        // Vertical Alphabet List with 3D Niagara Elastic Wave Curve Projection
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp),
            horizontalAlignment = if (isRightSide) Alignment.End else Alignment.Start
        ) {
            val waveRadiusPx = with(density) { 110.dp.toPx() } // Niagara wide influence radius (~5-6 letters)
            val itemHeight = if (totalHeight > 0f) totalHeight / alphabet.size else 20f

            alphabet.forEachIndexed { i, letter ->
                val letterCenterY = (i + 0.5f) * itemHeight

                val (scale, offsetX, isApex) = if (waveIntensity > 0.01f) {
                    val dist = abs(animatedTouchY - letterCenterY)
                    if (dist < waveRadiusPx) {
                        // Smooth Cosine bell-curve wave projection
                        val normalized = dist / waveRadiusPx
                        val bellFactor = ((1f + cos(normalized * PI.toFloat())) / 2f)
                        val effectiveFactor = bellFactor * waveIntensity

                        val s = 1.0f + (1.15f * effectiveFactor) // Expands up to 2.15x
                        val ox = (if (isRightSide) -48f else 48f) * effectiveFactor // Curves outward up to 48dp!
                        Triple(s, ox, bellFactor > 0.82f)
                    } else {
                        Triple(1.0f, 0f, false)
                    }
                } else {
                    Triple(1.0f, 0f, false)
                }

                val hasApps = availableLetters.contains(letter)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(38.dp)
                        .offset { IntOffset(with(density) { offsetX.dp.roundToPx() }, 0) }
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = if (isApex) 16.sp else 11.sp,
                        fontWeight = when {
                            isApex -> FontWeight.ExtraBold
                            hasApps -> FontWeight.SemiBold
                            else -> FontWeight.Normal
                        },
                        color = when {
                            isApex -> FlowElectricBlue
                            hasApps -> FlowTextPrimary
                            else -> FlowTextSecondary.copy(alpha = 0.35f)
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
