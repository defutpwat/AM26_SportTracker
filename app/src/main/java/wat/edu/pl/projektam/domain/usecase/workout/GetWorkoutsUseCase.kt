package wat.edu.pl.projektam.domain.usecase.workout

import kotlinx.coroutines.flow.Flow
import wat.edu.pl.projektam.domain.model.Workout
import wat.edu.pl.projektam.domain.repository.WorkoutRepository
import javax.inject.Inject

class GetWorkoutsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<Workout>> = repository.getWorkoutsFlow()
}
