package ir

import information.VariableInfo
import providers.visitors.Visitor

abstract class Initialization protected constructor(val variableInfo: VariableInfo, initExpr: IRNode): IRNode(variableInfo.type) {
    init {
        addChild(initExpr)
    }

    override fun complexity() = getChild(0)?.complexity()?.plus(1) ?: 0L

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}