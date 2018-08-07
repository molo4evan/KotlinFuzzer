package IR.Types

class TypeBoolean(): Type("kotlin.Boolean") {

    override fun canImplicityCastTo(other: Type) =  this == other

    override fun canExplicityCastTo(other: Type) =  this == other

    override fun canCompareTo(t: Type) = false

    override fun canEquateTo(t: Type) = this == t
}