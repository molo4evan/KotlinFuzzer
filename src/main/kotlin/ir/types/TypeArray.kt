package ir.types

import exceptions.ProductionFailedException
import information.Symbol
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import providers.visitors.Visitor
import utils.PseudoRandom
import java.util.*
import kotlin.collections.ArrayList

//TODO: in future maybe makes sense to add 'generic' behaviour/marker

open class TypeArray(typename: String, val type: Type): Type(typename, FINAL) {
    init {
        parents.add(TypeList.ANY.typename)
        parentClass = TypeList.ANY
    }

    constructor(type: Type): this("Array<${type.typename}>", type)

    override fun exportSymbols() {
        SymbolTable.add(VariableInfo("size", this, TypeList.INT, Symbol.PUBLIC))
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null || other !is TypeArray) return false
        if (super.equals(other)) return other.type == type
        return false
    }

    override fun hashCode() = 313 * (159 + Objects.hashCode(this.type)) //what a magic constants?!

    override fun compareTo(other: Type): Int {
        var result = super.compareTo(other)
        if (result == 0){
            try {
                val arr = other as TypeArray
                result = type.compareTo(arr.type)
            } catch (ex: Throwable) {}
        }
        return result
    }

    fun produce(): TypeArray {
        val all = ArrayList(TypeList.getAll())
        PseudoRandom.shuffle(all)
        for (type in all) {
            if (type is TypeArray) continue
            return TypeArray(type)
        }
        throw ProductionFailedException()
    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}