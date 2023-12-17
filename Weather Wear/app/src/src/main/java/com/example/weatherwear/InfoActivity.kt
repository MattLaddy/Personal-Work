package com.example.weatherwear

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class InfoActivity : AppCompatActivity() {

    private lateinit var adView: AdView

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_main)

        val submitButton = findViewById<Button>(R.id.submitButton)

        adView = AdView(this)
        val adSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)

        val adUnitId = "ca-app-pub-3940256099942544/6300978111" // Use your actual ad unit ID
        adView.adUnitId = adUnitId

        val adLayout = findViewById<LinearLayout>(R.id.ad_view)
        adLayout.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        submitButton.setOnClickListener {
            val startTimePicker = findViewById<TimePicker>(R.id.timePicker1)
            val endTimePicker = findViewById<TimePicker>(R.id.timePicker2)

            val startHour = startTimePicker.hour
            val startMinute = startTimePicker.minute

            val endHour = endTimePicker.hour
            val endMinute = endTimePicker.minute

            val daysDifference = intent.getLongExtra("daysDifference", 0)
            val previous = intent.getSerializableExtra("prev", Previous::class.java)

            if (previous == null) {
                Log.w("MainActivity", "previous null in info")
            } else {
                Log.w("MainActivity", "weather = " + previous.getWeather())
            }

            val intent = Intent(this, Weather::class.java)
            intent.putExtra("start_hour", startHour)
            intent.putExtra("start_minute", startMinute)
            intent.putExtra("end_hour", endHour)
            intent.putExtra("end_minute", endMinute)
            intent.putExtra("daysDifference", daysDifference)
            intent.putExtra("prev", previous)

            val animationBundle = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in_and_scale, 0).toBundle()
            startActivity(intent, animationBundle)

        }
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

}
