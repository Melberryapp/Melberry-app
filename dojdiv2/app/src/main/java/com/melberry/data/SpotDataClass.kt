package com.melberry.data

data class SpotDataClass(
        val disabilityext: String = "1000", // camel style can be used to match data from firebase
        private val duration: String = "1000",
        val infoBayId: String = "",
        val lat: String = "0.0",
        val lng: String = "0.0",
        val markerId: String = "",
        val status: String = "",
        private val typedesc: String = "") {

    fun getDuration(isDisabled: Boolean): Int {

        return if (duration == "1001" || disabilityext == "1001") 0
        else {
            if (isDisabled) {
                disabilityext.toInt()
            } else
                duration.toInt()
        }

    }

    fun getCurrentTypeDesc(): String {
        // TODO how to use string resources out of context?
        return if (typedesc == "") "Unrestricted - check details"
        else typedesc
    }

}
