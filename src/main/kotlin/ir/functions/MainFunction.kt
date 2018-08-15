package ir.functions

import information.FunctionInfo
import information.TypeList
import ir.IRNode
import ir.NothingNode
import ir.PrintVariables
import providers.visitors.Visitor

class MainFunction(private val name: String, body: IRNode?, pv: PrintVariables): IRNode(TypeList.UNIT) {
    init {
        owner = null
        addChild(body ?: NothingNode())
        addChild(pv)
    }

    override fun getName() = name
    override fun complexity() = getChild(0).complexity() + getChild(1).complexity()

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}