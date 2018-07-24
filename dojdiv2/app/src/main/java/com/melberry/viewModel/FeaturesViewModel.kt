package com.melberry.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.melberry.R
import com.melberry.utils.OnboardingStateValue


class FeaturesViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        @BindingAdapter("android:src")
        @JvmStatic
        fun setImageDrawable(view: ImageView, drawable: Drawable) {
            view.setImageDrawable(drawable)
        }
    }

    private var onboardingState: OnboardingStateValue = OnboardingStateValue.ONBOARDING_REAL_TIME
    var notifyActivity = MutableLiveData<Int>()

    var image = MutableLiveData<Drawable>()
    var title = MutableLiveData<String>()
    var description = MutableLiveData<String>()
    var buttonLabel = MutableLiveData<String>()
    var gulicky = MutableLiveData<Drawable>()

    init {
        notifyActivity.value = 1
        realTime()
    }

    fun onContinuePressed() {
        when (onboardingState) {
            OnboardingStateValue.ONBOARDING_REAL_TIME -> filter()
            OnboardingStateValue.ONBOARDING_FILTER -> priceAndRestrictions()
            else -> {
                if (notifyActivity.value == 1) notifyActivity.value = 2
            }
        }
    }

    private fun realTime() {
        image.value = getApplication<Application>().getDrawable(R.drawable.onboarding_real_time_1_screen)
        title.value = getApplication<Application>().getString(R.string.realtime_title)
        description.value = getApplication<Application>().getString(R.string.realtime_description)
        buttonLabel.value = getApplication<Application>().getString(R.string.next_button_label)
        gulicky.value = getApplication<Application>().getDrawable(R.drawable.gulicky1)

        onboardingState = OnboardingStateValue.ONBOARDING_REAL_TIME
    }

    private fun filter() {
        image.value = getApplication<Application>().getDrawable(R.drawable.onboarding_filter_2_screen)
        title.value = getApplication<Application>().getString(R.string.filter_title)
        description.value = getApplication<Application>().getString(R.string.filter_description)
        buttonLabel.value = getApplication<Application>().getString(R.string.next_button_label)
        gulicky.value = getApplication<Application>().getDrawable(R.drawable.gulicky2)

        onboardingState = OnboardingStateValue.ONBOARDING_FILTER
    }

    private fun priceAndRestrictions() {
        image.value = getApplication<Application>().getDrawable(R.drawable.onboarding_price_and_restrictions_3_screen)
        title.value = getApplication<Application>().getString(R.string.price_and_restrictions_title)
        description.value = getApplication<Application>().getString(R.string.price_and_restrictions_description)
        buttonLabel.value = getApplication<Application>().getString(R.string.got_it_button_label)
        gulicky.value = getApplication<Application>().getDrawable(R.drawable.gulicky3)

        onboardingState = OnboardingStateValue.ONBOARDING_PRICE_AND_RESTRICTIONS
    }

    fun onBackPressed(): Boolean {
        return when (onboardingState) {
            OnboardingStateValue.ONBOARDING_PRICE_AND_RESTRICTIONS -> {
                filter()
                true
            }
            OnboardingStateValue.ONBOARDING_FILTER -> {
                realTime()
                true
            }
            else -> false
        }
    }
}