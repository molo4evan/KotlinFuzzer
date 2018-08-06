package IR.Types

import Exceptions.NotInitializedOptionException
import Settings.ProductionParams
import Information.Symbol
import Information.TypeList
import java.util.*

class ClassType(name: String, flags: Int = 0x00): Type(name) {
    companion object {
        val NONE = 0x00
        val FINAL = 0x01
        val INTERFACE = 0x02
        val ABSTRACT = 0x04
    }

    lateinit var parentClass: ClassType
    private val parents: MutableSet<String> = HashSet()
    private val childrenSet: MutableSet<String> = HashSet()
    private val symbols: MutableSet<Symbol> = HashSet()

    fun addSymbol(s: Symbol) = symbols.add(s)

    override fun canImplicityCastTo(other: Type): Boolean {
        if (other is ClassType) return this == other || getAllParents().contains(other)
        return false
    }

    fun getAllParents(): Set<ClassType>{
        val result = TreeSet<ClassType>()
        parents.stream().map { TypeList.find(it) }.map { it as ClassType }.forEach {
            result.add(it)
            result.addAll(getAllParents())
        }
        return result
    }

    override fun canExplicityCastTo(other: Type): Boolean {
        if (this == other) return true
        if (other is ClassType && ProductionParams.disableDowncasts?.value()?.not() ?: throw NotInitializedOptionException("disableDowncasts")){
            return getAllChildren().contains(other)
        }
        return false
    }

    fun getAllChildren(): Set<ClassType>{
        val result = TreeSet<ClassType>()
        childrenSet.stream().map { TypeList.find(it) }.map { it as ClassType }.forEach {
            result.add(it)
            result.addAll(getAllChildren())
        }
        return result
    }
}