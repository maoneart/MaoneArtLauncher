package id.maoneart.flow.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import id.maoneart.flow.ui.theme.FlowDarkSurface
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
            .width(48.dp)
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
        // Floating Niagara Active Letter Bubble
        AnimatedVisibility(
            visible = isTouching && activeLetter != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    val offsetY = (touchY - with(density) { 32.dp.toPx() }).roundToInt()
                    val offsetX = if (isRightSide) with(density) { (-64).dp.roundToPx() } else with(density) { 64.dp.roundToPx() }
                    IntOffset(offsetX, offsetY.coerceIn(0, (totalHeight - 100).toInt().coerceAtLeast(0)))
                }
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(12.dp, CircleShape)
                    .background(FlowElectricBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter?.toString() ?: "",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Vertical Alphabet List with Wave Projection
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp),
            horizontalAlignment = if (isRightSide) Alignment.End else Alignment.Start
        ) {
            val waveRadiusPx = with(density) { 85.dp.toPx() }
            val itemHeight = if (totalHeight > 0f) totalHeight / alphabet.size else 20f

            alphabet.forEachIndexed { i, letter ->
                val letterCenterY = (i + 0.5f) * itemHeight

                val (scale, offsetX, isHighlighted) = if (isTouching) {
                    val dist = abs(touchY - letterCenterY)
                    if (dist < waveRadiusPx) {
                        val factor = 1f - (dist / waveRadiusPx)
                        val smoothFactor = ((1f - cos(factor * PI.toFloat())) / 2f)
                        val s = 1.0f + (0.75f * smoothFactor)
                        val ox = if (isRightSide) -24f * smoothFactor else 24f * smoothFactor
                        Triple(s, ox, factor > 0.6f)
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
                        .width(36.dp)
                        .offset { IntOffset(with(density) { offsetX.dp.roundToPx() }, 0) }
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = if (isHighlighted) 15.sp else 11.sp,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isHighlighted -> FlowElectricBlue
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
