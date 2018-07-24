package com.melberry.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.melberry.utils.City

class CitiesViewModel(application: Application) : AndroidViewModel(application){

    val cityClicked = MutableLiveData<City>()
    val contactUsClicked = MutableLiveData<Int>()

    init {
        cityClicked.value = City.DEFAULT
    }

    fun test(){
        Log.d("onContinuePressed", "onContinuePressed")
    }

    fun onMelbourneCityClick(){
       cityClicked.value = City.MELBOURNE
    }

    fun onBratislavaCityClick(){
        cityClicked.value = City.BRATISLAVA
    }

    // Use live data to inform Fragment that
    fun onContactUsClick(){
        if (contactUsClicked.value == 1) contactUsClicked.value = 2
        else contactUsClicked.value = 1
    }
}