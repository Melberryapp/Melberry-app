package com.melberry.data

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Singleton connection to get interval date for clicked spot
object FirebaseIntervals {

    private val TAG = this::class.java.simpleName

    private val intervalsRef = FirebaseDatabase.getInstance().getReference("intervals")
    val data: MutableLiveData<List<ParkingInterval>> = MutableLiveData()

    fun getIntervals(id: String) {
        val bayIntervalsRef = intervalsRef.child(id)
        bayIntervalsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, dataSnapshot.toString())

                val list: MutableList<ParkingInterval> = mutableListOf()
                dataSnapshot.children.mapNotNullTo(list) { it.getValue<ParkingInterval>(ParkingInterval::class.java) }
                data.value = list
                Log.d(TAG, data.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read value.", error.toException())
            }
        })
    }
}