package com.erickwok.composeslidable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.ltfan.multihaptic.HapticEffect
import top.ltfan.multihaptic.PrimitiveType
import top.ltfan.multihaptic.compose.rememberVibrator
import kotlin.math.abs
import kotlin.math.roundToInt

data class SlidableAction(
    val icon: ImageVector,
    val label: String,
    val backgroundColor: Color,
    val iconTint: Color = Color.White,
    val isPrimary: Boolean = false,
    val onClick: () -> Unit
)

private val CircleSize = 48.dp
private val ButtonMargin = 8.dp
private val SnapAnimSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
private val ButtonAnimSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
private const val ResistanceFactor = 0.15f
private const val OverSlideButtonScaleFactor = 0.3f
private const val OverSlideMarginScaleFactor = 6f

private enum class SlidableSide(val sign: Float) {
    Start(-1f),
    End(1f);

    companion object {
        fun fromDrag(drag: Float): SlidableSide? = when {
            drag > 0f -> End
            drag < 0f -> Start
            else -> null
        }
    }
}

private data class SlidableSideConfig(
    val side: SlidableSide,
    val actions: List<SlidableAction>,
    val fullSnapPx: Float,
    val overThresholdPx: Float
) {
    val primaryAction = actions.lastOrNull { it.isPrimary }
    val hasActions = actions.isNotEmpty()

    fun axisDrag(drag: Float): Float = drag * side.sign
}

private data class SlidableConfigs(
    val start: SlidableSideConfig,
    val end: SlidableSideConfig
) {
    operator fun get(side: SlidableSide): SlidableSideConfig = when (side) {
        SlidableSide.Start -> start
        SlidableSide.End -> end
    }

    fun configForDrag(drag: Float): SlidableSideConfig? = SlidableSide.fromDrag(drag)?.let(::get)
}

private data class SlidableLayoutMetrics(
    val circleSizePx: Float,
    val buttonSlotPx: Float,
    val buttonMarginPx: Float,
    val iconHalfSizePx: Float
)

private data class SlidableRuntimeState(
    val drag: Float = 0f,
    val openedSide: SlidableSide? = null
)

private data class SlidableReleaseResult(
    val target: Float,
    val action: SlidableAction? = null,
    val lockedSide: SlidableSide? = null
)

@Composable
fun SlidableCell(
    modifier: Modifier = Modifier,
    startActions: List<SlidableAction> = emptyList(),
    endActions: List<SlidableAction> = emptyList(),
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val metrics = remember(density) {
        SlidableLayoutMetrics(
            circleSizePx = with(density) { CircleSize.toPx() },
            buttonSlotPx = with(density) { (CircleSize + ButtonMargin * 2).toPx() },
            buttonMarginPx = with(density) { ButtonMargin.toPx() },
            iconHalfSizePx = with(density) { 11.dp.toPx() }
        )
    }
    val configs = remember(startActions, endActions, metrics) {
        SlidableConfigs(
            start = SlidableSideConfig(
                side = SlidableSide.Start,
                actions = startActions,
                fullSnapPx = metrics.buttonSlotPx * startActions.size,
                overThresholdPx = metrics.buttonSlotPx * startActions.size + metrics.circleSizePx * 2.5f
            ),
            end = SlidableSideConfig(
                side = SlidableSide.End,
                actions = endActions,
                fullSnapPx = metrics.buttonSlotPx * endActions.size,
                overThresholdPx = metrics.buttonSlotPx * endActions.size + metrics.circleSizePx * 2.5f
            )
        )
    }

    val settleAnimatable = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val vibrator = rememberVibrator()
    var state by remember { mutableStateOf(SlidableRuntimeState()) }
    var lastOverSlide by remember { mutableStateOf<Boolean?>(null) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    val collapse: () -> Unit = {
        val currentDrag = state.drag
        settleJob?.cancel()
        settleJob = coroutineScope.launch {
            val currentJob = coroutineContext[Job]
            try {
                settleAnimatable.snapTo(currentDrag)
                settleAnimatable.animateTo(0f, SnapAnimSpec) {
                    state = state.copy(drag = value)
                }
            } finally {
                if (settleJob === currentJob) {
                    state = state.copy(openedSide = null)
                    settleJob = null
                }
            }
        }
    }

    val overSlideTriggered = isOverSlide(state, configs)
    val visibleConfig = visibleConfig(state, configs)

    LaunchedEffect(overSlideTriggered) {
        lastOverSlide?.let { wasOverSlide ->
            if (wasOverSlide != overSlideTriggered) {
                vibrator.vibrate(HapticEffect { predefined(PrimitiveType.Click) { scale = 1f } })
            }
        }
        lastOverSlide = overSlideTriggered
    }

    Box(modifier = modifier.fillMaxWidth()) {
        visibleConfig?.let { config ->
            SlidableCellButtons(
                config = config,
                metrics = metrics,
                drag = config.axisDrag(state.drag),
                overSlideTriggered = overSlideTriggered,
                onActionClick = { action ->
                    action.onClick()
                    collapse()
                },
                modifier = Modifier.align(
                    when (config.side) {
                        SlidableSide.Start -> Alignment.CenterStart
                        SlidableSide.End -> Alignment.CenterEnd
                    }
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset((-state.drag).roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        state = reduceDrag(state = state, input = -delta, configs = configs)
                    },
                    onDragStarted = {
                        settleJob?.cancel()
                        settleJob = null
                    },
                    onDragStopped = {
                        val currentState = state
                        val release = resolveRelease(currentState, configs)
                        val currentDrag = currentState.drag

                        release.lockedSide?.let { lockedSide ->
                            state = state.copy(openedSide = lockedSide)
                        }
                        release.action?.onClick()

                        settleJob?.cancel()
                        settleJob = coroutineScope.launch {
                            val currentJob = coroutineContext[Job]
                            try {
                                settleAnimatable.snapTo(currentDrag)
                                settleAnimatable.animateTo(release.target, SnapAnimSpec) {
                                    state = state.copy(drag = value)
                                }
                            } finally {
                                if (settleJob === currentJob) {
                                    if (release.target == 0f && state.openedSide == release.lockedSide) {
                                        state = state.copy(openedSide = null)
                                    }
                                    settleJob = null
                                }
                            }
                        }
                    }
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = state.openedSide != null
                ) { collapse() }
        ) {
            content()
        }
    }
}

private fun reduceDrag(
    state: SlidableRuntimeState,
    input: Float,
    configs: SlidableConfigs
): SlidableRuntimeState {
    if (input == 0f) return state

    val draggingSide = state.openedSide ?: SlidableSide.fromDrag(
        if (state.drag != 0f) state.drag else input
    )
    val nextDrag = draggingSide?.let { side ->
        applyOpenedSideDragDelta(
            current = state.drag,
            input = input,
            openedSide = side,
            side = configs[side]
        )
    } ?: applyFreeDragDelta(
        current = state.drag,
        input = input,
        configs = configs
    )

    return state.copy(
        drag = nextDrag,
        openedSide = state.openedSide ?: draggingSide
    )
}

private fun applyFreeDragDelta(
    current: Float,
    input: Float,
    configs: SlidableConfigs
): Float {
    if (current == 0f || (current > 0f) == (input > 0f)) {
        val side = configs.configForDrag(input) ?: return current
        return applyOutwardDelta(current = current, input = input, side = side)
    }

    val next = current + input
    if ((current > 0f && next >= 0f) || (current < 0f && next <= 0f)) {
        return next
    }

    val side = configs.configForDrag(next) ?: return 0f
    return applyOutwardDelta(current = 0f, input = next, side = side)
}

private fun applyOutwardDelta(
    current: Float,
    input: Float,
    side: SlidableSideConfig
): Float {
    val direction = if (input > 0f) 1f else -1f
    val currentMagnitude = abs(current)
    val inputMagnitude = abs(input)
    val nextMagnitude = when {
        !side.hasActions -> currentMagnitude + inputMagnitude * ResistanceFactor
        side.primaryAction != null -> currentMagnitude + inputMagnitude
        currentMagnitude < side.fullSnapPx -> {
            val freeDrag = side.fullSnapPx - currentMagnitude
            if (inputMagnitude <= freeDrag) currentMagnitude + inputMagnitude
            else side.fullSnapPx + (inputMagnitude - freeDrag) * ResistanceFactor
        }
        else -> currentMagnitude + inputMagnitude * ResistanceFactor
    }
    return direction * nextMagnitude
}

private fun applyOpenedSideDragDelta(
    current: Float,
    input: Float,
    openedSide: SlidableSide,
    side: SlidableSideConfig
): Float {
    val axisCurrent = current * openedSide.sign
    val axisInput = input * openedSide.sign
    val axisResult = when {
        axisCurrent >= 0f && axisInput >= 0f -> {
            applyOutwardDelta(current = axisCurrent, input = axisInput, side = side)
        }
        axisCurrent >= 0f && axisInput < 0f -> {
            val next = axisCurrent + axisInput
            if (next >= 0f) next else next * ResistanceFactor
        }
        axisCurrent < 0f && axisInput < 0f -> {
            axisCurrent + axisInput * ResistanceFactor
        }
        else -> {
            val next = axisCurrent + axisInput
            if (next <= 0f) next
            else applyOutwardDelta(current = 0f, input = next, side = side)
        }
    }
    return axisResult * openedSide.sign
}

private fun resolveRelease(
    state: SlidableRuntimeState,
    configs: SlidableConfigs
): SlidableReleaseResult {
    val lockedSide = state.openedSide
    val releaseSide = lockedSide ?: SlidableSide.fromDrag(state.drag)
    val config = releaseSide?.let(configs::get) ?: return SlidableReleaseResult(target = 0f)
    val axisDrag = config.axisDrag(state.drag)

    if (lockedSide != null && axisDrag <= 0f) {
        return SlidableReleaseResult(target = 0f, lockedSide = lockedSide)
    }

    val release = resolveSideRelease(
        drag = if (lockedSide != null) axisDrag else abs(state.drag),
        side = config
    )
    return release.copy(
        target = release.target * config.side.sign,
        lockedSide = lockedSide ?: config.side
    )
}

private fun resolveSideRelease(
    drag: Float,
    side: SlidableSideConfig
): SlidableReleaseResult {
    if (!side.hasActions) return SlidableReleaseResult(target = 0f)
    side.primaryAction?.let { primaryAction ->
        if (drag > side.overThresholdPx) {
            return SlidableReleaseResult(target = 0f, action = primaryAction)
        }
    }
    val target = if (drag > side.fullSnapPx * 0.5f) side.fullSnapPx else 0f
    return SlidableReleaseResult(target = target)
}

private fun isOverSlide(
    state: SlidableRuntimeState,
    configs: SlidableConfigs
): Boolean {
    val side = state.openedSide ?: SlidableSide.fromDrag(state.drag) ?: return false
    val config = configs[side]
    return config.primaryAction != null && config.axisDrag(state.drag) > config.overThresholdPx
}

private fun visibleConfig(
    state: SlidableRuntimeState,
    configs: SlidableConfigs
): SlidableSideConfig? = when {
    state.drag > 0f && state.openedSide != SlidableSide.Start && configs.end.hasActions -> configs.end
    state.drag < 0f && state.openedSide != SlidableSide.End && configs.start.hasActions -> configs.start
    else -> null
}

@Composable
private fun SlidableCellButtons(
    config: SlidableSideConfig,
    metrics: SlidableLayoutMetrics,
    drag: Float,
    overSlideTriggered: Boolean,
    onActionClick: (SlidableAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val n = config.actions.size
    val displayActions = when (config.side) {
        SlidableSide.End -> config.actions
        SlidableSide.Start -> config.actions.reversed()
    }
    val iconTranslationDirection = when (config.side) {
        SlidableSide.End -> -1f
        SlidableSide.Start -> 1f
    }

    val overSlideButtonScale = if (config.primaryAction == null && drag > config.fullSnapPx) {
        1f + ((drag - config.fullSnapPx) / (config.overThresholdPx - config.fullSnapPx)).coerceIn(0f, 1f) * OverSlideButtonScaleFactor
    } else {
        1f
    }
    val overSlideMarginScale = if (config.primaryAction == null && drag > config.fullSnapPx) {
        1f + ((drag - config.fullSnapPx) / (config.overThresholdPx - config.fullSnapPx)).coerceIn(0f, 1f) * OverSlideMarginScaleFactor
    } else {
        1f
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (config.side == SlidableSide.Start) Spacer(Modifier.width(ButtonMargin))

        displayActions.forEachIndexed { index, action ->
            val isPrimary = action === config.primaryAction
            val revealIndex = when (config.side) {
                SlidableSide.End -> (n - 1) - index
                SlidableSide.Start -> index
            }
            val progress =
                ((drag - revealIndex * metrics.buttonSlotPx) / metrics.buttonSlotPx).coerceIn(0f, 1f)
            val nonPrimaryAlphaMult by animateFloatAsState(
                targetValue = if (!isPrimary && config.primaryAction != null && drag > config.fullSnapPx) 0.5f else 1f,
                animationSpec = ButtonAnimSpec,
                label = "nonPrimaryAlpha"
            )
            val compositeAlpha = if (isPrimary) progress else progress * nonPrimaryAlphaMult

            val buttonWidthPx = if (isPrimary && drag > config.fullSnapPx) {
                metrics.circleSizePx + (drag - config.fullSnapPx)
            } else {
                metrics.circleSizePx
            }
            val buttonWidthDp = with(density) { buttonWidthPx.toDp() }
            val iconAlignProgress by animateFloatAsState(
                targetValue = if (isPrimary && overSlideTriggered) 1f else 0f,
                animationSpec = ButtonAnimSpec,
                label = "iconAlign"
            )
            val labelAlpha by animateFloatAsState(
                targetValue = if (isPrimary && overSlideTriggered) 0f else 1f,
                animationSpec = ButtonAnimSpec,
                label = "labelAlpha"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = compositeAlpha
                        if (!isPrimary || drag <= config.fullSnapPx) {
                            val s = if (progress >= 1f) overSlideButtonScale else progress
                            scaleX = s
                            scaleY = s
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = progress >= 1f
                    ) { onActionClick(action) }
            ) {
                Box(
                    modifier = Modifier
                        .width(buttonWidthDp)
                        .height(CircleSize)
                        .clip(RoundedCornerShape(50))
                        .background(action.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = action.iconTint,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                translationX =
                                    iconTranslationDirection *
                                        (buttonWidthPx / 2f - metrics.buttonMarginPx - metrics.iconHalfSizePx) *
                                        iconAlignProgress
                            }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = action.label,
                    color = action.backgroundColor,
                    fontSize = 11.sp,
                    modifier = Modifier.graphicsLayer { alpha = labelAlpha }
                )
            }

            if (index < n - 1) Spacer(Modifier.width(ButtonMargin * overSlideMarginScale))
        }

        if (config.side == SlidableSide.End) Spacer(Modifier.width(ButtonMargin * overSlideMarginScale))
    }
}
