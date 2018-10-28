package ir.arrays

import ir.IRNode
import ir.Literal
import ir.types.TypeArray
import ir.variables.VariableDeclaration
import providers.visitors.Visitor

class ArrayCreation(                        //TODO: add lambda for array initializing
        val variable: VariableDeclaration,
        val arrayType: TypeArray,
        sizeExpr: IRNode    //not really used now, current creation is just emptyArray<arrayType>()
): IRNode(arrayType) {
    val size: Int

    init {
        addChild(sizeExpr)
        size = if (sizeExpr is Literal) {
            sizeExpr.value as Int
        } else 0

    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}