package factories.variables

import exceptions.ProductionFailedException
import factories.Factory
import information.SymbolTable
import information.VariableInfo
import ir.types.Type
import ir.variables.LocalVariable
import utils.PseudoRandom

internal class LocalVariableFactory(private val type: Type, private val flags: Int) : Factory<LocalVariable>() {

    override fun produce(): LocalVariable {
        // Get the variables of the requested type from SymbolTable
        val allVariables = ArrayList(SymbolTable.get(type, VariableInfo::class))
        if (!allVariables.isEmpty()) {
            PseudoRandom.shuffle(allVariables)
            for (symbol in allVariables) {
                val varInfo = symbol as VariableInfo
                if (varInfo.flags and VariableInfo.CONST == flags and VariableInfo.CONST
                        && varInfo.flags and VariableInfo.INITIALIZED == flags and VariableInfo.INITIALIZED
                        && varInfo.flags and VariableInfo.LOCAL > 0) {
                    return LocalVariable(varInfo)
                }
            }
        }
        throw ProductionFailedException()
    }
}