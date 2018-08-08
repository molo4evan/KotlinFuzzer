package factories

import exceptions.ProductionFailedException
import ir.IRNode
import information.SymbolTable

abstract class SafeFactory<T : IRNode> : Factory<T>() {
    protected abstract fun sproduce(): T

    override fun produce(): T {
        try {
            SymbolTable.push()
            val p = sproduce()
            SymbolTable.merge()
            return p
        } catch (e: ProductionFailedException) {
            SymbolTable.pop()
            throw e
        }

    }
}