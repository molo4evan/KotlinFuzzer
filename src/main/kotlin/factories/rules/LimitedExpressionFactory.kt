package factories.rules

import factories.rules.ExpressionFactory
import factories.utils.ProductionLimiter
import ir.IRNode
import ir.types.Type

class LimitedExpressionFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean,
        noAssignments: Boolean
) : ExpressionFactory(complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts, noAssignments) {

    override fun sproduce(): IRNode {
        ProductionLimiter.setLimit()
        try {
            return super.sproduce()
        } finally {
            ProductionLimiter.setUnlimited()
        }
    }
}