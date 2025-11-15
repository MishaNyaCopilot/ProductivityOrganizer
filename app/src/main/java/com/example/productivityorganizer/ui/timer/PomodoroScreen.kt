package com.example.productivityorganizer.ui.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect // Import DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.productivityorganizer.ui.viewmodels.PomodoroViewModel
import com.example.productivityorganizer.ui.viewmodels.SessionType
import com.example.productivityorganizer.ui.viewmodels.TimerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val currentSessionType by viewModel.currentSessionType.collectAsStateWithLifecycle()
    val remainingTimeInMillis by viewModel.remainingTimeInMillis.collectAsStateWithLifecycle()
    val pomodoroCount by viewModel.pomodoroCount.collectAsStateWithLifecycle()

    // FIX: Сброс таймера при уходе с экрана
    DisposableEffect(key1 = viewModel) {
        onDispose {
            // При удалении композиции (например, при навигации на другой экран),
            // сбросим таймер.
            viewModel.resetTimer()
        }
    }

    val totalDuration = remember(currentSessionType, pomodoroCount) {
        when (currentSessionType) {
            SessionType.FOCUS -> PomodoroViewModel.FOCUS_DURATION_MS
            SessionType.BREAK -> {
                if ((pomodoroCount % PomodoroViewModel.POMODOROS_UNTIL_LONG_BREAK == 0) && pomodoroCount != 0) {
                    PomodoroViewModel.LONG_BREAK_DURATION_MS
                } else {
                    PomodoroViewModel.SHORT_BREAK_DURATION_MS
                }
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (totalDuration - remainingTimeInMillis).toFloat() / totalDuration.toFloat(),
        animationSpec = tween(durationMillis = 900),
        label = "pomodoro_progress_animation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (currentSessionType) {
                SessionType.FOCUS -> "Время Фокуса"
                SessionType.BREAK -> if ((pomodoroCount % PomodoroViewModel.POMODOROS_UNTIL_LONG_BREAK == 0) && pomodoroCount != 0) "Длинный Перерыв" else "Короткий Перерыв"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = primaryColor
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val strokeWidth = 16.dp.toPx()

                drawArc(
                    color = surfaceVariantColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                )

                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360 * animatedProgress,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                )
            }
            Text(
                text = formatTime(remainingTimeInMillis),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    when (timerState) {
                        TimerState.RUNNING -> viewModel.pauseTimer()
                        TimerState.PAUSED, TimerState.IDLE, TimerState.FINISHED -> viewModel.startTimer()
                    }
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.weight(1f)
            ) {
                // FIX: Правильный текст для кнопки Старт/Пауза/Продолжить
                Text(
                    when (timerState) {
                        TimerState.RUNNING -> "Пауза"
                        TimerState.PAUSED -> "Продолжить"
                        else -> "Старт" // IDLE or FINISHED
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { viewModel.resetTimer() },
                // Кнопка сброса всегда доступна, если таймер не в IDLE и время не равно полному (т.е. был какой-то прогресс)
                enabled = timerState != TimerState.IDLE || remainingTimeInMillis < totalDuration,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.weight(1f)
            ) {
                Text("Сброс")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.skipSession() },
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(
                text = when (currentSessionType) {
                    SessionType.FOCUS -> "Пропустить к Перерыву"
                    SessionType.BREAK -> "Пропустить к Фокусу"
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Завершено Помодоро: $pomodoroCount / ${PomodoroViewModel.POMODOROS_UNTIL_LONG_BREAK}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}