package ir.controlflow.loops

import ir.Block
import ir.IRNode
import ir.operators.RangeOperator
import providers.visitors.Visitor
import kotlin.math.max

// header;                   // for (header) {
// body;                     //    [loop body]
// }

class For(
        level: Long,
        private val thisLoopIterLimit: Long,
        header: RangeOperator,
        body: Block
): IRNode(body.getResultType()) {

    enum class ForPart{
        HEADER,
        BODY
    }

    init {
        this.level = level
        setChild(ForPart.HEADER.ordinal, header)
        setChild(ForPart.BODY.ordinal, body)
    }

    override fun complexity(): Long {

        val header = getChild(ForPart.HEADER.ordinal)
        val body = getChild(ForPart.BODY.ordinal)

        return header.complexity() + thisLoopIterLimit * body.complexity()
    }

    override fun countDepth() = max(level, super.countDepth())

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}