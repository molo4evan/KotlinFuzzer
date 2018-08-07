package IR.Types

class TypeUnit: Type("kotlin.Unit"){
    override fun canCompareTo(t: Type) = false

    override fun canEquateTo(t: Type) = false
}