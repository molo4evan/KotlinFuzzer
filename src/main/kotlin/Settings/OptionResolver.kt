package Settings


object OptionResolver {
    private val values = emptyMap<Option<*>, Any>()
    private val options = linkedMapOf<String, Option<*>>()

    fun parse(args: Array<String>) = parse(args, null)

    fun parse(args: Array<String>, propertyFileOption: Option<String>?){
        var pos = 0
        while (pos < args.size){
            val curArg = args[pos]
            if (curArg.startsWith("-")){
                val valueArg = //TODO: here i stopped
            }
        }
    }

    abstract class Option<T>(protected val shortName: Char, protected val longName: String, protected val defaultValue: T, protected val description: String){

        fun value() = values.getOrDefault(this, defaultValue) as T

        fun isSet() = values.containsKey(this)

        open fun isFlag() = false

        abstract fun parseFromString(arg: String): T
    }

    private class StringOption internal constructor(short: Char, long: String, default: String, description: String) : Option<String>(short, long, default, description) {

        override fun parseFromString(arg: String): String {
            return arg
        }
    }

    private class LongOption internal constructor(short: Char, long: String, default: Long, description: String) : Option<Long>(short, long, default, description) {

        override fun parseFromString(arg: String): Long {
            return java.lang.Long.valueOf(arg)
        }
    }

    private class IntOption internal constructor(short: Char, long: String, default: Int, description: String) : Option<Int>(short, long, default, description) {

        override fun parseFromString(arg: String): Int {
            return Integer.valueOf(arg)
        }
    }

    private class BooleanOption internal constructor(short: Char, long: String, default: Boolean, description: String) : Option<Boolean>(short, long, default, description) {

        override fun isFlag(): Boolean {
            return true
        }

        override fun parseFromString(arg: String): Boolean {
            //null and empty value is considered true, as option is flag and value could be absent
            return "" == arg || "1".equals(arg, ignoreCase = true) || "true".equals(arg, ignoreCase = true)
        }
    }

    fun getRegisteredOptions() = options.values.toSet()
}