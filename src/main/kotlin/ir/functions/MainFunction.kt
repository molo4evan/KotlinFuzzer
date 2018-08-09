package ir.functions

import information.FunctionInfo
import information.TypeList
import ir.IRNode
import providers.visitors.Visitor

class MainFunction(body: IRNode?): IRNode(TypeList.UNIT) {
    init {
        owner = null
        addChild(body)
    }

    override fun complexity() = getChild(0)?.complexity() ?: 0L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}