package factories.rules

import factories.SafeFactory
import factories.rules.operators.BitwiseOperatorFactory
import factories.utils.IRNodeBuilder
import factories.utils.ProductionLimiter
import ir.IRNode
import ir.operators.OperatorKind
import ir.types.Type
import utils.ProductionParams

open class ExpressionFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean,
        noAssignments: Boolean
): SafeFactory<IRNode>() {
    private val rule = Rule<IRNode>("expression")

    init {
        val builder = IRNodeBuilder().
                setComplexityLimit(complexityLimit).
                setOperatorLimit(operatorLimit).
                setOwnerClass(ownerClass).
                setResultType(resultType).
                setExceptionSafe(exceptionSafe).
                setNoConsts(noconsts)

        if (!noconsts){
            rule.add("literal", builder.getLiteralFactory())
            rule.add("constant", builder.setIsConstant(true).setIsInitialized(true)/*.setVariableType(resultType)*/.getVariableFactory())
        }
        rule.add("variable", builder.setIsConstant(false).setIsInitialized(true).getVariableFactory(), 15.0) //TODO: add options
        if (operatorLimit > 0 && complexityLimit > 0){
            //rule.add("cast", builder.getCastOperatorFactory(), 0.1)                   //TODO: uncomment
            rule.add("arithmetic", builder.getArithmeticOperatorFactory())
            rule.add("logic", builder.getLogicOperatorFactory())
            rule.add("bitwise", BitwiseOperatorFactory(complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts))
            if (!noAssignments) rule.add("assignment", builder.getAssignmentOperatorFactory())
            rule.add("function", builder.getFunctionFactory(), ProductionParams.functionCallsPercent?.value() ?: 3.0)
            rule.add("str_plus", builder.setOperatorKind(OperatorKind.STRADD).getBinaryOperatorFactory())
        }
//        if (!ProductionParams.disableArrays.value() && !exceptionSafe) {          //TODO: uncomment
//            //rule.add("array_creation", builder.getArrayCreationFactory());
//            rule.add("array_element", builder.getArrayElementFactory())
//            rule.add("array_extraction", builder.getArrayExtractionFactory())
//        }
    }

    override fun sproduce(): IRNode {
        ProductionLimiter.limitProduction()
        return rule.produce()
    }
}