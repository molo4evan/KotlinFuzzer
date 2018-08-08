package factories.operators

import information.TypeList
import ir.operators.OperatorKind
import ir.types.Type
import utils.PseudoRandom
import utils.TypeUtil

internal class BinaryShiftOperatorFactory(
        opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : BinaryOperatorFactory(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type): Boolean {
        return resultType.equals(TypeList.INT) || resultType.equals(TypeList.LONG)
    }

    override fun generateTypes(): Pair<Type, Type> {
        val leftType = if (resultType == TypeList.INT){
            PseudoRandom.randomElement(TypeUtil.getImplicitlyCastable(TypeList.getBuiltInInt(), resultType))
        } else resultType
        val rightType = PseudoRandom.randomElement(TypeUtil.getImplicitlyCastable(TypeList.getBuiltInInt(), TypeList.LONG))
        return Pair(leftType, rightType)
    }
}
