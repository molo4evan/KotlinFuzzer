package factories.rules.operators

import exceptions.ProductionFailedException
import factories.Factory
import factories.rules.Rule
import factories.utils.IRNodeBuilder
import ir.operators.Operator
import ir.operators.OperatorKind
import ir.types.Type

class LogicOperatorFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : Factory<Operator>() {
    private val rule: Rule<Operator>

    init {
        val builder = IRNodeBuilder
                .setComplexityLimit(complexityLimit)
                .setOperatorLimit(operatorLimit)
                .setOwnerClass(ownerClass)
                .setResultType(resultType)
                .setExceptionSafe(exceptionSafe)
                .setNoConsts(noconsts)
        rule = Rule("arithmetic")
        rule.add("land", builder.setOperatorKind(OperatorKind.AND).getBinaryOperatorFactory())
        rule.add("lor", builder.setOperatorKind(OperatorKind.OR).getBinaryOperatorFactory())
        rule.add("greater", builder.setOperatorKind(OperatorKind.GT).getBinaryOperatorFactory())
        rule.add("less", builder.setOperatorKind(OperatorKind.LT).getBinaryOperatorFactory())
        rule.add("ge", builder.setOperatorKind(OperatorKind.GE).getBinaryOperatorFactory())
        rule.add("le", builder.setOperatorKind(OperatorKind.LE).getBinaryOperatorFactory())
        rule.add("eq", builder.setOperatorKind(OperatorKind.EQ).getBinaryOperatorFactory())
        rule.add("neq", builder.setOperatorKind(OperatorKind.NE).getBinaryOperatorFactory())
        rule.add("not", builder.setOperatorKind(OperatorKind.NOT).getUnaryOperatorFactory())
    }

    override fun produce() = rule.produce()
}