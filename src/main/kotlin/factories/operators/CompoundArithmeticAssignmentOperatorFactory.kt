package factories.operators

import factories.utils.IRNodeBuilder
import information.TypeList
import ir.operators.BinaryOperator
import ir.operators.OperatorKind
import ir.types.Type
import utils.PseudoRandom
import utils.TypeUtil

class CompoundArithmeticAssignmentOperatorFactory(
        opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
): BinaryOperatorFactory(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type) = resultType.isBuiltIn() && resultType !=  TypeList.BOOLEAN

    override fun generateTypes() = Pair(resultType, PseudoRandom.randomElement(TypeUtil.getExplicitlyCastable(TypeList.getBuiltIn(), resultType)))

    override fun generateProduction(leftType: Type, rightType: Type): BinaryOperator {
        val leftCompLimit = (PseudoRandom.random() * complexityLimit).toLong()
        val rightCompLimit = complexityLimit - 1 - leftCompLimit
        val leftOpLimit = (PseudoRandom.random() * operatorLimit).toInt()
        val rightOpLimit = operatorLimit - 1 - leftOpLimit

        val builder = IRNodeBuilder().setOwnerClass(owner).setExceptionSafe(exceptionSafe).setNoConsts(noconsts)

        val rightExpr = builder.
                setComplexityLimit(rightCompLimit).
                setOperatorLimit(rightOpLimit).
                setResultType(rightType).
                getExpressionFactory().produce()
        val leftExpr = builder.
                setComplexityLimit(leftCompLimit).
                setOperatorLimit(leftOpLimit).
                setResultType(leftType).
                setIsConstant(false).
                setIsInitialized(true).
                getVariableFactory().produce()
        return BinaryOperator(opKind, resultType, leftExpr, rightExpr)
    }
}