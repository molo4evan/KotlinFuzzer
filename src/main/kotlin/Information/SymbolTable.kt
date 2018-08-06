package Information

import Exceptions.NotInitializedOptionException
import IR.Types.Type
import Settings.ProductionParams
import java.util.*

object SymbolTable {
    private val SYMBOL_STACK = Stack<HashMap<Type, ArrayList<Symbol>>>()
    private var VARIABLE_NUMBER = 0
    private var FUNCTION_NUMBER = 0

    private fun initExternalSymbols(){
       val classList: String = ProductionParams.addExternalSymbols?.value() ?: throw NotInitializedOptionException("addExternalSymbols")
        if (classList == "all"){
            TypeList.getReferenceTypes
        }
    }

}