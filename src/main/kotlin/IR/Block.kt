package IR

import IR.Types.Type
import Visitors.Visitor
import kotlin.math.max

class Block(owner: Type, returnType: Type, content: List<out IRNode>): IRNode(returnType) {
    init {
        this.owner = owner
        addChildren(content)
        this.level = level
    }

    protected val size = children.size

    override fun complexity() = children.stream().mapToLong(IRNode::complexity).sum()

    override fun countDepth() = max(level, super.countDepth())

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}