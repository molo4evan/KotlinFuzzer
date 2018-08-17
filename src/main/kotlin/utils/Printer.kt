package utils

import java.util.Stack

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
        return print_r(Stack(), arg)
    }

    private fun print_r(visitedObjects: Stack<Any>, arg: Any?): String {
        var result = ""
        when {
            arg == null -> result += "null"
            arg.javaClass.isArray -> {
                for (i in visitedObjects.indices) {
                    if (visitedObjects.elementAt(i) === arg) {
                        return "<recursive>"
                    }
                }

                visitedObjects.push(arg)

                val delimiter = ", "
                result += "["

                when (arg) {
                    is Array<*> -> {
                        for (i in arg.indices) {
                            result += print_r(visitedObjects, arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
                    }
                    is BooleanArray -> {
                        for (i in arg.indices) {
                            result += print(arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
                    }
                    is ByteArray -> {
                        for (i in arg.indices) {
                            result += print(arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
                    }
                    is ShortArray -> {
                        for (i in arg.indices) {
                            result += print(arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
                    }
                    is CharArray -> {
                        for (i in arg.indices) {
                            result += print(arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
                    }
                    is IntArray -> {
                        for (i in arg.indices) {
                            result += print(arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
                    }
                    is LongArray -> {
                        for (i in arg.indices) {
                            result += print(arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
                    }
                    is FloatArray -> {
                        for (i in arg.indices) {
                            result += print(arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
                    }
                    is DoubleArray -> {
                        for (i in arg.indices) {
                            result += print(arg[i])
                            if (i < arg.size - 1) {
                                result += delimiter
                            }
                        }
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