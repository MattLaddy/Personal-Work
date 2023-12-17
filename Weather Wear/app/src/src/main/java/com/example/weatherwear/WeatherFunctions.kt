// WeatherFunctions.kt
package com.example.weatherwear

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

open class WeatherFunctions : AppCompatActivity() {

    private val apiKey = "WSMENTNNBZNJTVHZR4MGHACLN" // Replace with your API key
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherView: WeatherView

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        weatherView = WeatherView()

        requestWeatherForCurrentLocation()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestWeatherForCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val daysDifference = intent.getLongExtra("daysDifference", 0L)

                    GlobalScope.launch(Dispatchers.IO) {
                        val response =
                            get16DayWeatherForecast(location.latitude, location.longitude, apiKey, daysDifference)

                        launch(Dispatchers.Main) {
                            displayWeather(response)
                        }
                    }
                } else {
                    // Handle the case when location is null
                }
            }
    }

    private fun get16DayWeatherForecast(latitude: Double, longitude: Double, apiKey: String, daysDifference: Long): String {
        val apiUrl =
            "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$latitude,$longitude?key=$apiKey"

        return try {
            val response = URL(apiUrl).readText()
            Log.d("WeatherAPI", "API Response: $response")
            response
        } catch (e: Exception) {
            Log.e("WeatherAPI", "Error fetching weather", e)
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun displayWeather(response: String) {
        val tvWeatherDescription: TextView = findViewById(R.id.weatherDescription)
        val clothesDescription: TextView = findViewById(R.id.clothesDescription)
        val weatherIcon: ImageView = findViewById(R.id.weatherIcon)
        val daysDifference = intent.getLongExtra("daysDifference", 0L)
        val startHour1 = intent.getIntExtra("start_hour", 0)
        val endHour1 = intent.getIntExtra("end_hour", 0)

        val startHour = formatHour(startHour1)
        val endHour = formatHour(endHour1)

        try {
            val jsonObject = JSONObject(response)
            val cityName = jsonObject.getString("resolvedAddress")

            if (jsonObject.has("days")) {
                val daysArray = jsonObject.getJSONArray("days")
                val todayWeather = daysArray.getJSONObject(0)

                val currentWeather = todayWeather.getJSONArray("hours").getJSONObject(0)
                val description = currentWeather.getString("conditions")
                val tempMin = todayWeather.getDouble("tempmin")
                val tempMax = todayWeather.getDouble("tempmax")
                val conditions = currentWeather.getString("conditions")
                val icon = todayWeather.getString("icon")

                val weatherInfo = when {
                    startHour1 in 6..10 && endHour1 in 11..17 -> {
                        "When leaving for class in the morning, the temperature will be $tempMin°F.\n" +
                                "When coming home at $endHour, the temperature will be $tempMax°F."
                    }
                    startHour1 in 11..16 && endHour1 in 17..23 -> {
                        "When leaving for class in the midday, the temperature will be $tempMin°F.\n" +
                                "When coming home at $endHour, the temperature will be $tempMax°F."
                    }
                    startHour1 in 17..21 && endHour1 > 21 -> {
                        "When leaving for class in the evening, the temperature will be $tempMin°F.\n" +
                                "When coming home at $endHour, the temperature will be $tempMax°F."
                    }
                    else -> {
                        "Current Weather in $cityName:\n" +
                                "Description: $description\n" +
                                "Morning Temperature: $tempMin°F\n" +
                                "Peak Temp Temperature: $tempMax°F"
                    }
                }

                var previous = intent.getSerializableExtra("prev", Previous::class.java)
                weatherView.updateWeatherDescription(tvWeatherDescription, weatherInfo)
                previous?.setWeather(weatherInfo)
                previous?.setPreferences(this)

                var clothes = "Today you should wear " + pickClothes(tempMin) + " and consider packing " +
                        pickClothes(tempMax) + "."

                val rain = conditions.contains("rain")
                val snow = conditions.contains("snow")

                if ((rain || snow) && !(conditions.contains("no rain"))) {
                    if (rain && snow) {
                        clothes += "Bring your snow boots and rain jacket too!"
                    } else if (rain) {
                        clothes += "Bring your rain jacket too!"
                    } else {
                        clothes += "Bring your snow boots too!"
                    }
                }

                weatherView.updateClothesDescription(clothesDescription, clothes)
                previous?.setClothes(clothes)
                previous?.setPreferences(this)

                if (previous == null) {
                    Log.w("MainActivity", "previous null")
                }

                val iconResource = when (icon) {
                    "clear-day", "clear-night", "clear" -> R.drawable.clear_day_icon
                    "partly-cloudy-day", "partly-cloudy-night" -> R.drawable.partly_cloudy_icon
                    "snow" -> R.drawable.snow_icon
                    "rain" -> R.drawable.rain_icon
                    "fog" -> R.drawable.fog_icon
                    "wind" -> R.drawable.wind_icon
                    else -> R.drawable.cloudy_icon
                }

                weatherView.updateWeatherIcon(weatherIcon, iconResource)
            } else {
                Log.e("WeatherError", "Error fetching weather: 'days' key not found in JSON response")
                tvWeatherDescription.text = "Error fetching weather. Check log for details."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("WeatherError", "Error fetching weather", e)
            tvWeatherDescription.text = "Error fetching weather. Check log for details."
        }
    }

    private fun formatHour(hour: Int): String {
        val formattedHour = if (hour > 12) {
            (hour - 12).toString() + "pm"
        } else {
            if (hour == 12) {
                "12pm"
            } else {
                hour.toString() + "am"
            }
        }
        return formattedHour
    }

    private fun pickClothes(temp: Double): String {
        return if (temp >= 70) {
            "shorts and a t shirt"
        } else if (temp < 70 && temp >= 56) {
            "pants and a t shirt"
        } else if (temp < 60 && temp >= 45) {
            "a sweatshirt"
        } else if (temp < 45 && temp >= 30) {
            "a jacket"
        } else {
            "a winter jacket"
        }
    }
}
