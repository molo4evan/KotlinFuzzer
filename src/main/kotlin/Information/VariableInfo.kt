package Information

import IR.Types.Type

class VariableInfo(name: String, owner: Type?, type: Type, flags: Int): Symbol(name, owner, type, flags) {
    constructor(value: VariableInfo): this(value.name, value.owner, value.type, value.flags)
    constructor(owner: Type, type: Type): this("", owner, type, Symbol.NONE)

    companion object {
        val LOCAL = 0x40
        val INITIALIZED = 0x80
    }

    override fun copy() = VariableInfo(this)

    override fun deepCopy() = VariableInfo(this)

    fun isLocal() =  flags and LOCAL != 0
}