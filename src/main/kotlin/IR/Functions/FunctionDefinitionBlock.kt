package IR.Functions

import IR.IRNode
import IR.Types.Type
import Information.TypeList
import Visitors.Visitor

class FunctionDefinitionBlock(content: List<IRNode>, level: Long, owner: Type): IRNode(TypeList.NOTHING) {     //TODO: correct?
    init {
        this.owner = owner
        this.level = level
        addChildren(content)
    }

    override fun complexity(): Long {
        var compl = 0L
        for (child in children){
            compl += child.complexity()
        }
        return compl
    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}