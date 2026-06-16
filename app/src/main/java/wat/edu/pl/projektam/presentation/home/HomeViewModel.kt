package wat.edu.pl.projektam.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import wat.edu.pl.projektam.domain.model.Workout
import wat.edu.pl.projektam.domain.usecase.workout.GetWorkoutsUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getWorkoutsUseCase: GetWorkoutsUseCase
) : ViewModel() {

    // Pokazujemy tylko 3 ostatnie treningi na dashboardzie
    val recentWorkouts: StateFlow<List<Workout>> = getWorkoutsUseCase()
        .map { it.take(3) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
