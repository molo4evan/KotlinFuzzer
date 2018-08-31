package factories.control_flow.loops

import exceptions.NotInitializedOptionException
import factories.SafeFactory
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import ir.LiteralInitializer
import ir.control_flow.loops.CounterInitializer
import ir.types.Type
import utils.ProductionParams

class CounterInitializerFactory(private val owner: Type?, private val value: Int): SafeFactory<CounterInitializer>() {
    override fun sproduce(): CounterInitializer {
        val init = LiteralInitializer(value, TypeList.INT)
        val resName = "var_${SymbolTable.getNextVariableNumber()}"
        val flags = if (ProductionParams.allowCounterMutation?.value() ?: throw NotInitializedOptionException("allowCounterMutation")) {
            VariableInfo.LOCAL or VariableInfo.INITIALIZED
        } else {
            VariableInfo.LOCAL or VariableInfo.INITIALIZED or VariableInfo.CONST
        }
        val varInfo = VariableInfo(resName, owner, TypeList.INT, flags)
        SymbolTable.add(varInfo)
        return CounterInitializer(varInfo, init)
    }
}