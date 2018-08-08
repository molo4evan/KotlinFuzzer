package ir.types

class TypeBoolean(): Type("kotlin.Boolean", Type.BUILTIN) {

    override fun canImplicitlyCastTo(other: Type) =  this == other

    override fun canExplicitlyCastTo(other: Type) =  this == other

    override fun canCompareTo(t: Type) = false

    override fun canEquateTo(t: Type) = this == t
}