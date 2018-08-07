package IR

import IR.Types.Type
import Visitors.Visitor

open class Literal(val value: Any, type: Type): IRNode(type) {
    override fun complexity() = 0L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}