package ir.types

class TypeUnit: Type("Unit", Type.BUILTIN){
    override fun canCompareTo(t: Type) = false

    override fun canEquateTo(t: Type) = false
}