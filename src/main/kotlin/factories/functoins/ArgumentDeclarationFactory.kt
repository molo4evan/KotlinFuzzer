package factories.functoins

import exceptions.NotInitializedOptionException
import factories.Factory
import information.Symbol
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import ir.functions.ArgumentDeclaration
import ir.types.Type
import utils.ProductionParams
import utils.PseudoRandom

internal class ArgumentDeclarationFactory(private val ownerClass: Type?, private val argumentNumber: Int) : Factory<ArgumentDeclaration>() {

    override fun produce(): ArgumentDeclaration {
        val resultType = PseudoRandom.randomElement(TypeList.getAll())
        val resultName = "arg_$argumentNumber"
        val flags = ((if ((ProductionParams.disableFinalVariables?.value()?.not() ?: throw NotInitializedOptionException("disableFinalVariables")) && PseudoRandom.randomBoolean())
            VariableInfo.CONST
        else
            Symbol.NONE)
                or VariableInfo.LOCAL or VariableInfo.INITIALIZED)
        val v = VariableInfo(resultName, ownerClass, resultType, flags)
        SymbolTable.add(v)
        return ArgumentDeclaration(v)
    }
}