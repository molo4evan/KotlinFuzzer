package ir.functions

import ir.IRNode
import visitors.Visitor

class Return(val retExpr: IRNode): IRNode(retExpr.getResultType()) {
    init {
        addChild(retExpr)
    }

    override fun complexity() = retExpr.complexity()

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}