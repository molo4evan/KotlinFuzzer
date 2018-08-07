package IR.Variables

import Information.VariableInfo
import Visitors.Visitor

class LocalVariable(variableInfo: VariableInfo): VariableBase(variableInfo) {
    override fun complexity() = 1L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}