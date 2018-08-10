package factories.functoins

import factories.Factory
import factories.utils.IRNodeBuilder
import information.*
import ir.IRNode
import ir.functions.MainFunction
import utils.PseudoRandom

class MainFunctionFactory (
        private val name: String,
        private val complexityLimit: Long,
        private val statementLimit: Int,
        private val operatorLimit: Int
): Factory<MainFunction>() {
    override fun produce(): MainFunction {
        var body: IRNode? = null

        SymbolTable.push()
        try {
            val builder = IRNodeBuilder().setArgumentType(null)
            val blockCompLimit = (PseudoRandom.random() * complexityLimit).toLong()
            body = builder.setOwnerClass(null).
                    setResultType(TypeList.UNIT).
                    setComplexityLimit(blockCompLimit).
                    setStatementLimit(statementLimit).
                    setOperatorLimit(operatorLimit).
                    setLevel(1).
                    setSubBlock(true).
                    setCanHaveBreaks(false).
                    setCanHaveContinues(false).
                    setCanHaveReturn(true).
                    getBlockFactory().produce()
        } finally {
            SymbolTable.pop()
        }
        return MainFunction(name, body)
    }
}