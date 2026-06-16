package wat.edu.pl.projektam.domain.usecase.workout

import wat.edu.pl.projektam.domain.repository.WorkoutRepository
import javax.inject.Inject

class StartWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(type: String): Long = repository.startWorkout(type)
}
