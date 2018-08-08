package ir.controlflow

import ir.IRNode
import information.TypeList
import visitors.Visitor

class Break(): IRNode(TypeList.NOTHING) {     //TODO: is correct?
    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)           //TODO: labels?
}