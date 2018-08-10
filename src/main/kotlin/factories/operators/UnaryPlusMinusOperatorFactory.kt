package factories.operators

import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import information.TypeList
import ir.operators.OperatorKind
import ir.operators.UnaryOperator
import ir.types.Type
import utils.PseudoRandom
import utils.TypeUtil

internal class UnaryPlusMinusOperatorFactory(
        opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean,
        noAssignments: Boolean
) : UnaryOperatorFactory(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts, noAssignments) {

    override fun isApplicable(resultType: Type): Boolean {
        if (!resultType.isBuiltIn() || resultType == TypeList.BOOLEAN) {
            return false
        }
        return resultType == TypeList.INT
    }

    override fun generateType(): Type {
        return if (resultType == TypeList.INT) {
            PseudoRandom.randomElement(TypeUtil.getImplicitlyCastable(TypeList.getBuiltIn(), resultType))
        } else {
            resultType
        }
    }

    override fun generateProduction(type: Type): UnaryOperator {
        return UnaryOperator(opKind, IRNodeBuilder()
                .setComplexityLimit(complexityLimit)
                .setOperatorLimit(operatorLimit)
                .setOwnerClass(ownerClass)
                .setResultType(type)
                .setExceptionSafe(exceptionSafe)
                .setNoConsts(noconsts)
                .getExpressionFactory()
                .produce())
    }
}