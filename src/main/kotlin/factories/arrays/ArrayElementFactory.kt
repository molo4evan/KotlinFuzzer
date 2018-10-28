package factories.arrays

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.SafeFactory
import factories.utils.IRNodeBuilder
import information.TypeList
import ir.Literal
import ir.arrays.ArrayCreation
import ir.arrays.ArrayElement
import ir.types.Type
import ir.types.TypeArray
import utils.ProductionParams
import utils.PseudoRandom

class ArrayElementFactory(
        private val complexityLimit: Long,
        private val operatorLimit: Int,
        private val owner: Type?,
        private val resultType: Type,
        private val exceptionSafe: Boolean,
        private val noConsts: Boolean
): SafeFactory<ArrayElement>() {
    override fun sproduce(): ArrayElement {
        if (resultType is TypeArray) throw ProductionFailedException()  //TODO: i think i should remove this

        val indexCompLimit = (complexityLimit * 0.5 * PseudoRandom.random()).toLong()
        val arrayCompLimit = complexityLimit - indexCompLimit
        val indexOpLimit = (operatorLimit * 0.5 * PseudoRandom.random()).toInt()
        val arrayOpLimit = operatorLimit - indexOpLimit

        val builder = IRNodeBuilder().
                setOwnerClass(owner).
                setExceptionSafe(exceptionSafe).
                setNoConsts(noConsts)

        val arrayReturningExpr = builder.
                setComplexityLimit(arrayCompLimit).
                setOperatorLimit(arrayOpLimit).
                setResultType(TypeArray(resultType)).
                getExpressionFactory().produce()

        val exprChance = ProductionParams.chanceExpressionIndex?.value()
                ?: throw NotInitializedOptionException("chanceExpressionIndex")

        val indexExpr = if (PseudoRandom.randomBoolean(exprChance)){
            builder.
                    setComplexityLimit(indexCompLimit).
                    setOperatorLimit(indexOpLimit).
                    setResultType(TypeList.INT).
                    getExpressionFactory().produce()
        } else {
            val size = if (arrayReturningExpr is ArrayCreation) {
                arrayReturningExpr.size
            } else 0
            Literal((PseudoRandom.randomNotNegative(size.toLong()).toInt()), TypeList.INT)
        }

        return ArrayElement(arrayReturningExpr, indexExpr)
    }
}