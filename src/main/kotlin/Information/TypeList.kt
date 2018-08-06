package Information

import IR.Types.ClassType
import IR.Types.Type

object TypeList {
    val VOID = TypeVoid()
    val BOOLEAN = TypeBoolean()
    val BYTE = TypeByte()
    val CHAR = TypeChar()
    val SHORT = TypeShort()
    val INT = TypeInt()
    val LONG = TypeLong()
    val FLOAT = TypeFloat()
    val DOUBLE = TypeDouble()
    val ANY = ClassType("kotlin.Any")
    val STRING = ClassType("kotlin.String", ClassType.FINAL)       //what means final??

    private val TYPES = mutableListOf<Type>()
    private val BUILTIN_TYPES = mutableListOf<Type>()
    private val BUILTIN_INT_TYPES = mutableListOf<Type>()
    private val BUILTIN_FP_TYPES = mutableListOf<Type>()
    private val REFERENCE_TYPES = mutableListOf<Type>()

    fun find(name: String): Type {

    }
}