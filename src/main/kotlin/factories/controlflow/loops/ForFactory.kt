package factories.controlflow.loops

import exceptions.ProductionFailedException
import factories.SafeFactory
import factories.utils.IRNodeBuilder
import information.SymbolTable
import ir.Block
import ir.controlflow.loops.For
import ir.types.Type
import utils.PseudoRandom

class ForFactory(
        private val owner: Type?,
        private val returnType: Type,
        private val complexityLimit: Long,
        private val statementsLimit: Int,
        private val operatorLimit: Int,
        private val level: Long,
        private val canHaveReturn: Boolean
): SafeFactory<For>() {
    override fun sproduce(): For {
        if (statementsLimit <= 0 || complexityLimit <= 0){
            throw ProductionFailedException()
        }
        val emptyBlock = Block(owner, returnType, listOf(), level - 1)

        val builder = IRNodeBuilder().
                setOwnerClass(owner).
                setResultType(returnType).
                setOperatorLimit(operatorLimit).
                setExceptionSafe(false).
                setNoConsts(false)

        val headerStatLimit = PseudoRandom.randomNotZero((statementsLimit/2.0).toInt())
        val bodyStatLimit = PseudoRandom.randomNotZero((statementsLimit/2.0).toInt())

        var complexity = complexityLimit
        val headerCompLimit = (complexity * PseudoRandom.random()).toLong()
        complexity -= headerCompLimit

        val loopIterLimit = (complexity * 0.001 * PseudoRandom.random()).toLong()
        if (loopIterLimit == 0L || loopIterLimit > Int.MAX_VALUE.toLong()) {
            throw ProductionFailedException()
        }

        complexity = if (loopIterLimit > 0) complexity / loopIterLimit else 0L
        val bodyCompLimit = (complexity * PseudoRandom.random()).toLong()

        SymbolTable.push()

        try {
            val header = builder.
                    setComplexityLimit(headerCompLimit).
                    setStatementLimit(headerStatLimit).
                    setForLoop(true).
                    getRangeOperatorFactory().produce()


            val body = try {
                builder.setComplexityLimit(bodyCompLimit).
                        setStatementLimit(bodyStatLimit).
                        setLevel(level).
                        setSubBlock(true).
                        setCanHaveBreaks(true).
                        setCanHaveContinues(true).
                        setCanHaveReturn(canHaveReturn).
                        getBlockFactory().produce()
            } catch (ex: ProductionFailedException) {
                emptyBlock
            }

            return For(level, loopIterLimit, header, body)
        } finally {
            SymbolTable.pop()
        }
    }
}