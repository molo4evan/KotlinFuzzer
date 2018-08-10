package factories.rules.operators

import factories.Factory
import factories.utils.IRNodeBuilder
import factories.rules.Rule
import ir.operators.Operator
import ir.operators.OperatorKind
import ir.types.Type

class BitwiseOperatorFactory(
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
        rule = Rule("bitwise")
        rule.add("and", builder.setOperatorKind(OperatorKind.BIT_AND).getBinaryOperatorFactory())
        rule.add("or", builder.setOperatorKind(OperatorKind.BIT_OR).getBinaryOperatorFactory())
        rule.add("xor", builder.setOperatorKind(OperatorKind.BIT_XOR).getBinaryOperatorFactory())
        rule.add("shl", builder.setOperatorKind(OperatorKind.SHL).getBinaryOperatorFactory())
        rule.add("shr", builder.setOperatorKind(OperatorKind.SHR).getBinaryOperatorFactory())
        rule.add("ushr", builder.setOperatorKind(OperatorKind.USHR).getBinaryOperatorFactory())
    }

    override fun produce() = rule.produce()
}