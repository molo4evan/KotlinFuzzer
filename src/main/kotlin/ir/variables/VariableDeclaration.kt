package ir.variables

import ir.IRNode
import information.VariableInfo
import providers.visitors.Visitor

class VariableDeclaration(val variableInfo: VariableInfo): IRNode(variableInfo.type) {
    override fun getName() = variableInfo.name

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}
