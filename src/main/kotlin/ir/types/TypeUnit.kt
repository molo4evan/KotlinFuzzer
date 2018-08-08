package ir.types

class TypeUnit: Type("kotlin.Unit", Type.BUILTIN){
    override fun canCompareTo(t: Type) = false

    override fun canEquateTo(t: Type) = false
}