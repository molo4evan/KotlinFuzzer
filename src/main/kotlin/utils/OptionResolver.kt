package utils

import java.io.FileReader
import java.util.*


object OptionResolver {
    private val values = mutableMapOf<Option<*>, Any>()
    private val options = mutableMapOf<String, Option<*>>()

    fun parse(args: Array<String>) = parse(args, null)

    fun parse(args: Array<String>, propertyFileOption: Option<String>?){
        var pos = 0
        while (pos < args.size){
            var curArg = args[pos]
            if (curArg.startsWith("-")){
                var valueArg: String? = null
                var opt = 0
                if (curArg.startsWith("--")){
                    opt = curArg.indexOf("=")
                    if (opt != -1){
                        valueArg = curArg.substring(opt + 1)
                        curArg = curArg.substring(0, opt)
                    }
                } else if (curArg.length > 2){
                    for (opt in 1 until curArg.length){
                        val key = curArg[opt]
                        val flagOpt = options["-$key"] ?: throw IllegalArgumentException("Unknown option: $key")

                        if (!flagOpt.isFlag()) throw IllegalArgumentException("Not a flag option: $key")

                        values[flagOpt] = true
                    }
                    pos++
                    continue
                }

                val curOpt = options[curArg] ?: throw IllegalArgumentException("Unknown option: $curArg")
                var value: Any? = null
                if (!curOpt.isFlag()){
                    if (valueArg == null){
                        pos++
                        if (pos < args.size){
                            valueArg = args[pos]
                        }
                    }
                }
                try {
                    value = curOpt.parseFromString(valueArg) ?: throw Exception()
                }
                catch (e: Exception) { throw IllegalArgumentException("Error parsing $valueArg, option $curArg", e) }
                values[curOpt] = value
            }
            pos++
        }

        if (propertyFileOption != null && values.containsKey(propertyFileOption)){
            parseProperties(propertyFileOption.value())
        }
    }

    private fun parseProperties(fileName: String) {
        val properties = Properties()
        properties.load(FileReader(fileName))

        for (optionName in properties.stringPropertyNames()){
            val curOpt = options["--$optionName"] ?: throw IllegalArgumentException("Unknown option in property file $optionName")
            val propValue = properties.getProperty(optionName)
            try {
                values.putIfAbsent(curOpt, curOpt.parseFromString(propValue) ?: throw Exception())
            } catch (ex: Exception){
                throw IllegalArgumentException("Error parsing $propValue, property $optionName", ex)
            }
        }
    }


    fun addIntOption(key: Char?, name: String, default: Int, description: String): Option<Int>{
        val opt = IntOption(key, name, default, description)
        register(opt)
        return opt
    }

    fun addLongOption(key: Char?, name: String, defaultValue: Long, description: String): Option<Long> {
        val option = LongOption(key, name, defaultValue, description)
        register(option)
        return option
    }

    fun addStringOption(key: Char?, name: String, defaultValue: String, description: String): Option<String> {
        val option = StringOption(key, name, defaultValue, description)
        register(option)
        return option
    }

    fun addBooleanOption(key: Char?, name: String, defaultValue: Boolean, description: String): Option<Boolean> {
        val option = BooleanOption(key, name, defaultValue, description)
        register(option)
        return option
    }

    fun addIntOption(name: String, defaultValue: Int, description: String): Option<Int> {
        return addIntOption(null, name, defaultValue, description)
    }

    fun addStringOption(name: String, defaultValue: String, description: String): Option<String> {
        return addStringOption(null, name, defaultValue, description)
    }

    fun addBooleanOption(name: String, description: String): Option<Boolean> {
        return addBooleanOption(null, name, false, description)
    }


    private fun register(option: Option<*>){
        if (options.put("--${option.longName}", option) != null) {
            throw RuntimeException("Option is already registered for key ${option.longName}")
        }
        if (option.shortName != null && options.put("-${option.shortName}", option) != null){
            throw  RuntimeException("Option is already registered for key ${option.shortName}")
        }
    }


    abstract class Option<T>(
            val shortName: Char?,
            val longName: String,
            val defaultValue: T,
            val description: String
    ){

        fun value() = values.getOrDefault(this, defaultValue) as T

        fun isSet() = values.containsKey(this)

        open fun isFlag() = false

        abstract fun parseFromString(arg: String?): T
    }

    private class StringOption internal constructor(
            short: Char?,
            long: String,
            default: String,
            description: String
    ) : Option<String>(short, long, default, description) {

        override fun parseFromString(arg: String?): String {
            return arg ?: throw IllegalArgumentException("No string")
        }
    }

    private class LongOption internal constructor(
            short: Char?,
            long: String,
            default: Long,
            description: String
    ) : Option<Long>(short, long, default, description) {

        override fun parseFromString(arg: String?): Long {
            return java.lang.Long.valueOf(arg)
        }
    }

    private class IntOption internal constructor(
            short: Char?,
            long: String,
            default: Int,
            description: String
    ) : Option<Int>(short, long, default, description) {

        override fun parseFromString(arg: String?): Int {
            return Integer.valueOf(arg)
        }
    }

    private class BooleanOption internal constructor(
            short: Char?,
            long: String,
            default: Boolean,
            description: String
    ) : Option<Boolean>(short, long, default, description) {

        override fun isFlag(): Boolean {
            return true
        }

        override fun parseFromString(arg: String?): Boolean {
            //null and empty value is considered true, as option is flag and value could be absent
            return "" == arg || "1".equals(arg, ignoreCase = true) || "true".equals(arg, ignoreCase = true)
        }
    }

    fun getRegisteredOptions() = options.values.toSet()
}