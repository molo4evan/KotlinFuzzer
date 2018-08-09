package factories.rules.operators

import factories.Factory
import factories.utils.IRNodeBuilder
import factories.rules.Rule
import ir.operators.Operator
import ir.operators.OperatorKind
import ir.types.Type

internal class ArithmeticOperatorFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : Factory<Operator>() {
    private val rule: Rule<Operator>

    init {
        val builder = IRNodeBuilder()
                .setComplexityLimit(complexityLimit)
                .setOperatorLimit(operatorLimit)
                .setOwnerClass(ownerClass)
                .setResultType(resultType)
                .setExceptionSafe(exceptionSafe)
                .setNoConsts(noconsts)
        rule = Rule("arithmetic")
        rule.add("add", builder.setOperatorKind(OperatorKind.ADD).getBinaryOperatorFactory())
        rule.add("sub", builder.setOperatorKind(OperatorKind.SUB).getBinaryOperatorFactory())
        rule.add("mul", builder.setOperatorKind(OperatorKind.MUL).getBinaryOperatorFactory())
        if (!exceptionSafe) {
            rule.add("div", builder.setOperatorKind(OperatorKind.DIV).getBinaryOperatorFactory())
            rule.add("mod", builder.setOperatorKind(OperatorKind.MOD).getBinaryOperatorFactory())
        }
        rule.add("unary_plus", builder.setOperatorKind(OperatorKind.UNARY_PLUS).getUnaryOperatorFactory())
        rule.add("unary_minus", builder.setOperatorKind(OperatorKind.UNARY_MINUS).getUnaryOperatorFactory())
    }

    override fun produce(): Operator {
        return rule.produce()
    }
}