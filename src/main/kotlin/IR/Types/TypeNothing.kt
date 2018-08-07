package IR.Types

import Visitors.Visitor

class TypeNothing: Type("kotlin.Nothing") {
    override fun canCompareTo(t: Type) = false

    override fun canEquateTo(t: Type) = false
}