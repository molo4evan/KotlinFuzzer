package ir.loops

import ir.IRNode
import providers.visitors.Visitor

class LoopingCondition(val condition: IRNode): IRNode(condition.getResultType()) {
    init {
        addChild(condition)
    }

    override fun complexity() = condition.complexity()

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}