package wat.edu.pl.projektam.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wat.edu.pl.projektam.domain.model.Workout
import wat.edu.pl.projektam.domain.repository.WorkoutRepository
import wat.edu.pl.projektam.domain.usecase.workout.GetWorkoutsUseCase
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getWorkoutsUseCase: GetWorkoutsUseCase,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    // stateIn konwertuje Flow na StateFlow — fragment subskrybuje raz,
    // WhileSubscribed(5000) zatrzymuje zbieranie 5s po zniknięciu obserwatora
    val workouts: StateFlow<List<Workout>> = getWorkoutsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workout.id)
        }
    }
}
