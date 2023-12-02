package com.group7.studdibuddi.ui.settings

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleService {
    private var PREFERENCE_KEY = "LANGUAGE_PREF"

    fun updateBaseContextLocale(context: Context): Context? {
        val sharedPreferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(PREFERENCE_KEY, "en")
        val locale = language?.let { Locale(it) }
        Locale.setDefault(locale)
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
        return context
    }
}