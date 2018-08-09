package ir.operators

import ir.IRNode
import ir.types.Type
import providers.visitors.Visitor

class CastOperator(resultType: Type, casted: IRNode): Operator( OperatorKind.CAST, resultType) {
    init {
        addChild(casted)
    }

    override fun complexity() = children[0].complexity() + 1

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}