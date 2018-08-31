package factories.operators.binary

import information.TypeList
import ir.operators.OperatorKind
import ir.types.Type

internal class BinaryShiftOperatorFactory(
        opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : BinaryOperatorFactory(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type) = resultType == TypeList.INT || resultType == TypeList.LONG

    override fun generateTypes() = Pair(resultType, TypeList.INT)
}
