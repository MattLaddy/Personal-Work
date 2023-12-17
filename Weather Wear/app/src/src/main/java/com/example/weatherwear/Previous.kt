package com.example.weatherwear

import android.content.Context
import android.content.SharedPreferences
import java.io.Serializable

class Previous : Serializable {
    private var weatherDescription : String? = ""
    private var clothesDescription : String? = ""

    constructor(context : Context){
        var pref : SharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        setWeather(pref.getString("weather", "Weather: N/A"))
        setClothes(pref.getString("clothes", "Clothes: N/A"))
    }

    fun setWeather(weather : String?){
        weatherDescription = weather
    }

    fun setClothes(clothes : String?){
        clothesDescription = clothes
    }

    fun getWeather() : String? {
        return weatherDescription
    }

    fun getClothes() : String? {
        return clothesDescription
    }

    fun setPreferences(context : Context){
        var pref : SharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        var editor : SharedPreferences.Editor = pref.edit()
        editor.putString("weather", weatherDescription)
        editor.putString("clothes", clothesDescription)
        editor.commit()
    }
}