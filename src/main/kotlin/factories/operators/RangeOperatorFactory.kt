package factories.operators

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import ir.Literal
import ir.NothingNode
import ir.operators.OperatorKind
import ir.operators.RangeOperator
import ir.types.Type
import ir.variables.LocalVariable
import utils.ProductionParams
import utils.PseudoRandom

class RangeOperatorFactory(
        complexitylimit: Long,
        operatorLimit: Int,
        protected val owner: Type?,
        exceptionSafe: Boolean,
        noConsts: Boolean,
        private val forLoop: Boolean
): OperatorFactory<RangeOperator>(OperatorKind.RANGE.priority, complexitylimit, operatorLimit, exceptionSafe, noConsts) {

    private fun generateType() = if (forLoop) {
        PseudoRandom.randomElement(listOf(TypeList.CHAR, TypeList.INT, TypeList.LONG))
    } else {
        PseudoRandom.randomElement(listOf(TypeList.CHAR, TypeList.INT, TypeList.LONG, TypeList.DOUBLE))
    }

    private fun generateProduction(paramType: Type): RangeOperator {
        val targetCompLimit = (PseudoRandom.random() * (complexityLimit - 1)).toLong()
        val fromCompLimit = (PseudoRandom.random() * (complexityLimit - 1 - targetCompLimit)).toLong()
        val toCompLimit = (PseudoRandom.random() * (complexityLimit - 1 - targetCompLimit - fromCompLimit)).toLong()
        val stepCompLimit = complexityLimit - 1 - targetCompLimit - fromCompLimit - toCompLimit

        val targetOpLimit = (PseudoRandom.random() * (operatorLimit - 1)).toInt()
        val fromOpLimit = (PseudoRandom.random() * (operatorLimit - 1 - targetOpLimit)).toInt()
        val toOpLimit = (PseudoRandom.random() * (operatorLimit - 1 - targetOpLimit - fromOpLimit)).toInt()
        val stepOpLimit = operatorLimit - 1 - targetOpLimit - fromOpLimit - toOpLimit

        if (targetCompLimit == 0L ||
                fromCompLimit == 0L ||
                toCompLimit == 0L ||
                stepCompLimit == 0L ||
                targetOpLimit == 0 ||
                fromOpLimit == 0 ||
                toOpLimit == 0 ||
                stepOpLimit == 0) {
            throw ProductionFailedException()
        }

        val builder = IRNodeBuilder().
                setExceptionSafe(exceptionSafe).
                setOwnerClass(owner).
                setNoConsts(noConsts).
                setResultType(paramType)

        val targetExpr = if (forLoop) {
            val resultName = "var_" + SymbolTable.getNextVariableNumber()
            val loopInfo = VariableInfo(resultName, owner, paramType, VariableInfo.LOCAL and VariableInfo.CONST)
            SymbolTable.add(loopInfo)
            LocalVariable(loopInfo)
        } else {
            builder.
                    setComplexityLimit(targetCompLimit).
                    setOperatorLimit(targetOpLimit).
                    getExpressionFactory().produce()
        }

        val fromExpr = builder.
                setComplexityLimit(fromCompLimit).
                setOperatorLimit(fromOpLimit).
                getExpressionFactory().produce()

        val toExpr = builder.
                setComplexityLimit(toCompLimit).
                setOperatorLimit(toOpLimit).
                getExpressionFactory().produce()

        if (paramType == TypeList.CHAR) builder.setResultType(TypeList.INT)
        val stepExpr = if (paramType == TypeList.DOUBLE) {
            NothingNode()
        } else  if (forLoop || ProductionParams.expressionsInRangeStep?.value()?.not()
                ?: throw NotInitializedOptionException("expressionsInRangeStep")) {
            getStepLiteral(paramType)
        }
        else {
            builder.setComplexityLimit(stepCompLimit).
                    setOperatorLimit(stepOpLimit).
                    getExpressionFactory().produce()
        }

        val inclusive = if (paramType == TypeList.DOUBLE) true else PseudoRandom.randomBoolean()
        val opposite = if (forLoop) false else PseudoRandom.randomBoolean()
        val forward = if (paramType == TypeList.DOUBLE) true else PseudoRandom.randomBoolean()
        return RangeOperator(targetExpr, fromExpr, toExpr, stepExpr, forward, inclusive, opposite)
    }

    private fun getStepLiteral(type: Type) = when (type) {
        TypeList.CHAR -> Literal(PseudoRandom.randomNotNegative(Char.MAX_SURROGATE.toLong()).toInt(), TypeList.INT)
        TypeList.INT -> Literal(PseudoRandom.randomNotNegative(Int.MAX_VALUE.toLong()).toInt(), TypeList.INT)
        TypeList.LONG -> Literal(PseudoRandom.randomNotNegative(Long.MAX_VALUE), TypeList.LONG)
        else -> throw ProductionFailedException()
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