package factories.operators

import information.TypeList
import ir.operators.OperatorKind
import ir.types.Type
import utils.PseudoRandom

class BinaryComparsionOperatorFactory(
        opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : BinaryOperatorFactory(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type) = resultType == TypeList.BOOLEAN

    override fun generateTypes(): Pair<Type, Type> {
        val builtInExceptBoolean = ArrayList(TypeList.getBuiltIn())
        builtInExceptBoolean.remove(TypeList.BOOLEAN)
        return Pair(PseudoRandom.randomElement(builtInExceptBoolean),
                PseudoRandom.randomElement(builtInExceptBoolean))
    }
}