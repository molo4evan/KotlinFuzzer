package factories.operators

import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import information.SymbolTable
import information.TypeList
import ir.IRNode
import ir.operators.BinaryOperator
import ir.operators.OperatorKind
import ir.types.Type
import utils.PseudoRandom

class BinaryLogicOperatorFactory(
        opKind: OperatorKind,
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
): BinaryOperatorFactory(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts) {

    override fun isApplicable(resultType: Type) = resultType == TypeList.BOOLEAN

    override fun generateTypes() = Pair(resultType, resultType)

    override fun generateProduction(leftType: Type, rightType: Type): BinaryOperator {
        val leftOpLimit = (PseudoRandom.randomDouble() * (operatorLimit - 1)).toInt()
        val rightOpLimit = operatorLimit - 1 - leftOpLimit
        val leftComplLimit = (PseudoRandom.randomDouble() * (complexityLimit - 1)).toLong()
        val rightComplLimit = complexityLimit - 1 - leftComplLimit

        if (leftOpLimit == 0 || rightOpLimit == 0 || leftComplLimit == 0L || rightComplLimit == 0L) {
            throw ProductionFailedException()
        }

        val swap = PseudoRandom.randomBoolean()
        val builder = IRNodeBuilder.setOwnerClass(owner).setExceptionSafe(exceptionSafe)

        val leftOperand = builder.setComplexityLimit(leftComplLimit)
                .setOperatorLimit(leftOpLimit)
                .setResultType(leftType)
                .setNoConsts(swap && noconsts)
                .getExpressionFactory()
                .produce()
        // Right branch won't necessarily execute. Ignore initalization performed in it.
        SymbolTable.push()
        val rightOperand: IRNode
        try {
            rightOperand = builder.setComplexityLimit(rightComplLimit)
                    .setOperatorLimit(rightOpLimit)
                    .setResultType(rightType)
                    .setNoConsts(!swap && noconsts)
                    .getExpressionFactory()
                    .produce()
        } finally {
            SymbolTable.pop()
        }
        return BinaryOperator(opKind, resultType, leftOperand, rightOperand)
    }
}