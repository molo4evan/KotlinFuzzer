package factories

import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import factories.rules.Rule
import information.SymbolTable
import information.TypeList
import ir.Block
import ir.IRNode
import ir.control_flow.If
import ir.control_flow.When
import ir.loops.DoWhile
import ir.loops.For
import ir.loops.While
import ir.types.Type
import utils.ProductionParams
import utils.PseudoRandom
import utils.TypeUtil
import java.util.ArrayList

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
                if (ProductionParams.disableVarsInBlock?.value()?.not()  ?: throw Exception("Option disableVarsInBlock not initialized")) {
                    rule.add("decl", builder.setIsLocal(true).getDeclarationFactory())
                }
                if (subLimit > 0) {
                    builder.setStatementLimit(subLimit).setLevel(level)
                    if (ProductionParams.disableNestedBlocks?.value()?.not() ?: throw Exception("Option disableNestedBlocks not initialized")) {
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
//            if (canHaveBreaks && !subBlock) {
//                rule.add("break", builder.getBreakFactory())
//            }
//            if (canHaveContinues && !subBlock) {
//                rule.add("continue", builder.getContinueFactory())
//            }
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
        if (ProductionParams.disableIf?.value()?.not() ?: throw Exception("Option disableIf not initialized")) {
            rule.add("if", builder.getIfFactory())
        }
//        if (ProductionParams.disableWhile?.value()?.not() ?: throw Exception("Option disableWhile not initialized")) {
//            rule.add("while", builder.getWhileFactory())
//        }
//        if (ProductionParams.disableDoWhile?.value()?.not() ?: throw Exception("Option disableDoWhile not initialized")) {
//            rule.add("do_while", builder.getDoWhileFactory())
//        }
//        if (ProductionParams.disableFor?.value()?.not() ?: throw Exception("Option disableFor not initialized")) {
//            rule.add("for", builder.getForFactory())
//        }
        if (ProductionParams.disableWhen?.value()?.not() ?: throw Exception("Option disableWhen not initialized")) {
            rule.add("when", builder.getWhenFactory())
        }
    }
}