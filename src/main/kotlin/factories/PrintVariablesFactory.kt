package factories

import ir.PrintVariables
import ir.types.Type

class PrintVariablesFactory(private val owner: Type?, private val level: Long): Factory<PrintVariables>() {
    override fun produce() = PrintVariables(owner, level)
}