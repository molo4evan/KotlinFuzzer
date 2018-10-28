package factories.functoins

import factories.Factory
import factories.utils.IRNodeBuilder
import information.*
import ir.IRNode
import ir.NothingNode
import ir.functions.ArgumentDeclaration
import ir.functions.FunctionDefinition
import ir.functions.Return
import ir.types.Type
import utils.PseudoRandom

class FunctionDefinitionFactory (               //TODO: add result types
        private val name: String,
        private val ownerClass: Type?,
        private val resultType: Type?,
        private val complexityLimit: Long,
        private val statementLimit: Int,
        private val operatorLimit: Int,
        private val memberFunctionsArgLimit: Int,
        private val level: Long,
        private val flags: Int
): Factory<FunctionDefinition>() {
    override fun produce(): FunctionDefinition {
        var resType = resultType
        if (resType == null) {
            val types = TypeList.getAllForFunctions()
            resType = PseudoRandom.randomElement(types)
        }

        val argNumber = (PseudoRandom.random() * memberFunctionsArgLimit).toInt()
        val argDecl = mutableListOf<ArgumentDeclaration>()
        val argInfo = mutableListOf<VariableInfo>()
        if ((flags and Symbol.STATIC) != 0 && ownerClass != null) {
            argInfo.add(VariableInfo("this", ownerClass, ownerClass, VariableInfo.CONST or VariableInfo.LOCAL or VariableInfo.INITIALIZED))
        }
        val body: IRNode
        val returnNode: Return
        var functionInfo: FunctionInfo

        //println("Function $name constructing")

        SymbolTable.push()
        try {
            val builder = IRNodeBuilder().setArgumentType(ownerClass)
            var i = 0
            while (i < argNumber){
                val d = builder.setVariableNumber(i).getArgumentDeclarationFactory().produce()
                argDecl.add(d)
                val info = d.variableInfo
                info.setConst()
                argInfo.add(info)
                i++
            }

            if (ownerClass != null) {
                val thisClassFuns = SymbolTable.getAllCombined(ownerClass, FunctionInfo::class)
                val parentFuns = FunctionDefinition.getFuncsFromParents(ownerClass)
                while (true) {
                    functionInfo = FunctionInfo(name, ownerClass, resType, 0, flags, argInfo)
                    if (thisClassFuns.contains(functionInfo) || FunctionDefinition.isInvalidOverride(functionInfo, parentFuns)) {
                        // try changing the signature, and go checking again
                        val d = builder.setVariableNumber(i++).getArgumentDeclarationFactory().produce()
                        argDecl.add(d)
                        argInfo.add(d.variableInfo)
                    } else {
                        break
                    }
                }
            }

            val blockCompLimit = (PseudoRandom.random() * complexityLimit).toLong()
            body = builder.setOwnerClass(ownerClass).
                    setResultType(resType).
                    setComplexityLimit(blockCompLimit).
                    setStatementLimit(statementLimit).
                    setOperatorLimit(operatorLimit).
                    setLevel(level + 1).
                    setSubBlock(true).
                    setCanHaveBreaks(false).
                    setCanHaveContinues(false).
                    setCanHaveReturn(true).
                    getBlockFactory().produce()

            returnNode = if (resType != TypeList.UNIT) {
                builder.setComplexityLimit(complexityLimit - blockCompLimit).setExceptionSafe(false).getReturnFactory().produce()
            } else {
                Return(NothingNode())
            }
        } finally {
            SymbolTable.pop()
        }

        //println("Function $name constructed")
        //SymbolTable.printAll()

        functionInfo = FunctionInfo(name, ownerClass, resType, body.complexity(), flags, argInfo)
        // If it's all ok, add the function to the symbol table.
        SymbolTable.add(functionInfo)
        return FunctionDefinition(functionInfo, argDecl, body, returnNode)
    }
}