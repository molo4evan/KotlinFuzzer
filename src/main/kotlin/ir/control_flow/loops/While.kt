package ir.control_flow.loops

import ir.Block
import ir.IRNode
import providers.visitors.Visitor
import kotlin.math.max

// int counter = x;
// header;                                // [subblock]
// while (condition) {
//      body1;                                 //    [subblock with breaks]
//      mutate(counter);
//      body2;                                 //    [subblock with breaks and continues]
//      body3;                                 //    [subblock with breaks]
// }

class While(level: Long, val loop: Loop, private val thisLoopIterLimit: Long,
            header: Block, body1: Block, body2: Block, body3: Block): IRNode(body1.getResultType()) {
    
    enum class WhilePart{
        HEADER,
        BODY1,
        BODY2,
        BODY3
    }
    
    init {
        this.level = level
        setChild(WhilePart.HEADER.ordinal, header)
        setChild(WhilePart.BODY1.ordinal, body1)
        setChild(WhilePart.BODY2.ordinal, body2)
        setChild(WhilePart.BODY3.ordinal, body3)
    }

    override fun complexity(): Long {
        val header = children[WhilePart.HEADER.ordinal]
        val body1 = children[WhilePart.BODY1.ordinal]
        val body2 = children[WhilePart.BODY2.ordinal]
        val body3 = children[WhilePart.BODY3.ordinal]

        return loop.initializer.complexity() + header.complexity() + thisLoopIterLimit *
                (loop.condition.complexity()
                 + body1.complexity()
                 + loop.manipulator.complexity()
                 + body2.complexity()
                 + body3.complexity())
    }

    override fun countDepth() = max(level, super.countDepth())

    override fun removeSelf(): Boolean {
        val header = children[WhilePart.HEADER.ordinal]
        val siblings = parent!!.children
        var index = siblings.indexOf(this)
        siblings[index++] = loop.initializer
        if (header is Block) {
            siblings.addAll(index, header.children)
        } else {
            siblings[index] = header
        }
        return true
    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}