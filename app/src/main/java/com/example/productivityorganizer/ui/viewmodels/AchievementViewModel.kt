package com.example.productivityorganizer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivityorganizer.data.model.achievement.DisplayAchievement
import com.example.productivityorganizer.data.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementsUiState(
    val achievements: List<DisplayAchievement> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
        // Initial check when ViewModel is created.
        // Further checks can be triggered by other user actions via other ViewModels calling AchievementManager,
        // or a manual refresh if implemented in UI.
        triggerAchievementChecks()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            achievementRepository.getAllAchievementsWithStatus()
                .catch { e ->
                    _uiState.value = AchievementsUiState(isLoading = false, error = e.message)
                }
                .collect { achievementsList ->
                    _uiState.value = AchievementsUiState(achievements = achievementsList, isLoading = false)
                }
        }
    }

    fun triggerAchievementChecks() {
        viewModelScope.launch {
            // Consider adding loading/feedback states if this is user-initiated and takes time
            achievementRepository.checkAndUnlockAchievements()
            // No direct reload needed here as the flows from repository should emit new data if unlocks happen.
            // If there are issues with flow not re-emitting, a manual refresh of achievements might be needed.
        }
    }
}
