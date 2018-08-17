package information

import ir.types.Type
import utils.ProductionParams
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.reflect.KClass

object SymbolTable {                                                            //TODO: can't see all local vars, fix
    private val SYMBOL_STACK = Stack<MutableMap<Type, MutableList<Symbol>>>()
    private var VARIABLE_AMOUNT = 0
    private var FUNCTION_AMOUNT = 0

    private fun initExternalSymbols(){
        val classList = ProductionParams.addExternalSymbols?.value() ?: throw Exception("Option addExternalSymbols not initialized")
        if (classList == "all"){
            TypeList.getReferenceTypes().forEach(Type::exportSymbols)
        } else {
            val splittedList = classList.split(",")
            for (type in TypeList.getReferenceTypes()){
                for (str in splittedList){
                    if (type.typename == str){
                        type.exportSymbols()
                        break
                    }
                }
            }
        }
    }

    init {
        SYMBOL_STACK.push(mutableMapOf())
        if (ProductionParams.disableExternalSymbols?.value() == false){
            initExternalSymbols()
        }
    }

    fun add(symbol: Symbol?){
        val vars = SYMBOL_STACK.peek()
        if (symbol != null){
            if (!vars.containsKey(symbol.type)){
                vars[symbol.type] = mutableListOf()
            }
            vars[symbol.type]!!.add(symbol)
        }

    }

    fun remove(symbol: Symbol?){
        val vars = SYMBOL_STACK.peek()
        if (symbol != null) {
            if (vars.containsKey(symbol.type)) {
                val symbolsOfType = vars[symbol.type]
                symbolsOfType!!.remove(symbol)
                if (symbolsOfType.isEmpty()) {
                    vars.remove(symbol.type)
                }
            }
        }
    }

    fun get(type: Type): List<Symbol> {
        val vars = SYMBOL_STACK.peek()
        return if (vars.containsKey(type)) vars[type]!!
               else emptyList()
    }

    fun get(type: Type, classToCheck: KClass<*>): List<Symbol> {
        val vars = SYMBOL_STACK.peek()
        return if (vars.containsKey(type)) {
            vars[type]!!.stream().filter { classToCheck.isInstance(it) }.collect(Collectors.toList<Symbol>())
        } else emptyList()
    }

    fun get(classType: Type, type: Type, classToCheck: KClass<*>): List<Symbol> {
        val vars = SYMBOL_STACK.peek()
        if (vars.containsKey(type)){
            val result = mutableListOf<Symbol>()
            val symbols = vars[type]
            for (symbol in symbols!!){
                if (classToCheck.isInstance(symbol) && classType == symbol.owner){
                    result.add(symbol)
                }
            }
            return result
        }
        return emptyList()
    }

    fun get(name: String, classToCheck: KClass<*>): Symbol? {
        for (type in SYMBOL_STACK.peek().keys) {
            val symbolsOfType = SYMBOL_STACK.peek()[type]
            for (symbol in symbolsOfType!!) {
                if (classToCheck.isInstance(symbol) && name == symbol.name) {
                    return symbol
                }
            }
        }
        return null
    }

    fun getAll() = HashMap<Type, List<Symbol>>(SYMBOL_STACK.peek())

    fun getAll(classToCheck: KClass<*>): HashMap<Type, List<Symbol>> {
        val result = mutableMapOf<Type, MutableList<Symbol>>()

        for (type in SYMBOL_STACK.peek().keys){
            val symbolsOfType = SYMBOL_STACK.peek().get(type)
            for (symbol in symbolsOfType!!){
                if (classToCheck.isInstance(symbol)){
                    if (!result.containsKey(type)){
                        result[type] = mutableListOf()
                    }
                    result[type]!!.add(symbol)
                }
            }
        }
        return HashMap(result)
    }

    fun getAll(type: Type, classToCheck: KClass<*>): HashMap<Type, List<Symbol>> {
        val result = mutableMapOf<Type, MutableList<Symbol>>()

        for (typeClass in SYMBOL_STACK.peek().keys) {
            val symbolsOfType = SYMBOL_STACK.peek()[type]
            for (symbol in symbolsOfType!!) {
                if (classToCheck.isInstance(symbol) && typeClass == symbol.owner) {
                    if (!result.containsKey(type)) {
                        result[type] = mutableListOf()
                    }
                    result[type]!!.add(symbol)
                }
            }
        }

        return HashMap(result)
    }

    fun getAllCombined(): List<Symbol> {
        val result = mutableListOf<Symbol>()

        for (type in SYMBOL_STACK.peek().keys) {
            val symbolsOfType = SYMBOL_STACK.peek()[type]
            for (symbol in symbolsOfType!!) {
                result.add(symbol)
            }
        }

        return result
    }

    fun getAllCombined(classToCheck: KClass<*>): List<Symbol> {
        val result = mutableListOf<Symbol>()

        for (type in SYMBOL_STACK.peek().keys) {
            val symbolsOfType = SYMBOL_STACK.peek()[type]
            for (symbol in symbolsOfType!!) {
                if (classToCheck.isInstance(symbol)) {
                    result.add(symbol)
                }
            }
        }

        return result
    }

    fun getAllCombined(typeClass: Type?, classToCheck: KClass<*>): List<Symbol> {
        val result = mutableListOf<Symbol>()

        for (type in SYMBOL_STACK.peek().keys) {
            val symbolsOfType = SYMBOL_STACK.peek()[type]
            for (symbol in symbolsOfType!!) {
                if (classToCheck.isInstance(symbol) && typeClass == symbol.owner) {
                    result.add(symbol)
                }
            }
        }

        return result
    }

    fun getAllCombined(type: Type): List<Symbol> {
        val result = mutableListOf<Symbol>()

        for (t in SYMBOL_STACK.peek().keys) {
            val symbolsOfType = SYMBOL_STACK.peek()[t]
            for (symbol in symbolsOfType!!) {
                if (type == symbol.owner) {
                    result.add(symbol)
                }
            }
        }

        return result
    }

    fun getAllCombined(name: String, classToCheck: KClass<*>): List<Symbol> {
        val result = mutableListOf<Symbol>()

        for (type in SYMBOL_STACK.peek().keys) {
            val symbolsOfType = SYMBOL_STACK.peek()[type]
            for (symbol in symbolsOfType!!) {
                if (classToCheck.isInstance(symbol) && name == symbol.name) {
                    result.add(symbol)
                }
            }
        }

        return result
    }

    fun removeAll() {
        SYMBOL_STACK.clear()
        SYMBOL_STACK.push(mutableMapOf())
        VARIABLE_AMOUNT = 0
        FUNCTION_AMOUNT = 0
        if (ProductionParams.disableExternalSymbols?.value() == false) {
            initExternalSymbols()
        }
    }

    fun push() {
        // Do deep cloning..
        val prev = SYMBOL_STACK.peek()
        val top = mutableMapOf<Type, MutableList<Symbol>>()
        SYMBOL_STACK.push(top)
        for ((key, prevArray) in prev) {
            val topArray = mutableListOf<Symbol>()
            top[key] = topArray
            for (symbol in prevArray) {
                topArray.add(symbol.copy())
            }
        }
    }

    fun merge() {
        // Merging means moving element at the top of stack one step down, while removing the
        // previous element.
        val top = SYMBOL_STACK.pop()
        SYMBOL_STACK.pop()
        SYMBOL_STACK.push(top)
    }

    fun pop() {
        SYMBOL_STACK.pop()
    }

    fun getNextVariableNumber() =  ++VARIABLE_AMOUNT

    fun getNextFunctionNumber() = ++ FUNCTION_AMOUNT

    override fun toString() = SYMBOL_STACK.toString()
}