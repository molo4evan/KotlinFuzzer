package factories.operators.binary

import information.TypeList
import ir.operators.OperatorKind
import ir.types.Type
import utils.PseudoRandom
import utils.TypeUtil

internal class BinaryArithmeticOperatorFactory(opKind: OperatorKind, complexityLimit: Long, operatorLimit: Int,
                                               ownerClass: Type?, resultType: Type, exceptionSafe: Boolean, noconsts: Boolean) : BinaryOperatorFactory(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type) = if (resultType.isBuiltIn()) resultType == TypeList.INT else false

    override fun generateTypes(): Pair<Type, Type> {
        val castableFromResultType = TypeUtil.getImplicitlyCastable(TypeList.getBuiltIn(), resultType)
        // built-in types less capacious than int are automatically casted to int in arithmetic.
        val leftType = PseudoRandom.randomElement(castableFromResultType)
        val rightType = if (resultType == TypeList.INT)
            PseudoRandom.randomElement(castableFromResultType)
        else
            resultType
        return if (PseudoRandom.randomBoolean()) Pair(leftType, rightType) else Pair(rightType, leftType)
    }
}
