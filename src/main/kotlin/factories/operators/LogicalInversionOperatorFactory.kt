package factories.operators

import factories.rules.ExpressionFactory
import information.TypeList
import ir.operators.OperatorKind
import ir.operators.UnaryOperator
import ir.types.Type

internal class LogicalInversionOperatorFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerType: Type?,
        exceptionSafe: Boolean,
        noconsts: Boolean,
        noAssignments: Boolean
) : UnaryOperatorFactory(OperatorKind.NOT, complexityLimit, operatorLimit, ownerType, TypeList.BOOLEAN, exceptionSafe, noconsts, noAssignments) {

    override fun isApplicable(resultType: Type) = resultType == TypeList.BOOLEAN

    override fun generateProduction(resultType: Type) = UnaryOperator(opKind, ExpressionFactory(
            complexityLimit - 1,
            operatorLimit - 1,
            ownerClass,
            TypeList.BOOLEAN,
            exceptionSafe,
            noconsts,
            noAssignments).produce())
}
