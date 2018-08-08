package factories

import factories.utils.IRNodeBuilder
import ir.functions.Return
import ir.types.Type

internal class ReturnFactory(
        private val complexityLimit: Long,
        private val operatorLimit: Int,
        private val ownerClass: Type?,
        private val resultType: Type,
        private val exceptionSafe: Boolean
) : SafeFactory<Return>() {

    override fun sproduce(): Return {
        return Return(IRNodeBuilder.setComplexityLimit(complexityLimit - 1)
                .setOperatorLimit(operatorLimit - 1)
                .setOwnerClass(ownerClass)
                .setResultType(resultType)
                .setExceptionSafe(exceptionSafe)
                .setNoConsts(false)
                .getLimitedExpressionFactory()
                .produce())
    }
}
