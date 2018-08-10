package utils

object PrintingUtils {
    fun align(l: Long): String {    //adding tabulation
        val shift = StringBuilder()
        for (i in 0 until l) {
            shift.append("\t")
        }
        return shift.toString()
    }
}