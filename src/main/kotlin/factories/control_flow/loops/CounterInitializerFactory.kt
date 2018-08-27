package factories.control_flow.loops

import factories.SafeFactory
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import ir.LiteralInitializer
import ir.control_flow.loops.CounterInitializer
import ir.types.Type

class CounterInitializerFactory(private val owner: Type?, private val value: Int): SafeFactory<CounterInitializer>() {
    override fun sproduce(): CounterInitializer {
        val init = LiteralInitializer(value, TypeList.INT)
        val resName = "var_${SymbolTable.getNextVariableNumber()}"
        val varInfo = VariableInfo(resName, owner, TypeList.INT, VariableInfo.LOCAL or VariableInfo.INITIALIZED)
        SymbolTable.add(varInfo)
        return CounterInitializer(varInfo, init)
    }
}