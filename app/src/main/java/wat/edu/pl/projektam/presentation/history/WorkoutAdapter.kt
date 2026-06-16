package wat.edu.pl.projektam.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import wat.edu.pl.projektam.databinding.ItemWorkoutBinding
import wat.edu.pl.projektam.domain.model.Workout
import wat.edu.pl.projektam.domain.model.WorkoutType
import wat.edu.pl.projektam.util.toDisplayString
import wat.edu.pl.projektam.util.toFormattedDistance
import wat.edu.pl.projektam.util.toFormattedDuration

class WorkoutAdapter(
    private val onDelete: (Workout) -> Unit
) : ListAdapter<Workout, WorkoutAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: Workout) {
            binding.tvWorkoutType.text = workout.workoutType.toLabel()
            binding.tvDate.text = workout.startedAt.toDisplayString()
            binding.tvDistance.text = workout.distanceM.toFormattedDistance()
            binding.tvSteps.text = workout.stepCount.toString()

            val durationMs = (workout.endedAt?.time ?: workout.startedAt.time) - workout.startedAt.time
            binding.tvDuration.text = durationMs.toFormattedDuration()

            // Długie przytrzymanie → dialog usuwania
            binding.root.setOnLongClickListener {
                onDelete(workout)
                true
            }
        }

        private fun WorkoutType.toLabel(): String = when (this) {
            WorkoutType.RUNNING  -> itemView.context.getString(wat.edu.pl.projektam.R.string.workout_type_running)
            WorkoutType.WALKING  -> itemView.context.getString(wat.edu.pl.projektam.R.string.workout_type_walking)
            WorkoutType.STRENGTH -> itemView.context.getString(wat.edu.pl.projektam.R.string.workout_type_strength)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Workout>() {
        override fun areItemsTheSame(old: Workout, new: Workout) = old.id == new.id
        override fun areContentsTheSame(old: Workout, new: Workout) = old == new
    }
}
