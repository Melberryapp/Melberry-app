package com.melberry.utils

import android.databinding.BindingAdapter
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import android.graphics.Typeface
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView

// DataBinding helpers for data binding - used even that AS tells they are not
@BindingAdapter("bottomSheetState")
fun bottomSheetState(view: View, @BottomSheetBehavior.State state: Int) {
    Log.d("BottomSheetState", state.toString())
    BottomSheetBehavior.from(view).state = state
}

@BindingAdapter("bottomSheetStateChange")
fun bottomSheetStateChange(view: View, callback: BottomSheetBehavior.BottomSheetCallback) {
    BottomSheetBehavior.from(view).setBottomSheetCallback(callback)
}

@BindingAdapter("android:onProgressChanged")
fun onProgressChanged(view: SeekBar, listener: SeekBar.OnSeekBarChangeListener) {
    view.setOnSeekBarChangeListener(listener)
}


@android.databinding.BindingAdapter("android:typeface")
fun setTypeface(v: TextView, style: String) {
    when (style) {
        "bold" -> v.setTypeface(null, Typeface.BOLD)
        else -> v.setTypeface(null, Typeface.NORMAL)
    }
}