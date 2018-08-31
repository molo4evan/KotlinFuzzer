package factories.operators.unary

import exceptions.ProductionFailedException
import factories.operators.OperatorFactory
import information.SymbolTable
import ir.operators.OperatorKind
import ir.operators.UnaryOperator
import ir.types.Type

abstract class UnaryOperatorFactory protected constructor(
        protected val opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        protected val ownerClass: Type?,
        protected val resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : OperatorFactory<UnaryOperator>(opKind.priority, complexityLimit, operatorLimit, exceptionSafe, noconsts) {

    protected open fun generateType(): Type {
        return resultType
    }

    protected abstract fun generateProduction(type: Type): UnaryOperator

    protected abstract fun isApplicable(resultType: Type): Boolean

    override fun produce(): UnaryOperator {
        if (!isApplicable(resultType)) {
            //avoid implicit use of resultType.toString()
            throw ProductionFailedException("""Type ${resultType.getName()} is not applicable by ${this.javaClass.name}""")
        }
        val type: Type
        try {
            type = generateType()
        } catch (ex: Exception) {
            throw ProductionFailedException(ex.message)
        }

        try {
            SymbolTable.push()
            val result = generateProduction(type)
            SymbolTable.merge()
            return result
        } catch (e: ProductionFailedException) {
            SymbolTable.pop()
            throw e
        }

    }
}