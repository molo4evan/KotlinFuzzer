package factories.rules

import factories.Factory
import factories.utils.IRNodeBuilder
import factories.utils.ProductionLimiter
import information.TypeList
import ir.IRNode
import ir.Statement
import utils.PseudoRandom
import ir.types.Type
import utils.ProductionParams

class StatementFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : Factory<Statement>() {
    private val rule: Rule<IRNode> = Rule("statement")

    init {
        val builder = IRNodeBuilder()
                .setComplexityLimit(complexityLimit)
                .setOperatorLimit(operatorLimit)
                .setOwnerClass(ownerClass)
                .setExceptionSafe(exceptionSafe)
                .setNoConsts(noconsts)
                .setResultType(PseudoRandom.randomElement(TypeList.getAll()))
        //rule.add("array_creation", builder.getArrayCreationFactory())             //TODO: uncomment
        rule.add("assignment", builder.getAssignmentOperatorFactory())
        rule.add("function", builder.getFunctionFactory(), ProductionParams.functionCallsPercent?.value() ?: 0.1)
    }

    override fun produce(): Statement {
        ProductionLimiter.setLimit()
        try {
            return Statement(rule.produce())
        } finally {
            ProductionLimiter.setUnlimited()
        }
    }
}
