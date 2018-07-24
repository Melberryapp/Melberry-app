package com.melberry.data

import android.arch.lifecycle.LiveData

// Interface so ViewModel can choose if it wants to use Bratislava or Melbourne connection
// TODO zistiť či sa to tak robí
interface FirebaseConnectionInterface {
    fun getData(): LiveData<List<SpotDataClass>>
}