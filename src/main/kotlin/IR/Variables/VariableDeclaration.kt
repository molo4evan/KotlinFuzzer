package IR.Variables

import IR.IRNode
import Information.VariableInfo
import Visitors.Visitor

class VariableDeclaration(val variableInfo: VariableInfo): IRNode(variableInfo.type) {
    override fun getName() = variableInfo.name

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}
