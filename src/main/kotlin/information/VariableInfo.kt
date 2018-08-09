package information

import ir.types.Type

class VariableInfo(name: String, owner: Type?, type: Type?, flags: Int): Symbol(name, owner, type, flags) {
    constructor(value: VariableInfo): this(value.name, value.owner, value.type, value.flags)
    constructor(owner: Type, type: Type): this("", owner, type, Symbol.NONE)

    companion object {
        const val CONST = 0x20
        const val LOCAL = 0x40
        const val INITIALIZED = 0x80
    }

    override fun copy() = VariableInfo(this)

    override fun deepCopy() = VariableInfo(this)

    fun isLocal() =  flags and LOCAL != 0

    fun isiInitialized() = flags and INITIALIZED != 0

    fun initialize(){
        flags = flags or INITIALIZED
    }
}