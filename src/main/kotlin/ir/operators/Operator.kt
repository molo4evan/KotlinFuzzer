package ir.operators

import ir.IRNode
import ir.types.Type

abstract class Operator(val opKind: OperatorKind, val priority: Int, resType: Type?): IRNode(resType) {
    constructor(opKind: OperatorKind, resType: Type?): this(opKind, opKind.priority, resType)

    enum class Order{
        LEFT,
        RIGHT
    }
}