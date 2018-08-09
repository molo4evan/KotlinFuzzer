package ir.functions

import ir.IRNode
import ir.types.Type
import information.FunctionInfo
import information.Symbol
import information.SymbolTable
import providers.visitors.Visitor

class FunctionDefinition(
        val functionInfo: FunctionInfo,
        argumentDeclarations: List<ArgumentDeclaration>,
        body: IRNode?,
        ret: Return?
): IRNode(functionInfo.type) {
    init {
        owner = functionInfo.owner
        addChild(body)
        addChild(ret)
        addChildren(argumentDeclarations)
    }

    companion object {

        // get the list of all functions from all parents of the given class.
        fun getFuncsFromParents(type: Type): List<Symbol> {
            val result = mutableListOf<Symbol>()
            for (parent in type.getAllParents()) {
                result.addAll(SymbolTable.getAllCombined(parent, FunctionInfo::class))
            }
            return result
        }

        // Check if the given function prototype f1 is a valid overload of
        // prototypes in collection S.
        // The override is invalid if function f1 has the same signature as
        // function f2 in S, but has different return type.
        fun isInvalidOverride(f1: FunctionInfo, symbols: Collection<Symbol>): Boolean {
            for (symbol in symbols) {
                val f2 = symbol as FunctionInfo
                if (f1.hasEqualSignature(f2)) {
                    if (f1.type != f2.type) {
                        return true
                    }
                    if (f2.isNonRecursive()
                            || f1.isAbstract() && !f2.isAbstract()
                            || f1.isStatic() != f2.isStatic()
                            || !f2.isOpen()
                            || f1.flags and Symbol.ACCESS_ATTRS_MASK < f2.flags and Symbol.ACCESS_ATTRS_MASK) {
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun complexity(): Long {
        val body = getChild(0)
        var ret: IRNode?
        try {
            ret = getChild(1)
        } catch (ex: ArrayIndexOutOfBoundsException){
            return body?.complexity() ?: 0
        }
        return (body?.complexity() ?: 0) + (ret?.complexity() ?: 0)
    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}