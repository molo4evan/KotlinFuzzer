package IR.Functions

import IR.IRNode
import Information.FunctionInfo
import Visitors.Visitor

class FunctionDeclaration(val functionInfo: FunctionInfo, argumentsDeclaration: List<ArgumentDeclaration>): IRNode(functionInfo.type) {

    init {
        addChildren(argumentsDeclaration)
        owner = functionInfo.owner
    }

    override fun complexity() = 0L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}