package com.group7.studdibuddi.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.group7.studdibuddi.ui.settings.LocaleService

open class BaseActivity: AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleService.updateBaseContextLocale(newBase))
    }

}