package factories.operators

import factories.utils.IRNodeBuilder
import information.TypeList
import ir.operators.OperatorKind
import ir.operators.UnaryOperator
import ir.types.Type
import utils.PseudoRandom
import utils.TypeUtil

internal class BitwiseInversionOperatorFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : UnaryOperatorFactory(OperatorKind.BIT_NOT, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type): Boolean {
        return resultType == TypeList.INT || resultType == TypeList.LONG
    }

    override fun generateType(): Type {
        return if (resultType == TypeList.INT) {
            PseudoRandom.randomElement(TypeUtil.getImplicitlyCastable(TypeList.getBuiltIn(), resultType))
        } else {
            resultType
        }
    }

    override fun generateProduction(type: Type): UnaryOperator {
        return UnaryOperator(opKind, IRNodeBuilder.setComplexityLimit(complexityLimit - 1)
                .setOperatorLimit(operatorLimit - 1)
                .setOwnerClass(ownerClass)
                .setResultType(type)
                .setExceptionSafe(exceptionSafe)
                .setNoConsts(noconsts)
                .getExpressionFactory()
                .produce())
    }
}
