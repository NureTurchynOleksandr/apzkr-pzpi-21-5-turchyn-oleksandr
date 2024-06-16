package com.example.foodautoplacer.activities.basic

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.example.foodautoplacer.R
import com.example.foodautoplacer.auxiliary_tools.RequestManager
import java.util.*
import kotlin.properties.Delegates

open class GeneralApplicationActivity: AppCompatActivity() {
    lateinit var currentLocale: String
    protected val requestManager = RequestManager()
    lateinit var requestQueue: RequestQueue
    var activityMenu by Delegates.notNull<Int>()

    fun startNewActivity(cls: Class<*>) {
        val componentCatalogIntent = Intent(this, cls)
        startActivity(componentCatalogIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(activityMenu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMenu = R.menu.activity_without_top_menu_menu
        currentLocale = resources.configuration.locale.toString()
    }

    override fun onResume() {
        val locale = resources.configuration.locale.toString()
        if (locale != currentLocale) {
            finish()
            startActivity(intent)
        }
        super.onResume()
    }

    fun eraseAccessToken() {
        val prefs = getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putString("access_token", "")
        edit.apply()
    }

    fun changeUserInterfaceLanguage() {
        currentLocale = if (currentLocale == "en") "uk" else "en"
        val locale = Locale(currentLocale)
        Locale.setDefault(locale)
        val resources: Resources = this.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        finish()
        startActivity(intent)
    }
}