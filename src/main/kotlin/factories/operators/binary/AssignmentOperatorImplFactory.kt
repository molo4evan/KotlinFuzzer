package factories.operators.binary

import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import factories.rules.Rule
import information.TypeList
import ir.operators.OperatorKind
import ir.types.Type
import ir.variables.VariableBase
import utils.PseudoRandom
import utils.TypeUtil

class AssignmentOperatorImplFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
): BinaryOperatorFactory(OperatorKind.ASSIGN, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type) = true

    override fun generateTypes() = Pair(resultType, PseudoRandom.randomElement(TypeUtil.getImplicitlyCastable(TypeList.getAll(), resultType)))

    override fun generateProduction(leftType: Type, rightType: Type): ir.operators.BinaryOperator {
        val leftOpLimit = (PseudoRandom.random() * (operatorLimit - 1)).toInt()
        val rightOpLimit = operatorLimit - 1 - leftOpLimit
        val leftComplLimit = (PseudoRandom.random() * (complexityLimit - 1)).toLong()
        val rightComplLimit = complexityLimit - 1 - leftComplLimit

        val builder = IRNodeBuilder().
                setOwnerClass(owner).
                setExceptionSafe(exceptionSafe).
                setNoConsts(noConsts).
                setComplexityLimit(leftComplLimit).
                setOperatorLimit(leftOpLimit).
                setResultType(leftType).
                setIsConstant(false)

        val rule = Rule<VariableBase>("assignment")
        rule.add("initialized_nonconst_var", builder.setIsInitialized(true).getVariableFactory())
        rule.add("uninitialized_nonconst_var", builder.setIsInitialized(false).getVariableFactory())

        val leftOperandValue = rule.produce()
        val rightOperandValue = builder.
                setComplexityLimit(rightComplLimit).
                setOperatorLimit(rightOpLimit).
                setResultType(rightType).
                getExpressionFactory().produce()

        try {
            if (!leftOperandValue.variableInfo.isiInitialized()){
                leftOperandValue.variableInfo.initialize()
            }
        } catch (ex: Exception){
            throw ProductionFailedException(ex.message)
        }
        return ir.operators.BinaryOperator(opKind, resultType, leftOperandValue, rightOperandValue)
    }
}