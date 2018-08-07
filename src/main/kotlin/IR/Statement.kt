package IR

import Visitors.Visitor

class Statement(body: IRNode): IRNode(body.getResultType()) {
    init {
        addChild(body)
    }

    override fun complexity() = getChild(0).complexity()

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}