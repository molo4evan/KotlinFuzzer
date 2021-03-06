package ir.types

import exceptions.NotInitializedOptionException
import information.Symbol
import information.SymbolTable
import information.TypeList
import ir.IRNode
import providers.visitors.Visitor
import utils.ProductionParams
import java.util.*

open class Type(val typename: String, private var flags: Int = 0x00): IRNode(null), Comparable<Type> {
    companion object {
        val NONE = 0x00
        val FINAL = 0x01
        val INTERFACE = 0x02
        val ABSTRACT = 0x04
        val BUILTIN = 0x08
        val ENUM = 0x10
        val NULLABLE = 0x20
    }

    lateinit var parentClass: Type
    val parents: MutableSet<String> = HashSet()
    val childrenSet: MutableSet<String> = HashSet()
    private val symbols: MutableSet<Symbol> = HashSet()

    override fun getResultType(): Type = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other === null || other !is Type) return false
        return typename == other.typename && isNullable() == other.isNullable()
    }

    override fun compareTo(other: Type) = typename.compareTo(other.typename)

    override fun hashCode() = typename.hashCode()

    fun addSymbol(s: Symbol) = symbols.add(s)

    open fun exportSymbols() {
        symbols.stream().forEach { SymbolTable.add(it) }
    }

    override fun getName() = typename

    open fun canCompareTo(t: Type) = false

    open fun canEquateTo(t: Type) = true

    open fun canImplicitlyCastTo(other: Type): Boolean {
        if (this == other) return true
        if (isBuiltIn()) return false
        return getAllParents().contains(other)
    }

    fun getAllParents(): Set<Type>{
        val result = TreeSet<Type>()
        parents.stream().map { TypeList.find(it) }.forEach {
            result.add(it)
            result.addAll(it.getAllParents())
        }
        return result
    }

    open fun canExplicitlyCastTo(other: Type): Boolean {
        if (this == other) return true
        if (isBuiltIn() || other.isBuiltIn()) return false
        if (ProductionParams.disableDowncasts?.value()?.not() ?: throw NotInitializedOptionException("disableDowncasts")){
            return getAllChildren().contains(other)
        }
        return false
    }

    fun getAllChildren(): Set<Type>{
        val result = TreeSet<Type>()
        childrenSet.stream().map { TypeList.find(it) }.map { it as Type }.forEach {
            result.add(it)
            result.addAll(getAllChildren())
        }
        return result
    }

    fun isOpen() = flags and FINAL == 0

    fun isAbstract() = flags and ABSTRACT != 0

    fun isNullable() = flags and NULLABLE != 0

    fun isInterface() = flags and INTERFACE != 0

    fun isBuiltIn() = flags and BUILTIN != 0

    fun isEnum() = flags and ENUM != 0

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}