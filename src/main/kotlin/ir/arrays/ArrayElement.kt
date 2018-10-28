package ir.arrays

import ir.IRNode
import ir.types.TypeArray
import providers.visitors.Visitor

class ArrayElement(arrayExpr: IRNode, indexExpr: IRNode):   //TODO: implement VariableBase (how?)
        IRNode((arrayExpr.getResultType() as TypeArray).type) {
    init {
        addChild(arrayExpr)
        addChild(indexExpr)
    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}