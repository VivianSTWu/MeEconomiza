package com.example.calculaeconomia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.calculaeconomia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura o NavController associado ao NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configura o BottomNavigationView para usar o NavController
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.formularioFragment -> {
                    val currentDestination = binding.navHostFragment.getFragment<NavHostFragment>().navController.currentDestination?.id
                    if (currentDestination != R.id.formularioFragment) {
                        binding.navHostFragment.getFragment<NavHostFragment>().navController.navigate(R.id.formularioFragment)
                    }
                    true
                }
                R.id.resultadosFragment -> {
                    val currentDestination = binding.navHostFragment.getFragment<NavHostFragment>().navController.currentDestination?.id
                    if (currentDestination != R.id.resultadosFragment) {
                        binding.navHostFragment.getFragment<NavHostFragment>().navController.navigate(R.id.resultadosFragment)
                    }
                    true
                }
                else -> false
            }
        }

    }
}
