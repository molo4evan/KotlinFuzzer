package ir

import information.VariableInfo
import visitors.Visitor

abstract class Initialization protected constructor(val variableInfo: VariableInfo, initExpr: IRNode): IRNode(variableInfo.type) {
    init {
        addChild(initExpr)
    }

    override fun complexity() = getChild(0).complexity() + 1

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}