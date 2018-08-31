package factories.operators.binary

import exceptions.ProductionFailedException
import factories.operators.OperatorFactory
import factories.utils.IRNodeBuilder
import information.SymbolTable
import ir.operators.BinaryOperator
import ir.operators.OperatorKind
import ir.types.Type
import utils.PseudoRandom

abstract class BinaryOperatorFactory protected constructor(
        protected val opKind: OperatorKind,
        complexitylimit: Long,
        operatorLimit: Int,
        protected val owner: Type?,
        protected  val resultType: Type,
        exceptionSafe: Boolean,
        noConsts: Boolean
): OperatorFactory<BinaryOperator>(opKind.priority, complexitylimit, operatorLimit, exceptionSafe, noConsts) {
    protected abstract fun isApplicable(resultType: Type): Boolean

    protected abstract fun generateTypes(): Pair<Type, Type>

    protected open fun generateProduction(leftType: Type, rightType: Type): BinaryOperator{
        val leftOpLimit = (PseudoRandom.random() * (operatorLimit - 1)).toInt()
        val rightOpLimit = operatorLimit - 1 - leftOpLimit
        val leftComplLimit = (PseudoRandom.random() * (complexityLimit - 1)).toLong()
        val rightComplLimit = complexityLimit - 1 - leftComplLimit

        if (leftOpLimit == 0 || rightOpLimit == 0 || leftComplLimit == 0L || rightComplLimit == 0L){
            throw ProductionFailedException()
        }

        val swap = PseudoRandom.randomBoolean()

        val builder = IRNodeBuilder().setExceptionSafe(exceptionSafe).setOwnerClass(owner).setNoConsts(!swap && noConsts)

        val leftExpr = builder.setComplexityLimit(leftComplLimit).setOperatorLimit(leftOpLimit).setResultType(leftType).getExpressionFactory().produce()

        val rightExpr = builder.setComplexityLimit(rightComplLimit).setOperatorLimit(rightOpLimit).setResultType(rightType).getExpressionFactory().produce()

        return BinaryOperator(opKind, resultType, leftExpr, rightExpr)
    }

    override fun produce(): BinaryOperator{
        if (!isApplicable(resultType)){
            //avoid implicit use of resultType.toString()
            throw ProductionFailedException("Type " + resultType.getName() + " is not applicable by " + this::class.simpleName)
        }

        val types: Pair<Type, Type> = try {
            generateTypes()
        } catch (ex: Exception) {
            throw ProductionFailedException(ex.message)
        }

        try {
            SymbolTable.push()
            val result = generateProduction(types.first, types.second)
            SymbolTable.merge()
            return result
        } catch (ex: ProductionFailedException){
            SymbolTable.pop()
            throw ex
        }
    }
}