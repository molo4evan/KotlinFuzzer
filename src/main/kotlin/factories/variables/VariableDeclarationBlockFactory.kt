package factories.variables

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.Factory
import factories.utils.IRNodeBuilder
import ir.Declaration
import ir.types.Type
import ir.variables.VariableDeclarationBlock
import utils.ProductionParams
import utils.PseudoRandom

internal class VariableDeclarationBlockFactory(
        private val ownerClass: Type?,
        private val complexityLimit: Long,
        private val operatorLimit: Int,
        private val level: Long,
        private val exceptionSafe: Boolean
) : Factory<VariableDeclarationBlock>() {

    override fun produce(): VariableDeclarationBlock {
        val content = ArrayList<Declaration>()
        val limit = Math.ceil(PseudoRandom.randomDouble() * (ProductionParams.dataMemberLimit?.value() ?: throw NotInitializedOptionException("dataMemberLimit"))).toInt()
        val declFactory = IRNodeBuilder
                .setOwnerClass(ownerClass)
                .setComplexityLimit(complexityLimit)
                .setOperatorLimit(operatorLimit)
                .setIsLocal(false)
                .setExceptionSafe(exceptionSafe)
                .getDeclarationFactory()
        for (i in 0 until limit) {
            try {
                content.add(declFactory.produce())
            } catch (e: ProductionFailedException) {
            }

        }
        return VariableDeclarationBlock(content, level)
    }
}