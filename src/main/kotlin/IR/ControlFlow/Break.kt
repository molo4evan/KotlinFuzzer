package IR.ControlFlow

import IR.IRNode
import Information.TypeList
import Visitors.Visitor

class Break(): IRNode(TypeList.NOTHING) {     //TODO: is correct?
    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)           //TODO: labels?
}