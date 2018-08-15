package ir.variables

import information.VariableInfo
import ir.types.Type
import providers.visitors.Visitor

class StaticMemberVariable(owner: Type, value: VariableInfo): VariableBase(value) {
    init {
        this.owner = owner
    }

    override fun complexity() = 1L

    override fun <T> accept(visitor: Visitor<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}