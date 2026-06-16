package wat.edu.pl.projektam.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import wat.edu.pl.projektam.R
import wat.edu.pl.projektam.databinding.FragmentHistoryBinding
import wat.edu.pl.projektam.domain.model.Workout
import wat.edu.pl.projektam.util.hide
import wat.edu.pl.projektam.util.show

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()

    private val adapter = WorkoutAdapter(onDelete = ::showDeleteDialog)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerWorkouts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWorkouts.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.workouts.collect { workouts ->
                    adapter.submitList(workouts)
                    if (workouts.isEmpty()) {
                        binding.tvEmpty.show()
                        binding.recyclerWorkouts.hide()
                    } else {
                        binding.tvEmpty.hide()
                        binding.recyclerWorkouts.show()
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(workout: Workout) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_workout_title)
            .setMessage(R.string.delete_workout_message)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteWorkout(workout)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
