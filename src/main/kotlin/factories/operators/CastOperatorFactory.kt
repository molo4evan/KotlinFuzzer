package factories.operators

import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import information.SymbolTable
import information.TypeList
import ir.operators.CastOperator
import ir.types.Type
import utils.PseudoRandom

class CastOperatorFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        private val owner: Type?,
        private val result: Type,
        exceptionSafe: Boolean,
        noConsts: Boolean
): OperatorFactory<CastOperator>(13, complexityLimit, operatorLimit, exceptionSafe, noConsts) {
    override fun produce(): CastOperator {
        val argTypes = TypeList.getAll()
        PseudoRandom.shuffle(argTypes)
        for (type in argTypes) {
            if (type == TypeList.BOOLEAN) continue
            try {
                val exprFactory = IRNodeBuilder().
                        setComplexityLimit(complexityLimit - 1).
                        setOperatorLimit(operatorLimit - 1).
                        setOwnerClass(owner).
                        setResultType(type).
                        setExceptionSafe(exceptionSafe).
                        setNoConsts(noConsts).
                        getExpressionFactory()
                SymbolTable.push()
                if (type == result || (exceptionSafe && type.isBuiltIn() || type.canExplicitlyCastTo(result) && !exceptionSafe)) {
                    // In safe mode we cannot explicitly cast an object, because it may throw
                    val castOp = CastOperator(result, exprFactory.produce(), result.isBuiltIn())
                    SymbolTable.merge()
                    return castOp
                }
                throw ProductionFailedException()
            } catch (ex: ProductionFailedException) {
                SymbolTable.pop()
            }
        }
        throw ProductionFailedException()
    }
}