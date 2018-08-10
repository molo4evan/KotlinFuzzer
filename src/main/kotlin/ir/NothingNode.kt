package ir

import information.TypeList
import providers.visitors.Visitor

class NothingNode: IRNode(TypeList.NOTHING) {
    override fun complexity() = 0L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}