package factories.operators

import information.TypeList
import ir.operators.OperatorKind
import ir.types.Type
import utils.PseudoRandom
import utils.TypeUtil

class BinaryBitwiseOperatorFactory(
        opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : BinaryOperatorFactory(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type) = resultType == TypeList.INT || resultType == TypeList.LONG || resultType == TypeList.BOOLEAN

    override fun generateTypes(): Pair<Type, Type> {
        val castableFromResult = TypeUtil.getImplicitlyCastable(TypeList.getBuiltIn(), resultType)
        // built-in types less capacious than int are automatically casted to int in arithmetic.
        val leftType = PseudoRandom.randomElement(castableFromResult)
        val rightType = if (resultType.equals(TypeList.INT)) PseudoRandom.randomElement(castableFromResult) else resultType
        //TODO: is there sense to swap them randomly as it was done in original code?
        return if (PseudoRandom.randomBoolean()) Pair(leftType, rightType) else Pair(rightType, leftType)
    }
}
