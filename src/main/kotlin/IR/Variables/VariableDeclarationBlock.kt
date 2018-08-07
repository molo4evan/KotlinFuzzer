package IR.Variables

import IR.Declaration
import IR.IRNode
import Information.TypeList
import Visitors.Visitor

class VariableDeclarationBlock(content: List<Declaration>, level: Long): IRNode(TypeList.NOTHING) {     //TODO: is correct?
    init {
        this.level = level
        addChildren(content)
    }

    override fun complexity() = children.stream().mapToLong(IRNode::complexity).sum()

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}
