package Information

import IR.Types.*
import Settings.ProductionParams

object TypeList {
    val ANY = Type("kotlin.Any", Type.OPEN)
    val NUMBER = TypeNumber()
    val BYTE = TypeByte()
    val SHORT = TypeShort()
    val INT = TypeInt()
    val LONG = TypeLong()
    val FLOAT = TypeFloat()
    val DOUBLE = TypeDouble()
    val UNIT = TypeUnit()
    val NOTHING = TypeNothing()
    val BOOLEAN = TypeBoolean()
    val CHAR = TypeChar()
    val STRING = Type("kotlin.String")       //what means final??

    private val TYPES = mutableListOf<Type>()
    private val BUILTIN_TYPES = mutableListOf<Type>()
    private val BUILTIN_INT_TYPES = mutableListOf<Type>()
    private val BUILTIN_FP_TYPES = mutableListOf<Type>()
    private val REFERENCE_TYPES = mutableListOf<Type>()

    init {
        BUILTIN_INT_TYPES.add(BYTE)
        BUILTIN_INT_TYPES.add(SHORT)
        BUILTIN_INT_TYPES.add(INT)
        BUILTIN_INT_TYPES.add(LONG)
        BUILTIN_FP_TYPES.add(FLOAT)
        BUILTIN_FP_TYPES.add(DOUBLE)

        BUILTIN_TYPES.addAll(BUILTIN_INT_TYPES)     //TODO: change addition?
        BUILTIN_TYPES.addAll(BUILTIN_FP_TYPES)

        BUILTIN_TYPES.add(BOOLEAN)
        BUILTIN_TYPES.add(CHAR)
        BUILTIN_TYPES.add(NUMBER)
        BUILTIN_TYPES.add(UNIT)
        BUILTIN_TYPES.add(NOTHING)


        TYPES.addAll(BUILTIN_TYPES)

        if (ProductionParams.disableArrays?.value() == false) {
            TYPES.addAll(REFERENCE_TYPES)
        }

        STRING.parents.add(ANY.getName())
        STRING.parentClass = ANY
        //NUMBER.parents.add(ANY.getName())     //TODO: mb add parentness for other builtin types?
        //NUMBER.parentClass = ANY

        add(STRING)
        add(ANY)
    }

    fun getAll() = TYPES

    fun getBuiltin() = BUILTIN_TYPES

    fun getBuiltInInt()= BUILTIN_INT_TYPES

    fun getBuiltInFP() = BUILTIN_FP_TYPES

    fun getReferenceTypes() = REFERENCE_TYPES

    fun isBuiltInFP(t: Type) =  BUILTIN_FP_TYPES.contains(t)

    fun isBuiltInInt(t: Type) = BUILTIN_INT_TYPES.contains(t)

    fun isBuiltIn(t: Type) = isBuiltInInt(t) || isBuiltInFP(t) || t.equals(NOTHING)

    fun isIn(t: Type) = TYPES.contains(t)

    fun isReferenceType(t: Type) = REFERENCE_TYPES.contains(t)

    fun find(name: String): Type {
        for (t in TYPES){
            if (t.getName() == name) return t
        }
        throw IllegalArgumentException("No class found: $name")
    }

    fun add(t: Type){
        REFERENCE_TYPES.add(t)
        TYPES.add(t)
    }

    fun remove(t: Type) {
        REFERENCE_TYPES.remove(t)
        TYPES.remove(t)
    }

    fun removeAll() {
        TYPES.removeIf { it.getName().startsWith("Test_")}
        REFERENCE_TYPES.removeIf{ it.getName().startsWith("Test_")}
    }
}






















