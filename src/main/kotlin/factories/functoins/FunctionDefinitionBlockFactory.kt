package factories.functoins

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.Factory
import factories.utils.IRNodeBuilder
import information.FunctionInfo
import information.Symbol
import information.SymbolTable
import information.VariableInfo
import ir.IRNode
import ir.functions.FunctionDefinitionBlock
import ir.types.Type
import utils.ProductionParams
import utils.PseudoRandom

class FunctionDefinitionBlockFactory(
        private val ownerClass: Type?,
        private val memberFunctionsLimit: Int,
        private val memberFunctionsArgLimit: Int,
        private val complexityLimit: Long,
        private val statementLimit: Int,
        private val operatorLimit: Int,
        private val level: Long,
        private val initialFlags: Int
): Factory<FunctionDefinitionBlock>() {
    override fun produce(): FunctionDefinitionBlock {
        val content = mutableListOf<IRNode>()
        val memFunLimit = (PseudoRandom.random() * memberFunctionsLimit).toLong()
        if (memFunLimit > 0) {
            val memFunCompl = complexityLimit / memFunLimit
            val builder = IRNodeBuilder().
                    setOwnerClass(ownerClass).
                    setComplexityLimit(memFunCompl).
                    setStatementLimit(statementLimit).
                    setOperatorLimit(operatorLimit).
                    setMemberFunctionsArgLimit(memberFunctionsArgLimit).
                    setLevel(level)
            for (i in 0 until memFunLimit) {
                var flags = initialFlags

                if (ownerClass != null) {
                    if (PseudoRandom.randomBoolean()) {
                        flags = flags or Symbol.STATIC
                    }
                    if (PseudoRandom.randomBoolean() &&
                            ProductionParams.disableFinalMethods?.value()?.not()
                            ?: throw NotInitializedOptionException("disableFinalMethods")) {
                        flags = flags or FunctionInfo.FINAL
                    }
                    if (PseudoRandom.randomBoolean()) {
                        flags = flags or FunctionInfo.SYNCHRONIZED
                    }
                } else {
                    flags = flags or FunctionInfo.FINAL
                }

                if (PseudoRandom.randomBoolean()) {
                    flags = flags or FunctionInfo.NONRECURSIVE
                }

                if (ownerClass == null) flags = flags or Symbol.PUBLIC
                else {
                    when ((PseudoRandom.random() * 4).toInt()) {
                        0 -> flags = if (flags and FunctionInfo.FINAL != 0) flags or Symbol.PRIVATE else flags or Symbol.PUBLIC
                        1 -> flags = flags or Symbol.PROTECTED
                        2 -> flags = flags or Symbol.INTERNAL
                        3 -> flags = flags or Symbol.PUBLIC
                    }
                }

                var thisSymbol: Symbol? = null
                if (flags and Symbol.STATIC != 0) {
                    thisSymbol = SymbolTable.get("this", VariableInfo::class)
                    SymbolTable.remove(thisSymbol)
                }

                try {
                    content.add(builder.
                            setName("fun_$i").
                            setFlags(flags).
                            getFunctionDefinitionFactory().produce())
                } catch (ex: ProductionFailedException) {}

                if (flags and Symbol.STATIC != 0) {
                    SymbolTable.add(thisSymbol)
                }
            }
        }
        return FunctionDefinitionBlock(content, level, ownerClass)
    }
}