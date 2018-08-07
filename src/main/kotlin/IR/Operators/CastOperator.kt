package IR.Operators

import IR.IRNode
import IR.Types.Type
import Visitors.Visitor

class CastOperator(resultType: Type, casted: IRNode): Operator( OperatorKind.CAST, resultType) {
    init {
        addChild(casted)
    }

    override fun complexity() = children[0].complexity() + 1

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}