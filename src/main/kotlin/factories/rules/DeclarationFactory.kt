package factories.rules

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.Factory
import factories.utils.IRNodeBuilder
import information.TypeList
import ir.Declaration
import ir.IRNode
import ir.types.Type
import utils.ProductionParams

internal class DeclarationFactory(
        private val ownerClass: Type?,
        private val complexityLimit: Long,
        private val operatorLimit: Int,
        private val isLocal: Boolean,
        private val exceptionSafe: Boolean
) : Factory<Declaration>() {
    private val rule = Rule<IRNode>("declaration")

    init {
        val builder = IRNodeBuilder().setOwnerClass(ownerClass)
                .setResultType(TypeList.UNIT)
                .setIsLocal(isLocal)
                .setComplexityLimit(complexityLimit)
                .setOperatorLimit(operatorLimit)
                .setIsLocal(isLocal)
                .setExceptionSafe(exceptionSafe)
        rule.add("decl", builder
                .setIsStatic(false)
                .getVariableDeclarationFactory())
        rule.add("decl_and_init", builder
                .setIsConstant(false)
                .setIsStatic(false)
                .getVariableInitializationFactory())
        if (ProductionParams.disableFinalVariables?.value()?.not() ?: throw NotInitializedOptionException("disableFinalVariables")) {   //TODO: settings
            rule.add("const_decl_and_init", builder
                    .setIsConstant(true)
                    .setIsStatic(false)
                    .getVariableInitializationFactory())
        }
        if (!(isLocal || ProductionParams.disableStatic?.value() ?: throw NotInitializedOptionException("disableStatic"))) {    //TODO: settings
            rule.add("static_decl", builder
                    .setIsConstant(false)
                    .setIsStatic(true)
                    .getVariableDeclarationFactory())
            rule.add("static_decl_and_init", builder
                    .setIsConstant(false)
                    .setIsStatic(true)
                    .getVariableInitializationFactory())
            if (ProductionParams.disableFinalVariables?.value()?.not() ?: throw NotInitializedOptionException("disableStatic")) {    //TODO: settings
                rule.add("static_const_decl_and_init", builder
                        .setIsConstant(true)
                        .setIsStatic(true)
                        .getVariableInitializationFactory())
            }
        }
    }


    override fun produce() =  Declaration(rule.produce())
}