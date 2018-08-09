package ir.variables

import ir.Declaration
import ir.IRNode
import information.TypeList
import providers.visitors.Visitor

class VariableDeclarationBlock(content: List<Declaration>, level: Long): IRNode(TypeList.NOTHING) {     //TODO: is correct?
    init {
        this.level = level
        addChildren(content)
    }

    override fun complexity() = children.stream().mapToLong{it?.complexity() ?: 0}.sum()

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}
