package com.example.weatherwear

import android.widget.ImageView
import android.widget.TextView

class WeatherView {

    fun updateWeatherDescription(textView: TextView, weatherInfo: String) {
        textView.text = weatherInfo
    }

    fun updateClothesDescription(textView: TextView, clothes: String) {
        textView.text = clothes
    }

    fun updateWeatherIcon(imageView: ImageView, iconResource: Int) {
        imageView.setImageResource(iconResource)
    }

}
