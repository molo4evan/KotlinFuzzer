package factories.operators.unary

import factories.utils.IRNodeBuilder
import information.TypeList
import ir.operators.OperatorKind
import ir.operators.UnaryOperator
import ir.types.Type

internal class IncDecOperatorFactory(
        opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        owner: Type?,
        resultType: Type,
        safe: Boolean,
        noconsts: Boolean
) : UnaryOperatorFactory(opKind, complexityLimit, operatorLimit, owner, resultType, safe, noconsts) {

    override fun isApplicable(resultType: Type): Boolean {
        return resultType.isBuiltIn() && resultType != TypeList.BOOLEAN
    }

    override fun generateProduction(type: Type): UnaryOperator {
        return UnaryOperator(opKind, IRNodeBuilder().setComplexityLimit(complexityLimit - 1)
                .setOperatorLimit(operatorLimit - 1)
                .setOwnerClass(ownerClass)
                .setResultType(type)
                .setIsConstant(false)
                .setIsInitialized(true)
                .setExceptionSafe(exceptionSafe)
                .setNoConsts(noConsts)
                .getVariableFactory()
                .produce())
    }
}