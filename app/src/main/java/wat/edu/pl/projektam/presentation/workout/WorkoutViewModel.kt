package wat.edu.pl.projektam.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wat.edu.pl.projektam.domain.repository.WorkoutRepository
import wat.edu.pl.projektam.domain.usecase.workout.FinishWorkoutUseCase
import wat.edu.pl.projektam.domain.usecase.workout.StartWorkoutUseCase
import wat.edu.pl.projektam.service.TrackingState
import wat.edu.pl.projektam.service.WorkoutTrackingService
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    // Bezpośrednio obserwujemy StateFlow z serwisu — nie ma potrzeby duplikowania stanu
    val trackingState: StateFlow<TrackingState> = WorkoutTrackingService.state

    fun startWorkout(type: String, onReady: (Long) -> Unit) {
        viewModelScope.launch {
            val workoutId = startWorkoutUseCase(type)
            onReady(workoutId)
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            val state = trackingState.value
            finishWorkoutUseCase(state.workoutId, state.distanceM, state.stepCount)
            workoutRepository.syncWithServer()
        }
    }
}
