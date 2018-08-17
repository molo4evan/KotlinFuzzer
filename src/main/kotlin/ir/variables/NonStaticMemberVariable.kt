package ir.variables

import information.VariableInfo
import ir.IRNode
import providers.visitors.Visitor

class NonStaticMemberVariable(obj: IRNode, value: VariableInfo): VariableBase(value) {
    init {
        addChild(obj)
    }

    override fun complexity() = getChild(0).complexity()

    override fun <T> accept(visitor: Visitor<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}