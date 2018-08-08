package ir.functions

import ir.IRNode
import ir.types.Type
import information.TypeList
import visitors.Visitor

class FunctionDeclarationBlock(owner: Type?, content: List<IRNode>, level: Long): IRNode(TypeList.NOTHING) {    //TODO: is correct?
    init {
        this.owner = owner
        this.level = level
        addChildren(content)
    }

    override fun complexity(): Long {
        var complexity = 0L
        for (child in children){
            complexity += child.complexity()
        }
        return complexity
    }

    fun size() = children.size

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}