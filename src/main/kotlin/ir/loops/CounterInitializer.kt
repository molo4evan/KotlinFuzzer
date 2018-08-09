package ir.loops

import ir.IRNode
import ir.Initialization
import information.VariableInfo
import providers.visitors.Visitor

class CounterInitializer(varInfo: VariableInfo, initExpr: IRNode): Initialization(varInfo, initExpr){
    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}