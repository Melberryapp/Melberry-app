package com.melberry.data

data class ParkingInterval(val description: String = "", // camel style can be used to match data from firebase
                           val effectiveonph: String = "",
                           val endtime: String = "0",
                           val fromday: String = "0",
                           val today: String = "0",
                           val starttime: String = "0",
                           val typedesc: String = "") {

    fun getFormattedStartTime(): String {
        return getFinalFormattedTime(starttime)
    }

    fun getFormattedEndTime(): String {
        return getFinalFormattedTime(endtime)
    }

    fun getFormattedToDay(): String {
        return getDayName(today.toInt())
    }

    fun getFormattedFromDay(): String {
        return getDayName(fromday.toInt())
    }

    private fun getDayName(dayNum: Int): String {
        return when (dayNum) {
            // TODO how to use string resources out of context?
            0 -> "Sun"
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thr"
            5 -> "Fri"
            else -> "Sat"
        }
    }

    private fun getFinalFormattedTime(time: String): String {
        val array = time.split(":")
        return if (array.size > 1){
            time.split(":")[0] + ":" + time.split(":")[1]
        } else ""
    }
}