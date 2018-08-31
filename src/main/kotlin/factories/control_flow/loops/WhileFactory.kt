package factories.control_flow.loops

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.SafeFactory
import factories.utils.IRNodeBuilder
import information.SymbolTable
import information.TypeList
import ir.Block
import ir.Literal
import ir.control_flow.loops.Loop
import ir.control_flow.loops.While
import ir.types.Type
import ir.variables.LocalVariable
import utils.ProductionParams
import utils.PseudoRandom

class WhileFactory(
        private val owner: Type?,
        private val result: Type,
        private val complexityLimit: Long,
        private val statementLimit: Int,
        private val operatorLimit: Int,
        private val level: Long,
        private val canHaveReturn: Boolean
): SafeFactory<While>() {                   //TODO: add downto loop?
    override fun sproduce(): While {
        if (statementLimit <= 0 || complexityLimit <= 0) {
            throw ProductionFailedException()
        }

        val emptyBlock = Block(owner, result, listOf(), level - 1)

        var currentCompl = complexityLimit
        val headerCompLimit = (PseudoRandom.random() * currentCompl * 0.005).toLong()   //TODO: hardcode const...
        currentCompl -= headerCompLimit
        val headerStatLimit = PseudoRandom.randomNotZero(statementLimit / 3)

        val iterationLimit = (0.0001 * currentCompl * PseudoRandom.random()).toLong() * PseudoRandom.randomNotZero(ProductionParams.stepLimit?.value() ?: throw NotInitializedOptionException("stepLimit"))
        if (iterationLimit > Int.MAX_VALUE.toLong() || iterationLimit == 0L) {
            throw ProductionFailedException()
        }
        var iterCompl = currentCompl / iterationLimit
        val condComplLimit = (iterCompl * PseudoRandom.random()).toLong()
        iterCompl -= condComplLimit
        val body1StatLimit = PseudoRandom.randomNotZero(statementLimit / 4)
        val body1CompLimit = (iterCompl * PseudoRandom.random()).toLong()
        iterCompl -= body1CompLimit
        val body2StatLimit = PseudoRandom.randomNotZero(statementLimit / 4)
        val body2CompLimit = (iterCompl * PseudoRandom.random()).toLong()
        iterCompl -= body2CompLimit
        val body3StatLimit = PseudoRandom.randomNotZero(statementLimit / 4)
        val body3CompLimit = (iterCompl * PseudoRandom.random()).toLong()

        val builder = IRNodeBuilder().
                setOwnerClass(owner).
                setResultType(result).
                setOperatorLimit(operatorLimit)

        val initializer = builder.getCounterInitializerFactory(0).produce()

        val header = try {
            builder.
                    setComplexityLimit(headerCompLimit).
                    setStatementLimit(headerStatLimit).
                    setLevel(level - 1).
                    setSubBlock(true).
                    setCanHaveBreaks(false).
                    setCanHaveContinues(false).
                    setCanHaveReturn(false).
                    getBlockFactory().produce()
        } catch (ex: ProductionFailedException) {
            emptyBlock
        }

        val counter = LocalVariable(initializer.variableInfo)
        val limiter = Literal(iterationLimit.toInt(), TypeList.INT)
        val condition = builder.
                setComplexityLimit(condComplLimit).
                setLocalVariable(counter).
                getLoopingConditionFactory(limiter).produce()

        SymbolTable.push()

        val body1 = try {
            builder.
                    setComplexityLimit(body1CompLimit).
                    setStatementLimit(body1StatLimit).
                    setLevel(level).
                    setSubBlock(true).
                    setCanHaveBreaks(true).
                    setCanHaveContinues(false).
                    setCanHaveReturn(canHaveReturn).
                    getBlockFactory().produce()
        } catch (ex: ProductionFailedException) {
            emptyBlock
        }

        val manipulator = builder.setLocalVariable(counter).getCounterManipulatorFactory().produce()

        val body2 = try {
            builder.setComplexityLimit(body2CompLimit).
                    setStatementLimit(body2StatLimit).
                    setLevel(level).
                    setSubBlock(true).
                    setCanHaveBreaks(true).
                    setCanHaveContinues(true).
                    setCanHaveReturn(canHaveReturn).
                    getBlockFactory().produce()
        } catch (ex: ProductionFailedException) {
            emptyBlock
        }

        val body3 = try {
            builder.setComplexityLimit(body3CompLimit).
                    setStatementLimit(body3StatLimit).
                    setLevel(level).
                    setSubBlock(true).
                    setCanHaveBreaks(true).
                    setCanHaveContinues(false).
                    setCanHaveReturn(canHaveReturn).
                    getBlockFactory().produce()
        } catch (ex: ProductionFailedException) {
            emptyBlock
        }

        SymbolTable.pop()

        val loop = Loop(initializer, condition, manipulator)
        return While(level, loop, iterationLimit, header, body1, body2, body3)
    }
}