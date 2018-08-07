package IR.Operators

import IR.IRNode
import Visitors.Visitor

class UnaryOperator(operatorKind: OperatorKind, expr: IRNode): Operator(operatorKind, expr.getResultType()) {
    init {
        if (!operatorKind.isUnary()) throw Exception("Illegal operator kind: ${operatorKind.name}")
        addChild(expr)
    }

    override fun complexity() = children[0].complexity()

    fun isPrefix() = opKind.isPrefix

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}