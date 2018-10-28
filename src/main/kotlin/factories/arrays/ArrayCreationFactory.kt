package factories.arrays

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.SafeFactory
import factories.utils.IRNodeBuilder
import information.TypeList
import ir.arrays.ArrayCreation
import ir.types.Type
import ir.types.TypeArray
import utils.ProductionParams
import utils.PseudoRandom

class ArrayCreationFactory(
        private val complexityLimit: Long,
        private val operatorLimit: Int,
        private val owner: Type?,
        private val resultType: Type,
        private val exceptionSafe: Boolean,
        private val noConsts: Boolean
): SafeFactory<ArrayCreation>() {
    override fun sproduce(): ArrayCreation {
        if (resultType !is TypeArray) throw ProductionFailedException()

        var arrayType = resultType
        if (arrayType.type == TypeList.UNIT){
            arrayType = arrayType.produce()
        }

        val builder = IRNodeBuilder().
                setOwnerClass(owner).
                setResultType(TypeList.INT).
                setComplexityLimit(complexityLimit).
                setExceptionSafe(exceptionSafe).
                setNoConsts(noConsts)

        val exprChance = ProductionParams.chanceExpressionIndex?.value()
                ?: throw NotInitializedOptionException("chanceExpressionIndex")
        val sizeExpr = if (PseudoRandom.randomBoolean(exprChance)){
            builder.
                    setOperatorLimit((PseudoRandom.random() * operatorLimit).toInt()).
                    getExpressionFactory().produce()
        } else {
            var size = builder.getLiteralFactory().produce()
            while (size.value.toString().toInt() < 1){
                size = builder.getLiteralFactory().produce()
            }
            size
        }

        val varivaleDeclaration = builder.
                setResultType(arrayType).
                setIsLocal(true).
                setIsStatic(false).
                getVariableDeclarationFactory().produce()

        return ArrayCreation(varivaleDeclaration, arrayType, sizeExpr)
    }
}