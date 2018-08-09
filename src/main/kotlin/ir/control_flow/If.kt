package ir.control_flow

import ir.Block
import ir.IRNode
import providers.visitors.Visitor
import kotlin.math.max

class If(cond: IRNode, thenBlock: Block, elseBlock: Block?, level: Long) : IRNode(thenBlock.getResultType()) {   //TODO: how to make then an expression?
    enum class IfPart{
        CONDITION,
        THEN,
        ELSE
    }

    init {
        setChild(IfPart.CONDITION.ordinal, cond)
        setChild(IfPart.THEN.ordinal, thenBlock)
        setChild(IfPart.ELSE.ordinal, elseBlock)
        this.level = level
    }

    override fun complexity(): Long {
        val cond = getChild(IfPart.CONDITION.ordinal)
        val thenBlock = getChild(IfPart.THEN.ordinal)
        val elseBlock = getChild(IfPart.ELSE.ordinal)

        return (cond?.complexity() ?: 0) + (thenBlock?.complexity() ?: 0) + (elseBlock?.complexity() ?: 0)
    }

    override fun countDepth() = max(level, super.countDepth())

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}