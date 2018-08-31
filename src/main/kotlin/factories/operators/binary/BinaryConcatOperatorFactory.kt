package factories.operators.binary

import information.TypeList
import ir.operators.OperatorKind
import ir.types.Type

internal class BinaryConcatOperatorFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : BinaryOperatorFactory(OperatorKind.STRADD, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type): Boolean {
        return resultType == TypeList.STRING
    }

    override fun generateTypes(): Pair<Type, Type> {
        return Pair(resultType, resultType)
    }
}