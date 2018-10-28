package ir.controlflow.loops

import ir.Block
import ir.IRNode
import providers.visitors.Visitor
import kotlin.math.max

// header;                  [subblock]
// do {
//      body1;              [subblock with breaks]
//      mutate(counter);
//      body2;              [subblock with breaks]
// } while(condition);

class DoWhile(level: Long, val loop: Loop, private val thisLoopIterLimit: Long,
              header: Block, body1: Block, body2: Block): IRNode(body1.getResultType()) {

    enum class DoWhilePart{
        HEADER,
        BODY1,
        BODY2
    }

    init {
        this.level = level
        setChild(DoWhilePart.HEADER.ordinal, header)
        setChild(DoWhilePart.BODY1.ordinal, body1)
        setChild(DoWhilePart.BODY2.ordinal, body2)
    }

    override fun complexity(): Long {
        val header = children[DoWhilePart.HEADER.ordinal]
        val body1 = children[DoWhilePart.BODY1.ordinal]
        val body2 = children[DoWhilePart.BODY2.ordinal]

        return loop.initializer.complexity() + header.complexity() + thisLoopIterLimit *
                (body1.complexity()
                + loop.manipulator.complexity()
                + body2.complexity()
                + loop.condition.complexity())
    }

    override fun countDepth() = max(level, super.countDepth())

    override fun removeSelf(): Boolean {
        val header = children[DoWhilePart.HEADER.ordinal]
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