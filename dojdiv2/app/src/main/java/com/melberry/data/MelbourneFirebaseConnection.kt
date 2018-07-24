package com.melberry.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

// Singleton class for connecting to firebase database full of data from melbourne
// Melbourne has to be in separated class because "object" singleton can not take an argument
object MelbourneFirebaseConnection : FirebaseConnectionInterface {

    private val TAG = this::class.java.simpleName

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("spots")

    private val data: MutableLiveData<List<SpotDataClass>> = object : MutableLiveData<List<SpotDataClass>>() {
        override fun onActive() {

            myRef.addValueEventListener(initListener)
            myRef.addChildEventListener(updatedDataListener)
        }

        override fun onInactive() {
            myRef.removeEventListener(initListener)
            myRef.removeEventListener(updatedDataListener)
        }
    }

    // Init listener is used for first load of spots from database
    // then is removed so all data goes one after one
    private val initListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // To avoid updates, unregister after initial call.
            myRef.removeEventListener(this)

            Log.d(TAG, "Value is: $dataSnapshot")

            // Coroutine runs mapping to object in different thread than UI thread so progress bar will be working
            launch {
                val list = async {
                    val list: MutableList<SpotDataClass> = mutableListOf()
                    dataSnapshot.children.mapNotNullTo(list) { it.getValue<SpotDataClass>(SpotDataClass::class.java) }
                    list
                }.await()
                data.postValue(list)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Failed to read value
            Log.e(TAG, "Failed to read value.", error.toException())
        }


    }

    // After initial load of spots, this listener is used to get updated spots one after one
    private val updatedDataListener = object : ChildEventListener {
        override fun onCancelled(dataError: DatabaseError) {}

        override fun onChildMoved(dataShnapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.d(TAG, "Changed: $dataSnapshot")
            data.value = listOf(dataSnapshot.getValue<SpotDataClass>(SpotDataClass::class.java)!!)
        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            Log.d(TAG, "Removed: $dataSnapshot")
            // TODO update spots when spot is removed from DB during running the app
        }
    }

    override fun getData(): LiveData<List<SpotDataClass>> {
        return data
    }
}