package com.example.weatherwear

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val REQUEST_LOCATION_PERMISSION = 1
    lateinit var previous : Previous

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previous = Previous(this)
        val prevWeather = findViewById<TextView>(R.id.previousWeather)
        val prevClothes = findViewById<TextView>(R.id.previousClothes)
        val clearButton = findViewById<Button>(R.id.clearButton)



        prevWeather.text = previous.getWeather()
        prevClothes.text = previous.getClothes()

        clearButton.setOnClickListener {
            previous.setClothes("Clothes: N/A")
            prevClothes.text = "Clothes: N/A"
            previous.setWeather("Weather: N/A")
            prevWeather.text = "Weather: N/A"
            previous.setPreferences(this)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            val startButton = findViewById<Button>(R.id.startButton)



            startButton.setOnClickListener {
                val intent = Intent(this, InfoActivity::class.java)
                intent.putExtra("prev", previous)
                val animationBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_left, 0).toBundle()
                startActivity(intent, animationBundle)

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location-related operations
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

}

