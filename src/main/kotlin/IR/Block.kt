package IR

import IR.Types.ClassType
import IR.Types.Type
import kotlin.math.max

class Block(owner: ClassType, returnType: Type, content: List<out IRNode>): IRNode(returnType) {
    init {
        this.owner = owner
        addChildren(content)
        this.level = level
    }

    protected val size = children.size

    override fun complexity() = children.stream().mapToLong(IRNode::complexity).sum()

    override fun countDepth() = max(level, super.countDepth())
}