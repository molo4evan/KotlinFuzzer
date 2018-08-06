package IR

import kotlin.math.max

class If(cond: IRNode, thenBlock: Block, elseBlock: Block, level: Long) : IRNode(thenBlock.getResultType()) {   //TODO: how to make then an expression?
    enum class IFPart{
        CONDITION,
        THEN,
        ELSE
    }

    init {
        setChild(IFPart.CONDITION.ordinal, cond)
        setChild(IFPart.THEN.ordinal, thenBlock)
        setChild(IFPart.ELSE.ordinal, elseBlock)
        this.level = level
    }

    override fun complexity(): Long {
        val cond = getChild(IFPart.CONDITION.ordinal)
        val thenBlock = getChild(IFPart.THEN.ordinal)
        val elseBlock = getChild(IFPart.ELSE.ordinal)

        return (cond?.complexity() ?: 0) + (thenBlock?.complexity() ?: 0) + (elseBlock?.complexity() ?: 0)
    }

    override fun countDepth() = max(level, super.countDepth())
}