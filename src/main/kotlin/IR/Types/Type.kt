package IR.Types

import IR.IRNode

abstract class Type(val typename: String): IRNode(null), Comparable<Type> {
    override fun getResultType(): Type? = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other === null || other !is Type) return false
        return typename == other.typename
    }

    override fun compareTo(other: Type) = typename.compareTo(other.typename)

    override fun hashCode() = typename.hashCode()

    abstract fun canImplicityCastTo(other: Type): Boolean

    abstract fun canExplicityCastTo(other: Type): Boolean

    //TODO: exportSymbols?
}