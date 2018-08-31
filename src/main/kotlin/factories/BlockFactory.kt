package factories

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.rules.Rule
import factories.utils.IRNodeBuilder
import information.SymbolTable
import information.TypeList
import ir.Block
import ir.IRNode
import ir.control_flow.If
import ir.control_flow.When
import ir.control_flow.loops.DoWhile
import ir.control_flow.loops.For
import ir.control_flow.loops.While
import ir.types.Type
import utils.ProductionParams
import utils.PseudoRandom
import java.util.*

class BlockFactory(
        private val owner: Type?,
        private val returnType: Type,
        private val complexityLimit: Long,
        private val statementLimit: Int,
        private val operatorLimit: Int,
        private val level: Long,
        private val subBlock: Boolean,
        private val canHaveBreaks: Boolean,
        private val canHaveContinues: Boolean,
        private val canHaveReturn: Boolean,
        private val canHaveThrow: Boolean
): Factory<Block>() {
    override fun produce(): Block {
        if (statementLimit > 0 && complexityLimit > 0) {
            val content = ArrayList<IRNode>()
            val slimit = PseudoRandom.randomNotZero(statementLimit)
            var climit = complexityLimit

            val builder = IRNodeBuilder()
                    .setOperatorLimit(operatorLimit)
                    .setOwnerClass(owner)
                    .setResultType(returnType)
                    .setCanHaveReturn(canHaveReturn)
                    .setCanHaveThrow(canHaveThrow)
                    .setCanHaveBreaks(canHaveBreaks)
                    .setCanHaveContinues(canHaveContinues)
                    .setExceptionSafe(false)
                    .setNoConsts(false)

            var rule: Rule<IRNode>
            SymbolTable.push()

            var i = 0
            while (i < slimit && climit > 0) {
                val subLimit = (PseudoRandom.random() * (slimit - i - 1)).toInt()
                builder.setComplexityLimit((PseudoRandom.random() * climit).toLong())
                rule = Rule("block")
                rule.add("statement", builder.getStatementFactory(), 3.0)
                if (ProductionParams.disableVarsInBlock?.value()?.not()  ?: throw NotInitializedOptionException("disableVarsInBlock")) {
                    rule.add("decl", builder.setIsLocal(true).getDeclarationFactory())
                }
                if (subLimit > 0) {
                    builder.setStatementLimit(subLimit).setLevel(level + 1)
                    if (ProductionParams.disableNestedBlocks?.value()?.not() ?: throw NotInitializedOptionException("disableNestedBlocks")) {
                        rule.add("block", builder.setCanHaveReturn(false)
                                .setCanHaveThrow(false)
                                .setCanHaveBreaks(false)
                                .setCanHaveContinues(false)
                                .getBlockFactory())
                        //rule.add("try-catch", builder.getTryCatchBlockFactory(), 0.3)
                        builder.setCanHaveReturn(canHaveReturn)
                                .setCanHaveThrow(canHaveThrow)
                                .setCanHaveBreaks(canHaveBreaks)
                                .setCanHaveContinues(canHaveContinues)
                    }
                    addControlFlowDeviation(rule, builder)
                }
                try {
                    val choiceResult = rule.produce()
                    if (choiceResult is If || choiceResult is While || choiceResult is DoWhile
                            || choiceResult is For || choiceResult is When) {
                        i += subLimit
                    } else {
                        i++
                    }
                    climit -= choiceResult.complexity()
                    content.add(choiceResult)
                } catch (e: ProductionFailedException) {
                    i++
                }

            }
            // Ok, if the block can end with break and continue. Generate the appropriate productions.
            rule = Rule("block_ending")
            if (canHaveBreaks && !subBlock) {
                rule.add("break", builder.getBreakFactory())
            }
            if (canHaveContinues && !subBlock) {
                rule.add("continue", builder.getContinueFactory())
            }
            if (canHaveReturn && !subBlock && returnType != TypeList.UNIT) {
                rule.add("return", builder.setComplexityLimit(climit).getReturnFactory())
            }
//            if (canHaveThrow && !subBlock) {
//                var rtException = TypeList.find("java.lang.RuntimeException")
//                rtException = PseudoRandom.randomElement(TypeUtil.getImplicitlyCastable(TypeList.getAll(), rtException))
//                //rule.add("throw", builder.setResultType(rtException).setComplexityLimit(Math.max(climit, 5)).setOperatorLimit(Math.max(operatorLimit, 5)).getThrowFactory())
//            }

            try {
                if (rule.size() > 0) {
                    content.add(rule.produce())
                }
            } catch (e: ProductionFailedException) {
            }

            if (!subBlock) {        // if !subblock add vars to outer table, else hide upper vars...
                SymbolTable.pop()
            } else {
                SymbolTable.merge()
            }
            return Block(owner, returnType, content, level)
        }
        throw ProductionFailedException()
    }

    private fun addControlFlowDeviation(rule: Rule<IRNode>, builder: IRNodeBuilder) {
        if (ProductionParams.disableIf?.value()?.not() ?: throw NotInitializedOptionException("disableIf")) {
            rule.add("if", builder.getIfFactory())
        }
        if (ProductionParams.disableWhile?.value()?.not() ?: throw NotInitializedOptionException("disableWhile")) {
            rule.add("while", builder.getWhileFactory())
        }
        if (ProductionParams.disableDoWhile?.value()?.not() ?: throw NotInitializedOptionException("disableDoWhile")) {
            rule.add("do_while", builder.getDoWhileFactory())
        }
//        if (ProductionParams.disableFor?.value()?.not() ?: throw NotInitializedOptionException("disableFor")) {
//            rule.add("for", builder.getForFactory())
//        }
        if (ProductionParams.disableWhen?.value()?.not() ?: throw NotInitializedOptionException("disableWhen")) {
            rule.add("when", builder.getWhenFactory())
        }
    }
}