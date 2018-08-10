package information

import ir.types.Type

open class Symbol(val name: String, val owner: Type?, val type: Type, var flags: Int) {
    //protected constructor(name: String) : this(name, null, null, NONE)
    protected constructor(other: Symbol): this(other.name, other.owner, other.type, other.flags)

    companion object {
        val NONE = 0x00
        val PRIVATE = 0x01
        val INTERNAL = 0x02
        val PROTECTED = 0x04
        val PUBLIC = 0x08
        val ACCESS_ATTRS_MASK = PRIVATE + PROTECTED + INTERNAL + PUBLIC
        val STATIC = 0x10
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other == null || other !is Symbol) return false

        return try{
            val s = other as Symbol
            owner == s.owner && name == s.name
        } catch (e: Exception) {
            false
        }
    }

    override fun hashCode() = name.hashCode()

    open fun isStatic() = (flags and STATIC) != 0

    fun isPrivate() = flags and PRIVATE != 0

    fun isInternal() = flags and INTERNAL != 0

    fun isProtected() = flags and PROTECTED != 0

    fun isPublic() = flags and PUBLIC != 0

    open fun copy() = Symbol(this)

    open fun deepCopy() = Symbol(this)
}