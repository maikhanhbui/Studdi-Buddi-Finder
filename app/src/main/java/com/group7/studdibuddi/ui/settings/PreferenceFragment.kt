package com.group7.studdibuddi.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.group7.studdibuddi.R

class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_layout, rootKey)

        //save distance unit preferences
        val manager: PreferenceManager = preferenceManager
        manager.sharedPreferencesName = "PREFERENCES"

        val sharedPreferences = requireContext().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("LANGUAGE_PREF", "en")
        println("debug: $language")

        val languagePreferences: ListPreference? = findPreference("LANGUAGE_PREF")
        languagePreferences?.let {
            if (language != null) {
                initLangPreferenceValue(language, it)
            }
            it.setOnPreferenceChangeListener { _, newValue ->
                handleChangeLanguage(newValue.toString())
                true
            }
        }
    }

    private fun initLangPreferenceValue(language: String, it: ListPreference) {
        val array = requireContext().resources.getStringArray(R.array.language_list_data)
        val langCode = when (language) {
            "fr" -> array[1]
            "ko" -> array[2]
            "vi" -> array[3]
            else -> array[0]
        }
        it.value = langCode.toString()
    }

    private fun handleChangeLanguage(newLanguage: String) {
        val array = requireContext().resources.getStringArray(R.array.language_list_data)
        val langCode = when (newLanguage) {
            array[1] -> "fr"
            array[2] -> "ko"
            array[3] -> "vi"
            else -> "en"
        }
        requireContext()
            .getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE).edit().apply {
                putString("LANGUAGE_PREF", langCode)
                apply()
            }
        requireActivity().recreate()
    }
}