package com.group7.studdibuddi.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class T3ViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is t3"
    }
    val text: LiveData<String> = _text
}