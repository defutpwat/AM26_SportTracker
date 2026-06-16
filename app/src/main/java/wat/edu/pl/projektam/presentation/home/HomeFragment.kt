package wat.edu.pl.projektam.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import wat.edu.pl.projektam.R
import wat.edu.pl.projektam.databinding.FragmentHomeBinding
import wat.edu.pl.projektam.presentation.history.WorkoutAdapter
import wat.edu.pl.projektam.util.hide
import wat.edu.pl.projektam.util.show

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val adapter = WorkoutAdapter(onDelete = {})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerRecent.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecent.adapter = adapter

        binding.btnStartWorkout.setOnClickListener {
            findNavController().navigate(R.id.workoutFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentWorkouts.collect { workouts ->
                    adapter.submitList(workouts)
                    if (workouts.isEmpty()) {
                        binding.tvNoWorkouts.show()
                        binding.recyclerRecent.hide()
                    } else {
                        binding.tvNoWorkouts.hide()
                        binding.recyclerRecent.show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
