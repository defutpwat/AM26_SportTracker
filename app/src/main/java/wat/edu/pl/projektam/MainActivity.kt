package wat.edu.pl.projektam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import wat.edu.pl.projektam.data.local.preferences.AppPreferences
import wat.edu.pl.projektam.data.local.preferences.TokenManager
import wat.edu.pl.projektam.databinding.ActivityMainBinding
import wat.edu.pl.projektam.util.hide
import wat.edu.pl.projektam.util.show
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Przywróć zapisany motyw i język przed narysowaniem UI
        lifecycleScope.launch {
            val theme = appPreferences.themeFlow.first()
            val nightMode = when (theme) {
                AppPreferences.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppPreferences.THEME_DARK  -> AppCompatDelegate.MODE_NIGHT_YES
                else                       -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)

            val lang = appPreferences.languageFlow.first()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (tokenManager.isLoggedIn()) {
            navController.setGraph(R.navigation.nav_graph)
            binding.bottomNav.show()
            binding.bottomNav.setupWithNavController(navController)
        } else {
            navController.setGraph(R.navigation.auth_nav_graph)
            binding.bottomNav.hide()
        }
    }

    fun switchToMainGraph() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph)
        binding.bottomNav.show()
        binding.bottomNav.setupWithNavController(navController)
    }

    fun switchToAuthGraph() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(R.navigation.auth_nav_graph)
        binding.bottomNav.hide()
    }
}
