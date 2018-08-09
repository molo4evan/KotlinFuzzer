package ir.functions

import ir.IRNode
import ir.types.Type
import information.TypeList
import providers.visitors.Visitor

class FunctionDefinitionBlock(content: List<IRNode>, level: Long, owner: Type?): IRNode(TypeList.NOTHING) {     //TODO: correct?
    init {
        this.owner = owner
        this.level = level
        addChildren(content)
    }

    override fun complexity(): Long {
        var compl = 0L
        for (child in children){
            compl += child?.complexity() ?: 0
        }
        return compl
    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}