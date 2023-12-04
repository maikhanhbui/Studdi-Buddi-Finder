package com.group7.studdibuddi

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PinViewModel: ViewModel() {
    val userImage = MutableLiveData<Bitmap>()
}