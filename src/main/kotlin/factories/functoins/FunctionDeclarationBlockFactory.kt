package factories.functoins

import exceptions.ProductionFailedException
import factories.Factory
import factories.utils.IRNodeBuilder
import information.FunctionInfo
import information.Symbol
import ir.IRNode
import ir.functions.FunctionDeclarationBlock
import ir.types.Type
import utils.PseudoRandom

class FunctionDeclarationBlockFactory(
        private val owner: Type,
        private val memberFunctionsLimit: Int,
        private val memberFunctionsArgLimit: Int,
        private val level: Long
): Factory<FunctionDeclarationBlock>() {
    override fun produce(): FunctionDeclarationBlock {
        val content = mutableListOf<IRNode>()
        val memFunLimit = (PseudoRandom.random() * memberFunctionsLimit).toInt()
        if (memFunLimit > 0) {
            val builder = IRNodeBuilder().
                    setOwnerClass(owner).
                    setMemberFunctionsArgLimit(memberFunctionsArgLimit).
                    setFlags(FunctionInfo.ABSTRACT or Symbol.PUBLIC)
            for (i in 0 until memFunLimit) {
                try {
                    content.add(builder.setName("fun_$i").getFunctionDeclarationFactory().produce())
                } catch (ex: ProductionFailedException){
                    // TODO: do we have to react here?
                }
            }
        }
        return FunctionDeclarationBlock(owner, content, level)
    }
}