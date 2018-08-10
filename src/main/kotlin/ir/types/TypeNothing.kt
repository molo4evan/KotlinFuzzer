package ir.types

class TypeNothing: Type("Nothing", Type.BUILTIN) {
    override fun canCompareTo(t: Type) = false

    override fun canEquateTo(t: Type) = false
}