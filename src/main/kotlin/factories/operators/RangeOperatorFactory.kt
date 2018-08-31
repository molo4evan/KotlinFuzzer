package factories.operators

import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import information.SymbolTable
import information.TypeList
import ir.NothingNode
import ir.operators.OperatorKind
import ir.operators.RangeOperator
import ir.types.Type
import utils.PseudoRandom

class RangeOperatorFactory(
        complexitylimit: Long,
        operatorLimit: Int,
        protected val owner: Type?,
        exceptionSafe: Boolean,
        noConsts: Boolean
): OperatorFactory<RangeOperator>(OperatorKind.RANGE.priority, complexitylimit, operatorLimit, exceptionSafe, noConsts) {

    private fun generateType() = PseudoRandom.randomElement(listOf(TypeList.DOUBLE, TypeList.CHAR, TypeList.INT, TypeList.LONG))

    private fun generateProduction(paramType: Type): RangeOperator {
        val targetCompLimit = (PseudoRandom.random() * (complexityLimit - 1)).toLong()
        val fromCompLimit = (PseudoRandom.random() * (complexityLimit - 1 - targetCompLimit)).toLong()
        val toCompLimit = complexityLimit - 1 - targetCompLimit - fromCompLimit

        val targetOpLimit = (PseudoRandom.random() * (operatorLimit - 1)).toInt()
        val fromOpLimit = (PseudoRandom.random() * (operatorLimit - 1 - targetOpLimit)).toInt()
        val toOpLimit = operatorLimit - 1 - targetOpLimit - fromOpLimit

        if (targetCompLimit == 0L || fromCompLimit == 0L || toCompLimit == 0L || targetOpLimit == 0 || fromOpLimit == 0 || toOpLimit == 0) {
            throw ProductionFailedException()
        }

        val builder = IRNodeBuilder().
                setExceptionSafe(exceptionSafe).
                setOwnerClass(owner).
                setNoConsts(noConsts).
                setResultType(paramType)

        val targetExpr = builder.
                setComplexityLimit(targetCompLimit).
                setOperatorLimit(targetOpLimit).
                getExpressionFactory().produce()
        val fromExpr = builder.
                setComplexityLimit(fromCompLimit).
                setOperatorLimit(fromOpLimit).
                getExpressionFactory().produce()
        val toExpr = builder.
                setComplexityLimit(toCompLimit).
                setOperatorLimit(toOpLimit).
                getExpressionFactory().produce()

        val forward = if (paramType == TypeList.DOUBLE) true else PseudoRandom.randomBoolean()
        val inclusive = if (paramType == TypeList.DOUBLE) true else PseudoRandom.randomBoolean()
        val opposite = PseudoRandom.randomBoolean()

        return RangeOperator(targetExpr, fromExpr, toExpr, NothingNode(), forward, inclusive, opposite)
    }

    override fun produce(): RangeOperator {
        val type = generateType()
        try {
            SymbolTable.push()
            val result = generateProduction(type)
            SymbolTable.merge()
            return result
        } catch (ex: ProductionFailedException) {
            SymbolTable.pop()
            throw ex
        }
    }
}