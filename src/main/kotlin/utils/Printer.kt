package utils

fun <T> MutableList<T>.push(elem: T) = add(elem)

fun <T> MutableList<T>.pop() = removeAt(size - 1)

object Printer {

    fun print(arg: Boolean): String {
        return arg.toString()
    }

    fun print(arg: Byte): String {
        return arg.toString()
    }

    fun print(arg: Short): String {
        return arg.toString()
    }

    fun print(arg: Char): String {
        return arg.toInt().toString()
    }

    fun print(arg: Int): String {
        return arg.toString()
    }

    fun print(arg: Long): String {
        return arg.toString()
    }

    fun print(arg: Float): String {
        return arg.toString()
    }

    fun print(arg: Double): String {
        return arg.toString()
    }

    fun print(arg: Any): String {
        return print_r(mutableListOf(), arg)
    }

    private fun print_r(visitedObjects: MutableList<Any>, arg: Any?): String {
        var result = ""
        when (arg) {
            null -> result += "null"
            is Array<*> -> {
                for (i in visitedObjects.indices) {
                    if (visitedObjects.elementAt(i) == arg) {
                        return "<recursive>"
                    }
                }

                visitedObjects.push(arg)

                val delimiter = ", "
                result += "["

                for (i in arg.indices) {
                    result += print_r(visitedObjects, arg[i])
                    if (i < arg.size - 1) {
                        result += delimiter
                    }
                }

                result += "]"
                visitedObjects.pop()

            }
            else -> result += arg.toString()
        }

        return result
    }
}