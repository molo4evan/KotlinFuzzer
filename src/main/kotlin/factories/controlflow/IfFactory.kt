package factories.controlflow

import exceptions.ProductionFailedException
import factories.SafeFactory
import factories.utils.IRNodeBuilder
import information.TypeList
import ir.Block
import ir.controlflow.If
import ir.types.Type
import utils.PseudoRandom

class IfFactory(
        private val ownerClass: Type?,
        private val returnType: Type,
        private val complexityLimit: Long,
        private val statementLimit: Int,
        private val operatorLimit: Int,
        private val level: Long,
        private val canHaveBreaks: Boolean,
        private val canHaveContinues: Boolean,
        private val canHaveReturn: Boolean
): SafeFactory<If>() {

    override fun sproduce(): If {
        if (statementLimit > 0 && complexityLimit > 0){
            val condCompLimit = (0.01 * PseudoRandom.random() * (complexityLimit - 1)).toLong()
            val builder = IRNodeBuilder().setOwnerClass(ownerClass).setOperatorLimit(operatorLimit)
            val condition = builder.            //TODO: forbid assignment
                    setComplexityLimit(condCompLimit).
                    setResultType(TypeList.BOOLEAN).
                    setExceptionSafe(false).
                    setNoConsts(false).
                    getLimitedExpressionFactory().produce()

            val remainder = complexityLimit - 1 - condition.complexity()
            val ifBlockComplLimit = (PseudoRandom.random() * remainder).toLong()
            val elseBlockComplLimit = remainder - ifBlockComplLimit
            val ifBlockLimit = (PseudoRandom.random() * statementLimit).toInt()
            val elseBlockLimit = statementLimit - ifBlockLimit

            val controlDeviation = if (ifBlockLimit > 0 && elseBlockLimit <= 0)
            {
                If.IfPart.THEN
            } else {
                if (PseudoRandom.randomBoolean()) If.IfPart.THEN else If.IfPart.ELSE
            }

            if (ifBlockLimit > 0 && ifBlockComplLimit > 0) {
                val thenBlock: Block
                builder.setResultType(returnType)
                        .setLevel(level)
                        .setComplexityLimit(ifBlockComplLimit)
                        .setStatementLimit(ifBlockLimit)
                thenBlock = if (controlDeviation == If.IfPart.THEN) {
                    builder.setSubBlock(false)
                            .setCanHaveBreaks(canHaveBreaks)
                            .setCanHaveContinues(canHaveContinues)
                            .setCanHaveReturn(canHaveReturn)
                            .getBlockFactory()
                            .produce()
                } else {
                    builder.setSubBlock(false)
                            .setCanHaveBreaks(false)
                            .setCanHaveContinues(false)
                            .setCanHaveReturn(false)
                            .getBlockFactory()
                            .produce()
                }

                var elseBlock: Block? = null
                if (elseBlockLimit > 0 && elseBlockComplLimit > 0) {
                    builder.setComplexityLimit(elseBlockComplLimit)
                            .setStatementLimit(elseBlockLimit)
                    elseBlock = if (controlDeviation === If.IfPart.ELSE) {
                        builder.setSubBlock(false)
                                .setCanHaveBreaks(canHaveBreaks)
                                .setCanHaveContinues(canHaveContinues)
                                .setCanHaveReturn(canHaveReturn)
                                .getBlockFactory()
                                .produce()
                    } else {
                        builder.setSubBlock(false)
                                .setCanHaveBreaks(false)
                                .setCanHaveContinues(false)
                                .setCanHaveReturn(false)
                                .getBlockFactory()
                                .produce()
                    }
                }


                return If(condition, thenBlock, elseBlock, level)
            }
        }
        throw ProductionFailedException()
    }
}