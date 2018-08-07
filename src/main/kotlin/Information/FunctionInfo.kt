package Information

import IR.Types.Type

class FunctionInfo(                         //TODO: change static modifier to companion objects???
        name: String,
        owner: Type?,
        retType: Type,
        val complexity: Long,
        flags: Int,
        val argTypes: List<VariableInfo>
): Symbol(name, owner, retType, flags) {

    companion object {
        val ABSTRACT = 0x40
        val NONRECURSIVE = 0x80
        val SYNCHRONIZED = 0x100
    }

    constructor(
            name: String,
            owner: Type?,
            retType: Type,
            complexity: Long,
            flags: Int,
            vararg argTypes: VariableInfo
    ) : this(name, owner, retType, complexity, flags, argTypes.asList())

    constructor(other: FunctionInfo) : this(
            other.name,
            other.owner,
            other.type,
            other.complexity,
            other.flags,
            other.argTypes
    )

    fun isSynchronized() = (flags and SYNCHRONIZED) > 0

    override fun copy() = this

    override fun deepCopy() = FunctionInfo(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other === null || other !is FunctionInfo) return false
        return try {
            owner == other.owner && hasEqualSignature(other)
        } catch (e: Exception) {
            false
        }
    }

    fun hasEqualSignature(other: FunctionInfo): Boolean{
        if (name == other.name){
            var i = if ((flags and STATIC) > 0) 0 else 1
            var j = if ((other.flags and STATIC) > 0) 0 else 1

            if (argTypes.size - i == other.argTypes.size - j){
                while (i < argTypes.size && j < other.argTypes.size){
                    if (argTypes[i++].type != other.argTypes[j++].type){
                        return false
                    }
                }
                return true
            }
        }
        return false
    }

    override fun hashCode() = name.hashCode()

    fun isConstructor(): Boolean {                  //???
        return name == owner?.getName() ?: false
    }

    override fun isStatic(): Boolean {
        return flags and STATIC > 0
    }

}