package wat.edu.pl.projektam.presentation.workout

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import wat.edu.pl.projektam.R
import wat.edu.pl.projektam.databinding.FragmentWorkoutBinding
import wat.edu.pl.projektam.service.WorkoutTrackingService
import wat.edu.pl.projektam.util.toFormattedDistance
import wat.edu.pl.projektam.util.toFormattedDuration

@AndroidEntryPoint
class WorkoutFragment : Fragment() {

    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private val routePoints = mutableListOf<LatLng>()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            enableMapLocation()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
        setupWorkoutTypeDropdown()
        setupStartStopButton()
        observeTrackingState()
    }

    // ── Mapa ─────────────────────────────────────────────────────────────

    private fun setupMap() {
        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync { map ->
            googleMap = map
            map.uiSettings.isZoomControlsEnabled = true
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), fine) == PackageManager.PERMISSION_GRANTED) {
            enableMapLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(fine, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    @Suppress("MissingPermission")
    private fun enableMapLocation() {
        googleMap?.isMyLocationEnabled = true
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_location_title)
            .setMessage(R.string.permission_location_message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .show()
    }

    // ── Dropdown wyboru typu treningu ─────────────────────────────────────

    private fun setupWorkoutTypeDropdown() {
        val types = listOf(
            getString(R.string.workout_type_running),
            getString(R.string.workout_type_walking),
            getString(R.string.workout_type_strength)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.dropdownType.setAdapter(adapter)
        binding.dropdownType.setText(types[0], false)
    }

    private fun selectedWorkoutType(): String {
        return when (binding.dropdownType.text.toString()) {
            getString(R.string.workout_type_walking) -> "WALKING"
            getString(R.string.workout_type_strength) -> "STRENGTH"
            else -> "RUNNING"
        }
    }

    // ── Start / Stop ──────────────────────────────────────────────────────

    private fun setupStartStopButton() {
        binding.btnStartStop.setOnClickListener {
            if (WorkoutTrackingService.state.value.isTracking) {
                stopWorkout()
            } else {
                startWorkout()
            }
        }
    }

    private fun startWorkout() {
        viewModel.startWorkout(selectedWorkoutType()) { workoutId ->
            WorkoutTrackingService.start(requireContext(), workoutId)
        }
    }

    private fun stopWorkout() {
        WorkoutTrackingService.stop(requireContext())
        viewModel.finishWorkout()
        routePoints.clear()
        googleMap?.clear()
    }

    // ── Obserwowanie stanu serwisu ────────────────────────────────────────

    private fun observeTrackingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trackingState.collect { state ->
                    // Aktualizuj liczniki
                    binding.tvDistance.text = state.distanceM.toFormattedDistance()
                    binding.tvDuration.text = state.durationMs.toFormattedDuration()
                    binding.tvSteps.text = state.stepCount.toString()

                    // Przycisk i dropdown
                    val tracking = state.isTracking
                    binding.btnStartStop.text = getString(
                        if (tracking) R.string.workout_stop else R.string.home_start_workout
                    )
                    binding.tilWorkoutType.visibility = if (tracking) View.GONE else View.VISIBLE

                    // Rysuj trasę na mapie
                    state.currentLocation?.let { loc ->
                        val latLng = LatLng(loc.latitude, loc.longitude)
                        if (tracking) {
                            routePoints.add(latLng)
                            drawRoute()
                            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                        }
                    }
                }
            }
        }
    }

    private fun drawRoute() {
        if (routePoints.size < 2) return
        googleMap?.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .width(10f)
                .color(requireContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_primary))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
