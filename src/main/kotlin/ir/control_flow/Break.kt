package ir.control_flow

import ir.IRNode
import information.TypeList
import providers.visitors.Visitor

class Break(): IRNode(TypeList.NOTHING) {     //TODO: is correct?
    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)           //TODO: labels?
}