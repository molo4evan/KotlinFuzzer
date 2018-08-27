package factories.control_flow.loops

import factories.Factory
import factories.utils.IRNodeBuilder
import information.TypeList
import ir.IRNode
import ir.Literal
import ir.control_flow.loops.LoopingCondition
import ir.operators.BinaryOperator
import ir.operators.OperatorKind
import ir.types.Type
import ir.variables.LocalVariable
import utils.PseudoRandom

class LoopingConditionFactory(
        private val complexityLimit: Long,
        private val operatorLimit: Int,
        private val owner: Type?,
        private val counter: LocalVariable,
        private val limiter: Literal
): Factory<LoopingCondition>() {
    override fun produce(): LoopingCondition {
        var leftExpr: IRNode? = null
        var rightExpr: IRNode? = null

        val factory = IRNodeBuilder().
                setResultType(TypeList.BOOLEAN).
                setComplexityLimit((complexityLimit - 1) / 2).
                setOperatorLimit((operatorLimit - 1) / 2).
                setOwnerClass(owner).setExceptionSafe(false).
                setNoConsts(false).
                getLimitedExpressionFactory()

        if (PseudoRandom.randomBoolean()) leftExpr = factory.produce()
        if (PseudoRandom.randomBoolean()) rightExpr = factory.produce()
        // Depending on loop counter direction, we should synthesize limiting condition.
        // Example: If the counter is counting forward. Then the looping condition can be:
        // counter < n, counter <= n, n > counter, n >= counter, n - counter > 0, etc..

        // Just as a temporary solution we'll assume that the counter is monotonically increasing.
        // And use counter < n condition to limit the loop.
        // In future we may introduce other equivalent relations as well.
        var condition = BinaryOperator(OperatorKind.LT, TypeList.BOOLEAN, counter, limiter)
        condition = if (rightExpr != null) BinaryOperator(OperatorKind.AND, TypeList.BOOLEAN, condition, rightExpr) else condition  //may get infinite cycle?
        condition = if (leftExpr != null) BinaryOperator(OperatorKind.AND, TypeList.BOOLEAN, condition, leftExpr) else condition
        return LoopingCondition(condition)
    }
}