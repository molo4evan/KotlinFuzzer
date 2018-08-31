package factories.control_flow

import exceptions.ProductionFailedException
import factories.SafeFactory
import factories.rules.Rule
import factories.utils.IRNodeBuilder
import information.TypeList
import ir.IRNode
import ir.NothingNode
import ir.control_flow.When
import ir.types.Type
import utils.PseudoRandom

class WhenFactory(
        private val complexityLimit: Long,
        private val statementLimit: Int,
        private val operatorLimit: Int,
        private val owner: Type?,
        private val returnType: Type?,
        private val level: Long,
        private val canHareReturn: Boolean
): SafeFactory<When>() {
    override fun sproduce(): When {
        if (statementLimit > 0 && complexityLimit > 0) {
            val whenTypes: List<Type>
            if (returnType == null) {
                whenTypes = TypeList.getAll()
                PseudoRandom.shuffle(whenTypes)
            } else {
                whenTypes = mutableListOf(returnType)
            }
            val builder = IRNodeBuilder().
                    setOwnerClass(owner).
                    setOperatorLimit(operatorLimit).
                    setSubBlock(false).
                    setCanHaveBreaks(false).
                    setCanHaveContinues(false).
                    setCanHaveReturn(canHareReturn)
            for (type in whenTypes) {
                val caseConds = mutableListOf<IRNode>()
                val caseBlocks = mutableListOf<IRNode>()
                try {
                    var accumulatedComplexity = 0L
                    var accumulatedStatements = 0
                    var elseCompLimit = 0L
                    var elseStatLimit = 0

                    val haveElse = if (!type.isEnum()) PseudoRandom.randomBoolean() else true

                    if (haveElse) {
                        elseCompLimit = PseudoRandom.randomNotZero(complexityLimit)
                        accumulatedComplexity = elseCompLimit
                        elseStatLimit = PseudoRandom.randomNotZero(statementLimit)
                        accumulatedStatements = elseStatLimit
                    }

                    var currentCompLimit = (PseudoRandom.random() * (complexityLimit - accumulatedComplexity)).toLong()
                    var currentStatLimit: Int

                    val whenExpr = builder.
                            setComplexityLimit(currentCompLimit).
                            setResultType(type).
                            setExceptionSafe(false).
                            setNoConsts(false).
                            getLimitedExpressionFactory().produce()

                    while (accumulatedStatements < statementLimit) {
                        currentCompLimit = (PseudoRandom.random() * (complexityLimit - accumulatedComplexity)).toLong()
                        accumulatedComplexity += currentCompLimit
                        val expr = builder.                     //TODO: add typecheck generation
                                setComplexityLimit(currentCompLimit).
                                setResultType(type).
                                setExceptionSafe(false).
                                setNoConsts(false).
                                getLimitedExpressionFactory().produce() //limited? not just expression?
                        caseConds.add(expr)

                        currentCompLimit = (PseudoRandom.random() * (complexityLimit - accumulatedComplexity)).toLong()
                        currentStatLimit = (PseudoRandom.random() * (statementLimit - accumulatedStatements)).toInt()

                        val rule = Rule<IRNode>("case_block")
                        rule.add("block", builder.
                                setComplexityLimit(currentCompLimit).
                                setStatementLimit(currentStatLimit).
                                setLevel(level + 1).
                                getBlockFactory())
                        rule.add("nothing", builder.getNothingFactory(), 0.3)    // mb decrease percentage of empty case blocks?
                        val chosenResult = rule.produce()
                        caseBlocks.add(chosenResult)
                        if (chosenResult is NothingNode) {
                            accumulatedStatements++
                        } else {
                            accumulatedStatements += currentStatLimit
                            accumulatedComplexity += currentCompLimit
                        }
                    }
                    PseudoRandom.shuffle(caseConds)

                    if (haveElse) {
                        caseConds.add(NothingNode())
                        caseBlocks.add(builder.
                                setComplexityLimit(elseCompLimit).
                                setLevel(level + 1).
                                setStatementLimit(elseStatLimit).
                                getBlockFactory().produce())
                    }

                    val accum = mutableListOf<IRNode>()
                    val caseBlocksIdx = caseConds.size + 1
                    accum.add(whenExpr)

                    for (i in 1 until caseBlocksIdx) {
                        accum.add(caseConds[i - 1])
                    }
                    for (i in caseBlocksIdx .. caseConds.size + caseBlocks.size) {
                        accum.add(caseBlocks[i - caseBlocksIdx])
                    }
                    return When(level, accum, caseBlocksIdx)
                } catch (ex: ProductionFailedException) {}
            }
        }
        throw ProductionFailedException()
    }
}