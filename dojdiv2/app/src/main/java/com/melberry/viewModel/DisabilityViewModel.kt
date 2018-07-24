package com.melberry.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import com.melberry.R

class DisabilityViewModel(application: Application) : AndroidViewModel(application) {

    var image = MutableLiveData<Drawable>()
    var disabilityPreference = MutableLiveData<String>()
    var isOn = MutableLiveData<Boolean>()

    var buttonPressed = MutableLiveData<Int>()

    init {
        if (isDisableOn()) {
            image.value = getApplication<Application>().getDrawable(R.drawable.disable_on)
            disabilityPreference.value = getApplication<Application>().getString(R.string.on)
            isOn.value = true
        } else {
            image.value = getApplication<Application>().getDrawable(R.drawable.disable_off)
            disabilityPreference.value = getApplication<Application>().getString(R.string.off)
            isOn.value = false
        }

        buttonPressed.value = 1
    }

    // Change style after switch click
    fun onSwitchClick() {
        if (!isOn.value!!) {
            image.value = getApplication<Application>().getDrawable(R.drawable.disable_on)
            disabilityPreference.value = getApplication<Application>().getString(R.string.on)
            isOn.value = true
            savePreferences(isOn.value)
        } else {
            image.value = getApplication<Application>().getDrawable(R.drawable.disable_off)
            disabilityPreference.value = getApplication<Application>().getString(R.string.off)
            isOn.value = false
            savePreferences(isOn.value)
        }
    }

    // Inform the fragment that save button is clicked, so user flow can continue
    fun onSaveButtonClick() {
        if (buttonPressed.value == 1) buttonPressed.value = 2
    }

    private fun savePreferences(disabilityPreference: Boolean?) {
        val sharedPreferences: SharedPreferences = getApplication<Application>().getSharedPreferences(getApplication<Application>().applicationContext.getString(R.string.disability_key), Context.MODE_PRIVATE)
                ?: return

        with(sharedPreferences.edit()) {
            putBoolean(getApplication<Application>().applicationContext.getString(R.string.disability_key), disabilityPreference!!)
            apply()
        }

    }

    private fun isDisableOn(): Boolean {
        val sharedPref = getApplication<Application>().getSharedPreferences(getApplication<Application>().applicationContext.getString(R.string.disability_key), Context.MODE_PRIVATE)
        return sharedPref.getBoolean(getApplication<Application>().applicationContext.getString(R.string.disability_key), false)
    }

}