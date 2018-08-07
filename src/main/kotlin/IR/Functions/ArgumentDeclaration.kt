package IR.Functions

import IR.IRNode
import Information.VariableInfo
import Visitors.Visitor

class ArgumentDeclaration(val variableInfo: VariableInfo): IRNode(variableInfo.type) {
    override fun complexity() = 0L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}