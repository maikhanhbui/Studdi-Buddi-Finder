package com.group7.studdibuddi.ui.settings

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

object LocaleService {
    private var LANGUAGE_PREFERENCE_KEY = "LANGUAGE_PREF"
    private var THEME_PREFERENCE_KEY = "THEME_PREF"

    fun updateBaseContextLocale(context: Context): Context? {
        val sharedPreferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(LANGUAGE_PREFERENCE_KEY, "en")
        val locale = language?.let { Locale(it) }
        if (locale != null) {
            Locale.setDefault(locale)
        }
        return locale?.let { updateResourcesLocaleLegacy(context, it) }
    }

    private fun updateResourcesLocaleLegacy(
        context: Context,
        locale: Locale
    ): Context? {
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        updateAppTheme(context)
        return context
    }

    private fun updateAppTheme(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)
        when (sharedPreferences.getString(THEME_PREFERENCE_KEY, "day")) {
            "day" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }
//    online resources used
}