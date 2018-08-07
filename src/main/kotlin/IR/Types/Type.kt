package IR.Types

import Exceptions.NotInitializedOptionException
import IR.IRNode
import Settings.ProductionParams
import Information.Symbol
import Information.SymbolTable
import Information.TypeList
import Visitors.Visitor
import java.util.*

open class Type(val typename: String, private var flags: Int = 0x00): IRNode(null), Comparable<Type> {
    companion object {
        val NONE = 0x00
        val OPEN = 0x01
        val INTERFACE = 0x02
        val ABSTRACT = 0x04
        val BUILTIN = 0x04
    }

    lateinit var parentClass: Type
    val parents: MutableSet<String> = HashSet()
    val childrenSet: MutableSet<String> = HashSet()
    private val symbols: MutableSet<Symbol> = HashSet()

    override fun getResultType(): Type? = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other === null || other !is Type) return false
        return typename == other.typename
    }

    override fun compareTo(other: Type) = typename.compareTo(other.typename)

    override fun hashCode() = typename.hashCode()

    fun addSymbol(s: Symbol) = symbols.add(s)

    fun exportSymbols() {
        symbols.stream().forEach { SymbolTable.add(it) }
    }

    override fun getName() = typename

    open fun canCompareTo(t: Type) = false

    open fun canEquateTo(t: Type) = true

    open fun canImplicityCastTo(other: Type): Boolean {
        if (isBuiltIn()) return false
        return this == other || getAllParents().contains(other)
    }

    fun getAllParents(): Set<Type>{
        val result = TreeSet<Type>()
        parents.stream().map { TypeList.find(it) }.map { it as Type }.forEach {
            result.add(it)
            result.addAll(getAllParents())
        }
        return result
    }

    open fun canExplicityCastTo(other: Type): Boolean {
        if (isBuiltIn() || other.isBuiltIn())
        if (this == other) return true
        if (other is Type && ProductionParams.disableDowncasts?.value()?.not() ?: throw NotInitializedOptionException("disableDowncasts")){
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

    fun isOpen() = flags and OPEN > 0

    fun isAbstract() = flags and ABSTRACT > 0

    fun setAbstract(){
        flags = flags or ABSTRACT
    }

    fun isInterface() = flags and INTERFACE > 0

    fun isBuiltIn() = flags and BUILTIN > 0

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}