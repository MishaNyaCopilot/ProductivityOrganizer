package com.example.productivityorganizer.ui.viewmodels

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivityorganizer.data.local.PomodoroSession
import com.example.productivityorganizer.data.local.PomodoroSessionDao
import com.example.productivityorganizer.domain.manager.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }
enum class SessionType { FOCUS, BREAK }

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroDao: PomodoroSessionDao,
    private val application: Application,
    private val achievementManager: AchievementManager
) : ViewModel() {

    // Default
    companion object {
        const val FOCUS_DURATION_MS = 25 * 60 * 1000L
        const val SHORT_BREAK_DURATION_MS = 5 * 60 * 1000L
        const val LONG_BREAK_DURATION_MS = 15 * 60 * 1000L
        const val POMODOROS_UNTIL_LONG_BREAK = 4
    }

//    // Testing (Uncomment for testing durations)
//    companion object {
//        const val FOCUS_DURATION_MS = 15 * 1000L // 15 секунд для Фокуса
//        const val SHORT_BREAK_DURATION_MS = 5 * 1000L // 5 секунд для Короткого перерыва
//        const val LONG_BREAK_DURATION_MS = 10 * 1000L // 10 секунд для Длинного перерыва
//        const val POMODOROS_UNTIL_LONG_BREAK = 2 // Изменим на 2, чтобы быстрее добраться до длинного перерыва
//    }

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _currentSessionType = MutableStateFlow(SessionType.FOCUS)
    val currentSessionType: StateFlow<SessionType> = _currentSessionType.asStateFlow()

    private val _remainingTimeInMillis = MutableStateFlow(FOCUS_DURATION_MS)
    val remainingTimeInMillis: StateFlow<Long> = _remainingTimeInMillis.asStateFlow()

    private val _pomodoroCount = MutableStateFlow(0) // Completed focus sessions in current cycle
    val pomodoroCount: StateFlow<Int> = _pomodoroCount.asStateFlow()

    private val _sessionsToday = MutableStateFlow(0) // Could be total focus sessions for the day
    val sessionsToday: StateFlow<Int> = _sessionsToday.asStateFlow()


    private var countDownTimer: CountDownTimer? = null
    private var currentSessionStartTime: Long = 0L // Tracks the start of the *current continuous* session (across pauses)
    private var currentCyclePomodoros = 0 // Tracks completed focus sessions for determining long break

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return // Prevent starting if already running

        val durationToStartTimerWith: Long

        // Determine duration based on current state *before* changing _timerState
        if (_timerState.value == TimerState.PAUSED) {
            durationToStartTimerWith = _remainingTimeInMillis.value // Resume from where it was paused
        } else { // TimerState.IDLE or TimerState.FINISHED (starting fresh)
            currentSessionStartTime = System.currentTimeMillis() // Set start time only for a new session
            durationToStartTimerWith = when (_currentSessionType.value) {
                SessionType.FOCUS -> FOCUS_DURATION_MS
                SessionType.BREAK -> if (currentCyclePomodoros % POMODOROS_UNTIL_LONG_BREAK == 0 && currentCyclePomodoros != 0) LONG_BREAK_DURATION_MS else SHORT_BREAK_DURATION_MS
            }
            // If starting fresh, we explicitly set the remaining time to the full duration
            _remainingTimeInMillis.value = durationToStartTimerWith
        }

        _timerState.value = TimerState.RUNNING // Now change the state to RUNNING

        countDownTimer?.cancel() // Always cancel previous timer before creating a new one
        countDownTimer = object : CountDownTimer(durationToStartTimerWith, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTimeInMillis.value = millisUntilFinished
            }

            override fun onFinish() {
                handleSessionFinish()
            }
        }.start()
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            countDownTimer?.cancel()
            _timerState.value = TimerState.PAUSED
        }
    }

    /**
     * Resets the timer to its initial state, stopping any active countdown
     * and clearing the current pomodoro cycle progress.
     */
    fun resetTimer() {
        countDownTimer?.cancel()
        // Save the current session if it was active before the reset.
        // This condition ensures we don't save an IDLE or FINISHED state.
        saveCurrentSession(earlyTermination = _timerState.value != TimerState.IDLE && _timerState.value != TimerState.FINISHED)

        // Perform a full reset of the timer state and cycle
        _timerState.value = TimerState.IDLE
        _currentSessionType.value = SessionType.FOCUS // Always start new cycle with FOCUS
        _remainingTimeInMillis.value = FOCUS_DURATION_MS
        currentCyclePomodoros = 0 // Reset the cycle count
        _pomodoroCount.value = 0 // Reset the UI state for pomodoro count
        currentSessionStartTime = 0L // Reset session start time for next new session
        // _sessionsToday is not reset here, as it tracks total for the day, not cycle.
    }

    fun skipSession() {
        countDownTimer?.cancel()
        saveCurrentSession(earlyTermination = true) // Skipping is a form of early termination

        // FIX: Removed incrementing pomodoro count and achievement call when skipping a FOCUS session.
        // Skipping a FOCUS session will now transition to BREAK without counting towards 'completed pomodoro' statistics.
        // The time spent will still be saved to the database via saveCurrentSession().

        moveToNextSessionType()
    }

    private fun handleSessionFinish() {
        saveCurrentSession(earlyTermination = false)

        if (_currentSessionType.value == SessionType.FOCUS) {
            currentCyclePomodoros++
            _pomodoroCount.value = currentCyclePomodoros
            viewModelScope.launch { // Launch a coroutine for the suspend function
                // This will now only be called for naturally completed Focus sessions
                achievementManager.checkAndUnlockAchievements("") // UserID if needed, "" for local
            }
        }
        moveToNextSessionType()
    }

    private fun moveToNextSessionType() {
        _timerState.value = TimerState.IDLE
        currentSessionStartTime = 0L // Reset for the *new* session that will start

        val nextSessionType: SessionType
        val nextSessionDuration: Long

        if (_currentSessionType.value == SessionType.FOCUS) {
            nextSessionType = SessionType.BREAK
            // Ensure consistent long break logic. Long break only after POMODOROS_UNTIL_LONG_BREAK completed and not the first cycle (currentCyclePomodoros != 0)
            nextSessionDuration = if (currentCyclePomodoros % POMODOROS_UNTIL_LONG_BREAK == 0 && currentCyclePomodoros != 0) {
                LONG_BREAK_DURATION_MS
            } else {
                SHORT_BREAK_DURATION_MS
            }
        } else { // Current session was BREAK
            nextSessionType = SessionType.FOCUS
            nextSessionDuration = FOCUS_DURATION_MS
            // If a long break just finished, reset currentCyclePomodoros for the new cycle
            if (currentCyclePomodoros % POMODOROS_UNTIL_LONG_BREAK == 0 && currentCyclePomodoros != 0) {
                currentCyclePomodoros = 0
                _pomodoroCount.value = 0
            }
        }

        _currentSessionType.value = nextSessionType
        _remainingTimeInMillis.value = nextSessionDuration // Устанавливаем правильную длительность сразу
    }

    private fun saveCurrentSession(earlyTermination: Boolean) {
        if (currentSessionStartTime == 0L) return // Nothing to save if session never started

        val endTime = System.currentTimeMillis()
        val durationCompleted = if (earlyTermination) {
            // Calculate actual time spent if terminated early from a running/paused state
            // This is the total time spent in this session type, regardless of pauses.
            val initialDurationOfCurrentSession = when (_currentSessionType.value) {
                SessionType.FOCUS -> FOCUS_DURATION_MS
                SessionType.BREAK -> {
                    // For a break session, currentCyclePomodoros already reflects the count *after* the focus session that led to this break.
                    // So, it's correct to use `currentCyclePomodoros` to determine the initial planned duration of *this* break.
                    if (currentCyclePomodoros % POMODOROS_UNTIL_LONG_BREAK == 0 && currentCyclePomodoros != 0) LONG_BREAK_DURATION_MS else SHORT_BREAK_DURATION_MS
                }
            }
            // Duration completed is the total planned duration minus what was remaining.
            initialDurationOfCurrentSession - _remainingTimeInMillis.value
        } else {
            // Full duration if finished normally.
            // When a session finishes naturally, the _remainingTimeInMillis will be 0 or very close to it.
            // We use the planned full duration for consistency.
            when (_currentSessionType.value) {
                SessionType.FOCUS -> FOCUS_DURATION_MS
                SessionType.BREAK -> if (currentCyclePomodoros % POMODOROS_UNTIL_LONG_BREAK == 0 && currentCyclePomodoros != 0) LONG_BREAK_DURATION_MS else SHORT_BREAK_DURATION_MS
            }
        }

        if (durationCompleted <= 0) return // Don't save if no time has passed or if it's a very short period

        val session = PomodoroSession(
            startTime = currentSessionStartTime,
            duration = durationCompleted,
            type = _currentSessionType.value.name.lowercase()
        )

        viewModelScope.launch {
            pomodoroDao.insert(session) // This saves the actual time spent for analytics/history
        }
        currentSessionStartTime = 0L // Reset for next session start
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel() // Ensure timer is cancelled when ViewModel is destroyed
    }
}