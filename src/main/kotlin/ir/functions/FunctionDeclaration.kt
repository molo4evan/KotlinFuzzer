package ir.functions

import ir.IRNode
import information.FunctionInfo
import visitors.Visitor

class FunctionDeclaration(val functionInfo: FunctionInfo, argumentsDeclaration: List<ArgumentDeclaration>): IRNode(functionInfo.type) {

    init {
        addChildren(argumentsDeclaration)
        owner = functionInfo.owner
    }

    override fun complexity() = 0L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}