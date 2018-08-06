package Visitors

import IR.IRNode

interface Visitor<T> {
    fun visit(node: IRNode): T
}