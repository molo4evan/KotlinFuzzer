package factories.rules

import factories.SafeFactory
import factories.rules.operators.BitwiseOperatorFactory
import factories.utils.IRNodeBuilder
import factories.utils.ProductionLimiter
import information.TypeList
import ir.IRNode
import ir.operators.OperatorKind
import ir.types.Type
import ir.types.TypeLong
import utils.ProductionParams

open class ExpressionFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        exceptionSafe: Boolean,
        noconsts: Boolean
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
        rule.add("variable", builder.setIsConstant(false).setIsInitialized(true).getVariableFactory()) //TODO: add options
        if (operatorLimit > 0 && complexityLimit > 0){
            rule.add("cast", builder.getCastOperatorFactory(), 0.1)                   //TODO: uncomment
            if (resultType != TypeList.BOOLEAN) rule.add("arithmetic", builder.getArithmeticOperatorFactory())
            if (resultType == TypeList.BOOLEAN) rule.add("logic", builder.getLogicOperatorFactory())
            if (resultType != TypeList.BOOLEAN) rule.add("bitwise", BitwiseOperatorFactory(complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts))
            //if (!noAssignments && resultType != TypeList.BOOLEAN) rule.add("assignment", builder.getAssignmentOperatorFactory())      // assignment isn't expression (?)
            if (resultType == TypeList.STRING) rule.add("str_plus", builder.setOperatorKind(OperatorKind.STRADD).getBinaryOperatorFactory())
            rule.add("function", builder.getFunctionCallFactory(), ProductionParams.functionCallsPercent?.value() ?: 0.1)

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