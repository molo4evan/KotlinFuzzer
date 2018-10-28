package factories.variables

import exceptions.ProductionFailedException
import factories.Factory
import information.Symbol
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import ir.types.Type
import ir.variables.VariableDeclaration
import utils.PseudoRandom

internal class VariableDeclarationFactory(private val ownerClass: Type?, private val isStatic: Boolean, private val isLocal: Boolean, private var resultType: Type) : Factory<VariableDeclaration>() {

    override fun produce(): VariableDeclaration {
        if (resultType == TypeList.UNIT) {
            val types = TypeList.getAll()
            PseudoRandom.shuffle(types)
            if (types.isEmpty()) {
                throw ProductionFailedException()
            }
            resultType = types[0]
        }
        val resultName = "var_" + SymbolTable.getNextVariableNumber()
        var flags = Symbol.NONE
        if (isStatic) {
            flags = flags or Symbol.STATIC
        }
        if (isLocal) {
            flags = flags or VariableInfo.LOCAL
        }
        val varInfo = VariableInfo(resultName, ownerClass, resultType, flags)
        SymbolTable.add(varInfo)
        return VariableDeclaration(varInfo)
    }
}