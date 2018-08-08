package ir.functions

import ir.IRNode
import information.VariableInfo
import visitors.Visitor

class ArgumentDeclaration(val variableInfo: VariableInfo): IRNode(variableInfo.type) {
    override fun complexity() = 0L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}