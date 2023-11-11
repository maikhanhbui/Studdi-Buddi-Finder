package com.group7.studdibuddi.ui.t1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class T2ViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is t2"
    }
    val text: LiveData<String> = _text
}