package com.example.weatherwear


import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi

class Weather : WeatherFunctions() {

    private var startHour: Int = 0
    private var startMinute: Int = 0
    private var endHour: Int = 0
    private var endMinute: Int = 0
    private lateinit var tryAgainButton: Button


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startHour = intent.getIntExtra("start_hour", 0)
        startMinute = intent.getIntExtra("start_minute", 0)
        endHour = intent.getIntExtra("end_hour", 0)
        endMinute = intent.getIntExtra("end_minute", 0)

        tryAgainButton = findViewById<Button>(R.id.tryAgainButton)
        tryAgainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val animationBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_left, 0).toBundle()
            startActivity(intent, animationBundle)
        }

    }

}
