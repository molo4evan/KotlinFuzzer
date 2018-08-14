package factories.operators

import information.TypeList
import ir.operators.OperatorKind
import ir.operators.UnaryOperator
import ir.types.Type

class TypeCheckOperatorFactory(     //TODO: how to do it?
        complexityLimit: Long,
        operatorLimit: Int,
        owner: Type?,
        exceptionSafe: Boolean,
        noConsts: Boolean
): UnaryOperatorFactory(OperatorKind.TYPE_CHECK, complexityLimit, operatorLimit, owner, TypeList.BOOLEAN, exceptionSafe, noConsts) {
    override fun isApplicable(resultType: Type) = resultType == TypeList.BOOLEAN

    override fun generateProduction(type: Type): UnaryOperator {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}