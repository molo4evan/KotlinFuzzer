package factories.variables

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.SafeFactory
import factories.rules.Rule
import factories.utils.IRNodeBuilder
import information.Symbol
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import ir.IRNode
import ir.types.Type
import ir.variables.VariableInitialization
import utils.ProductionParams
import utils.PseudoRandom

internal class VariableInitializationFactory(
        private val ownerClass: Type?,
        private val constant: Boolean,
        private val isStatic: Boolean,
        private val isLocal: Boolean,
        private val complexityLimit: Long,
        private val operatorLimit: Int,
        private val exceptionSafe: Boolean
) : SafeFactory<VariableInitialization>() {

    @Throws(ProductionFailedException::class)
    override fun sproduce(): VariableInitialization {
        val types = TypeList.getAll()
        PseudoRandom.shuffle(types)
        if (types.isEmpty()) {
            throw ProductionFailedException()
        }
        val resultType = types[0]
        val b = IRNodeBuilder().setComplexityLimit(complexityLimit - 1)
                .setOperatorLimit(operatorLimit - 1)
                .setOwnerClass(ownerClass)
                .setResultType(resultType)
                .setExceptionSafe(exceptionSafe)
                .setNoConsts(false)
        val rule = Rule<IRNode>("initializer")
        rule.add("literal_initializer", b.getLiteralFactory())
        if (ProductionParams.disableExprInInit?.value()?.not() ?: throw NotInitializedOptionException("disableExprInInit")) {
            rule.add("expression", b.getLimitedExpressionFactory())
        }
        var thisSymbol: Symbol? = null
        if (isStatic) {
            thisSymbol = SymbolTable.get("this", VariableInfo::class)
            SymbolTable.remove(thisSymbol)
        }
        val init: IRNode
        try {
            init = rule.produce()
        } finally {
            if (isStatic) {
                SymbolTable.add(thisSymbol)
            }
        }
        val resultName = "var_" + SymbolTable.getNextVariableNumber()
        var flags = VariableInfo.INITIALIZED
        if (constant) {
            flags = flags or VariableInfo.CONST
        }
        if (isStatic) {
            flags = flags or Symbol.STATIC          //TODO: incorrect
        }
        if (isLocal) {
            flags = flags or VariableInfo.LOCAL
        }
        val varInfo = VariableInfo(resultName, ownerClass, resultType, flags)
        SymbolTable.add(varInfo)
        return VariableInitialization(varInfo, init)
    }
}