package factories.rules.operators

import exceptions.ProductionFailedException
import factories.Factory
import factories.utils.IRNodeBuilder
import factories.rules.Rule
import information.SymbolTable
import information.TypeList
import ir.operators.Operator
import ir.operators.OperatorKind
import ir.types.Type
import utils.PseudoRandom

class AssignmentOperatorFactory(
        val complexityLimit: Long,
        val operatorLimit: Int,
        val ownerClass: Type?,
        val resultType: Type?,
        val exceptionSafe: Boolean,
        val noconsts: Boolean
): Factory<Operator>() {
    private fun fillRule(resultType: Type): Rule<Operator> {
        val rule = Rule<Operator>("assignment")
        val builder = IRNodeBuilder().
                setComplexityLimit(complexityLimit).
                setOperatorLimit(operatorLimit).
                setOwnerClass(ownerClass).
                setResultType(resultType).
                setExceptionSafe(exceptionSafe).
                setNoConsts(noconsts)

        rule.add("simple_assign", builder.setOperatorKind(OperatorKind.ASSIGN).getBinaryOperatorFactory())
        rule.add("compound_add", builder.setOperatorKind(OperatorKind.COMPOUND_ADD).getBinaryOperatorFactory())
        rule.add("compound_sub", builder.setOperatorKind(OperatorKind.COMPOUND_SUB).getBinaryOperatorFactory())
        rule.add("compound_mul", builder.setOperatorKind(OperatorKind.COMPOUND_MUL).getBinaryOperatorFactory())
        if (!exceptionSafe) {
            rule.add("compound_div", builder.setOperatorKind(OperatorKind.COMPOUND_DIV).getBinaryOperatorFactory())
            rule.add("compound_mod", builder.setOperatorKind(OperatorKind.COMPOUND_MOD).getBinaryOperatorFactory())
        }

        rule.add("prefix_inc", builder.setOperatorKind(OperatorKind.PRE_INC).getUnaryOperatorFactory())
        rule.add("prefix_dec", builder.setOperatorKind(OperatorKind.PRE_DEC).getUnaryOperatorFactory())
        rule.add("postfix_inc", builder.setOperatorKind(OperatorKind.POST_INC).getUnaryOperatorFactory())
        rule.add("postfix_dec", builder.setOperatorKind(OperatorKind.POST_DEC).getUnaryOperatorFactory())
        return rule
    }

    override fun produce(): Operator {
        if (resultType == null) { // if no result type is given - choose any.
            val allTypes = ArrayList(TypeList.getAll())
            PseudoRandom.shuffle(allTypes)
            for (type in allTypes) {
                SymbolTable.push()
                try {
                    val result = fillRule(type).produce()
                    SymbolTable.merge()
                    return result
                } catch (e: ProductionFailedException) {
                    SymbolTable.pop()
                }

            }
        } else {
            return fillRule(resultType).produce()
        }
        throw ProductionFailedException()
    }
}