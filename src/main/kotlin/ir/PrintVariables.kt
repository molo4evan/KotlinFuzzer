package ir

import information.Symbol
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import ir.types.Type
import providers.visitors.Visitor

class PrintVariables(owner: Type?, level: Long): IRNode(TypeList.NOTHING) {
    val vars: List<Symbol>

    init {
        this.owner = owner
        this.level = level
        vars = SymbolTable.getAllCombined(owner, VariableInfo::class)
    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}