package factories.functoins

import factories.Factory
import factories.utils.IRNodeBuilder
import information.FunctionInfo
import information.SymbolTable
import information.TypeList
import information.VariableInfo
import ir.functions.ArgumentDeclaration
import ir.functions.FunctionDeclaration
import ir.functions.FunctionDefinition
import ir.types.Type
import utils.PseudoRandom

class FunctionDeclarationFactory(
        private val name: String,
        private val ownerClass: Type?,
        private val resultType: Type?,
        private val memberFunctionsArgLimit: Int,
        private val flags: Int
): Factory<FunctionDeclaration>() {
    override fun produce(): FunctionDeclaration {
        var resType = resultType
        if (resType == null) {
            val types = TypeList.getAll()
            types.add(TypeList.UNIT)
            resType = PseudoRandom.randomElement(types)
        }
        val argNumber = (PseudoRandom.random() * memberFunctionsArgLimit).toInt()
        val argInfo = mutableListOf<VariableInfo>()
        if (ownerClass != null) {
            argInfo.add(VariableInfo("this", ownerClass, ownerClass, VariableInfo.CONST or VariableInfo.LOCAL or VariableInfo.INITIALIZED))
        }
        val argumentsDeclaration = mutableListOf<ArgumentDeclaration>()
        SymbolTable.push()
        var functionInfo: FunctionInfo
        val builder = IRNodeBuilder().setArgumentType(ownerClass)
        try {
            var i = 0
            while (i < argNumber) {
                val d = builder.setVariableNumber(i).getArgumentDeclarationFactory().produce()
                argumentsDeclaration.add(d)
                argInfo.add(d.variableInfo)
                i++
            }
            if (ownerClass != null) {
                val thisClassFun = SymbolTable.getAllCombined(ownerClass, FunctionInfo::class)
                val parentFun = FunctionDefinition.getFuncsFromParents(ownerClass)
                while (true) {
                    functionInfo = FunctionInfo(name, ownerClass, resType, 0L, flags, argInfo)
                    if (thisClassFun.contains(functionInfo) || FunctionDefinition.isInvalidOverride(functionInfo, parentFun)) {
                        // try changing the signature, and go checking again.
                        val d = builder.setVariableNumber(i++).getArgumentDeclarationFactory().produce()
                        argumentsDeclaration.add(d)
                        argInfo.add(d.variableInfo)
                    } else {
                        break
                    }
                }
            }
        } finally {
            SymbolTable.pop()
        }
        functionInfo = FunctionInfo(name, ownerClass, resType, 0, flags, argInfo)
        // If it's all ok, add the function to the symbol table.
        SymbolTable.add(functionInfo)
        return FunctionDeclaration(functionInfo, argumentsDeclaration)
    }
}