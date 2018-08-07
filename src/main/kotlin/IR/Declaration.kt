package IR

import Visitors.Visitor

class Declaration(declExpr: IRNode): IRNode(declExpr.getResultType()) {
    init {
        addChild(declExpr)
    }

    override fun complexity() = children[0].complexity()

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}