package wat.edu.pl.projektam.domain.usecase.workout

import wat.edu.pl.projektam.domain.repository.WorkoutRepository
import wat.edu.pl.projektam.util.Resource
import javax.inject.Inject

class FinishWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(id: Long, distanceM: Double, stepCount: Int): Resource<Unit> =
        repository.finishWorkout(id, distanceM, stepCount)
}
