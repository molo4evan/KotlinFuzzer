package ir

import ir.types.Type
import providers.visitors.Visitor
import kotlin.math.max

class Block(owner: Type?, returnType: Type, content: List<IRNode>, level: Long): IRNode(returnType) {
    init {
        this.owner = owner
        addChildren(content)
        this.level = level
    }

    protected val size = children.size

    override fun complexity() = children.stream().mapToLong{it?.complexity() ?: 0}.sum()

    override fun countDepth() = max(level, super.countDepth())

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}