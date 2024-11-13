package com.example.calculaeconomia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // Carregar o fragmento
            supportFragmentManager.commit {
                replace(R.id.fragment_container, LocationFragment())
            }
        }
    }
}
