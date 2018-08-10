package ir.functions

import information.FunctionInfo
import information.TypeList
import ir.IRNode
import ir.NothingNode
import providers.visitors.Visitor

class MainFunction(private val name: String, body: IRNode?): IRNode(TypeList.UNIT) {
    init {
        owner = null
        addChild(body ?: NothingNode())
    }

    override fun getName() = name
    override fun complexity() = getChild(0).complexity()

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}