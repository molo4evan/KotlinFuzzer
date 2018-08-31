package factories.utils

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.*
import factories.control_flow.BreakFactory
import factories.control_flow.ContinueFactory
import factories.control_flow.IfFactory
import factories.control_flow.WhenFactory
import factories.control_flow.loops.*
import factories.functoins.*
import factories.operators.CastOperatorFactory
import factories.operators.RangeOperatorFactory
import factories.operators.binary.*
import factories.operators.unary.IncDecOperatorFactory
import factories.operators.unary.LogicalInversionOperatorFactory
import factories.operators.unary.UnaryPlusMinusOperatorFactory
import factories.rules.*
import factories.rules.operators.ArithmeticOperatorFactory
import factories.rules.operators.AssignmentOperatorFactory
import factories.rules.operators.LogicOperatorFactory
import factories.variables.LocalVariableFactory
import factories.variables.VariableDeclarationBlockFactory
import factories.variables.VariableDeclarationFactory
import factories.variables.VariableInitializationFactory
import information.FunctionInfo
import information.TypeList
import ir.*
import ir.control_flow.Break
import ir.control_flow.Continue
import ir.control_flow.If
import ir.control_flow.When
import ir.control_flow.loops.*
import ir.functions.*
import ir.operators.*
import ir.operators.OperatorKind.*
import ir.types.Type
import ir.types.TypeNothing
import ir.variables.*
import utils.ProductionParams
import java.util.*

class IRNodeBuilder {
    private var argumentType = Optional.empty<Type?>()
    private var variableNumber = Optional.empty<Int>()
    private var complexityLimit = Optional.empty<Long>()
    private var operatorLimit = Optional.empty<Int>()
    private var ownerClass = Optional.empty<Type>()
    private var resultType = Optional.empty<Type>()
    private var safe = Optional.empty<Boolean>()
    private var noConsts = Optional.empty<Boolean>()
    private var opKind = Optional.empty<OperatorKind>()
    private var statementLimit = Optional.empty<Int>()
    private var subBlock = Optional.empty<Boolean>()
    private var canHaveBreaks = Optional.empty<Boolean>()
    private var canHaveContinues = Optional.empty<Boolean>()
    private var canHaveReturn = Optional.empty<Boolean>()
    //not in use yet because 'throw' is only placed to the locations where 'return' is allowed
    private var canHaveThrow = Optional.empty<Boolean>()
    private var level = Optional.empty<Long>()
    private var prefix = Optional.empty<String>()
    private var memberFunctionsLimit = Optional.empty<Int>()
    private var memberFunctionsArgLimit = Optional.empty<Int>()
    private var localVariable = Optional.empty<LocalVariable>()
    private var isLocal = Optional.empty<Boolean>()
    private var isStatic = Optional.empty<Boolean>()
    private var isConstant = Optional.empty<Boolean>()
    private var isInitialized = Optional.empty<Boolean>()
    private var name = Optional.empty<String>()
    private var flags = Optional.empty<Int>()
    private var functionInfo = Optional.empty<FunctionInfo>()


    /** factories */

    fun getArgumentDeclarationFactory(): Factory<ArgumentDeclaration> {
        return ArgumentDeclarationFactory(getArgumentType(), getVariableNumber())
    }

    fun getArithmeticOperatorFactory(): Factory<Operator> {
        return ArithmeticOperatorFactory(getComplexityLimit(), getOperatorLimit(),
                getOwnerClass(), getResultType(), getExceptionSafe(), getNoConsts())
    }

//    fun getArrayCreationFactory(): Factory<ArrayCreation> {
//        return ArrayCreationFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
//                getResultType(), getExceptionSafe(), getNoConsts())
//    }
//
//    fun getArrayElementFactory(): Factory<ArrayElement> {
//        return ArrayElementFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
//                getResultType(), getExceptionSafe(), getNoConsts())
//    }
//
//    fun getArrayExtractionFactory(): Factory<ArrayExtraction> {
//        return ArrayExtractionFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
//                getResultType(), getExceptionSafe(), getNoConsts())
//    }

    fun getAssignmentOperatorFactory(): Factory<Operator> {
        return AssignmentOperatorFactory(getComplexityLimit(), getOperatorLimit(),
                getOwnerClass(), resultType.orElse(null), getExceptionSafe(), getNoConsts())
    }

    fun getBinaryOperatorFactory(): Factory<BinaryOperator> {
        val o = getOperatorKind()
        when (o) {
            ASSIGN -> return AssignmentOperatorImplFactory(getComplexityLimit(), getOperatorLimit(),
                    getOwnerClass(), resultType.orElse(TypeNothing()), getExceptionSafe(), getNoConsts())
            AND, OR -> return BinaryLogicOperatorFactory(o, getComplexityLimit(), getOperatorLimit(),
                    getOwnerClass(), getExceptionSafe(), getNoConsts())
            BIT_OR, BIT_XOR, BIT_AND -> return BinaryBitwiseOperatorFactory(o, getComplexityLimit(), getOperatorLimit(),
                    getOwnerClass(), resultType.orElse(null), getExceptionSafe(), getNoConsts())

            EQ, NE -> return BinaryEqualityOperatorFactory(o, getComplexityLimit(),
                    getOperatorLimit(), getOwnerClass(), getExceptionSafe(),
                    getNoConsts())
            GT, LT, GE, LE -> return BinaryComparsionOperatorFactory(o, getComplexityLimit(),
                    getOperatorLimit(), getOwnerClass(), getExceptionSafe(),
                    getNoConsts())
            SHR, SHL, USHR -> return BinaryShiftOperatorFactory(o, getComplexityLimit(), getOperatorLimit(),
                    getOwnerClass(), resultType.orElse(null), getExceptionSafe(), getNoConsts())
            ADD, SUB, MUL, DIV, MOD -> return BinaryArithmeticOperatorFactory(o, getComplexityLimit(),
                    getOperatorLimit(), getOwnerClass(), resultType.orElse(null), getExceptionSafe(),
                    getNoConsts())
            STRADD -> return BinaryConcatOperatorFactory(getComplexityLimit(), getOperatorLimit(),
                    getOwnerClass(), resultType.orElse(null), getExceptionSafe(), getNoConsts())
            COMPOUND_ADD, COMPOUND_SUB, COMPOUND_MUL, COMPOUND_DIV, COMPOUND_MOD -> return CompoundArithmeticAssignmentOperatorFactory(o, getComplexityLimit(),
                    getOperatorLimit(), getOwnerClass(), resultType.orElse(TypeNothing()), getExceptionSafe(),
                    getNoConsts())
            else -> throw ProductionFailedException()
        }
    }

    fun getUnaryOperatorFactory(): Factory<UnaryOperator> {
        val o = getOperatorKind()
        return when (o) {
            NOT -> LogicalInversionOperatorFactory(getComplexityLimit(),
                    getOperatorLimit(), getOwnerClass(), getExceptionSafe(),
                    getNoConsts())
            UNARY_PLUS, UNARY_MINUS -> UnaryPlusMinusOperatorFactory(o, getComplexityLimit(),
                    getOperatorLimit(), getOwnerClass(), resultType.orElse(null), getExceptionSafe(),
                    getNoConsts())
            PRE_DEC, POST_DEC, PRE_INC, POST_INC -> IncDecOperatorFactory(o, getComplexityLimit(), getOperatorLimit(),
                    getOwnerClass(), resultType.orElse(TypeNothing()), getExceptionSafe(), getNoConsts())
            else -> throw ProductionFailedException()
        }
    }

    fun getBlockFactory(): Factory<Block> {
        return BlockFactory(getOwnerClass(), getResultType(), getComplexityLimit(),
                getStatementLimit(), getOperatorLimit(), getLevel(), subBlock.orElse(false),
                canHaveBreaks.orElse(false), canHaveContinues.orElse(false),
                canHaveReturn.orElse(false), canHaveReturn.orElse(false))
        //now 'throw' can be placed only in the same positions as 'return' TODO: mb should change
    }

    fun getBreakFactory(): Factory<Break> {
        return BreakFactory()
    }

    fun getCastOperatorFactory(): Factory<CastOperator> {
        return CastOperatorFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
                getResultType(), getExceptionSafe(), getNoConsts())
    }

//    fun getClassDefinitionBlockFactory(): Factory<ClassDefinitionBlock> {
//        return ClassDefinitionBlockFactory(getPrefix(),
//                ProductionParams.classesLimit.value(),
//                ProductionParams.memberFunctionsLimit.value(),
//                ProductionParams.functionsArgLimit.value(),
//                getComplexityLimit(),
//                ProductionParams.statementLimit.value(),
//                ProductionParams.operatorLimit.value(),
//                getLevel())
//    }
//
//    fun getMainKlassFactory(): Factory<MainKlass> {
//        return MainKlassFactory(getName(), getComplexityLimit(),
//                ProductionParams.memberFunctionsLimit.value(),
//                ProductionParams.functionsArgLimit.value(),
//                ProductionParams.statementLimit.value(),
//                ProductionParams.testStatementLimit.value(),
//                ProductionParams.operatorLimit.value())
//    }

    fun getMainFunctionFactory(): Factory<MainFunction> {
        return MainFunctionFactory(getName(), getComplexityLimit(),
                ProductionParams.statementLimit?.value() ?: throw NotInitializedOptionException("statementLimit"),
                ProductionParams.operatorLimit?.value() ?: throw NotInitializedOptionException("operatorLimit"))
    }

//    fun getConstructorDefinitionBlockFactory(): Factory<ConstructorDefinitionBlock> {
//        return ConstructorDefinitionBlockFactory(getOwnerClass(), getMemberFunctionsLimit(),
//                ProductionParams.functionsArgLimit.value(), getComplexityLimit(),
//                getStatementLimit(), getOperatorLimit(), getLevel())
//    }
//
//    fun getConstructorDefinitionFactory(): Factory<ConstructorDefinition> {
//        return ConstructorDefinitionFactory(getOwnerClass(), getComplexityLimit(),
//                getStatementLimit(), getOperatorLimit(),
//                getFunctionsArgLimit(), getLevel())
//    }

    fun getContinueFactory(): Factory<Continue> {
        return ContinueFactory()
    }

    fun getCounterInitializerFactory(counterValue: Int): Factory<CounterInitializer> {
        return CounterInitializerFactory(getOwnerClass(), counterValue)
    }

    fun getCounterManipulatorFactory(forward: Boolean): Factory<CounterManipulator> {
        return CounterManipulatorFactory(getLocalVariable(), forward)
    }

    fun getDeclarationFactory(): Factory<Declaration> {     //BlockFactory, VariableDeclarationBlockFactory
        return DeclarationFactory(getOwnerClass(), getComplexityLimit(), getOperatorLimit(),
                getIsLocal(), getExceptionSafe())
    }

    fun getDoWhileFactory(): Factory<DoWhile> {
        return DoWhileFactory(getOwnerClass(), getResultType(), getComplexityLimit(),
                getStatementLimit(), getOperatorLimit(), getLevel(), getCanHaveReturn())
    }

    fun getWhileFactory(): Factory<While> {
        return WhileFactory(getOwnerClass(), getResultType(), getComplexityLimit(),
                getStatementLimit(), getOperatorLimit(), getLevel(), getCanHaveReturn())
    }

    fun getIfFactory(): Factory<If> {
        return IfFactory(getOwnerClass(), getResultType(), getComplexityLimit(),
                getStatementLimit(), getOperatorLimit(), getLevel(), getCanHaveBreaks(),
                getCanHaveContinues(), getCanHaveReturn())
    }

//    fun getForFactory(): Factory<For> {
//        return ForFactory(getOwnerClass(), getResultType(), getComplexityLimit(),
//                getStatementLimit(), getOperatorLimit(), getLevel(), getCanHaveReturn())
//    }

    fun getWhenFactory(): Factory<When> {
        return WhenFactory(getComplexityLimit(), getStatementLimit(), getOperatorLimit(),
                getOwnerClass(), getResultType(), getLevel(), getCanHaveReturn())
    }

    fun getExpressionFactory(): Factory<IRNode> {       //rule
        return ExpressionFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
                getResultType(), getExceptionSafe(), getNoConsts())
    }

    fun getFunctionDeclarationBlockFactory(): Factory<FunctionDeclarationBlock> {
        return FunctionDeclarationBlockFactory(getOwnerClass() ?: throw IllegalArgumentException("Function declaration must have an owner"),
                getMemberFunctionsLimit(),
                getMemberFunctionsArgLimit(), getLevel())
    }

    fun getFunctionDeclarationFactory(): Factory<FunctionDeclaration> {     //for FunctionDeclarationBlockFactory
        return FunctionDeclarationFactory(getName(), getOwnerClass() ?: throw IllegalArgumentException("Function declaration must have an owner"),
                resultType.orElse(TypeList.UNIT),
                getMemberFunctionsArgLimit(), getFlags())
    }

    fun getFunctionDefinitionBlockFactory(): Factory<FunctionDefinitionBlock> {
        return FunctionDefinitionBlockFactory(getOwnerClass(), getMemberFunctionsLimit(),
                getMemberFunctionsArgLimit(), getComplexityLimit(), getStatementLimit(),
                getOperatorLimit(), getLevel(), getFlags())
    }

    fun getFunctionDefinitionFactory(): Factory<FunctionDefinition> {
        return FunctionDefinitionFactory(getName(), getOwnerClass(), resultType.orElse(null),
                getComplexityLimit(), getStatementLimit(), getOperatorLimit(),
                getMemberFunctionsArgLimit(), getLevel(), getFlags())
    }

    fun getFunctionCallFactory(): Factory<FunctionCall> {
        return FunctionCallFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
                resultType.orElse(null), getExceptionSafe())
    }

//    fun getFunctionRedefinitionBlockFactory(functionSet: Collection<Symbol>): Factory<FunctionRedefinitionBlock> {
//        return FunctionRedefinitionBlockFactory(functionSet, getOwnerClass(),
//                getComplexityLimit(), getStatementLimit(), getOperatorLimit(), getLevel())
//    }
//
//    fun getFunctionRedefinitionFactory(): Factory<FunctionRedefinition> {
//        return FunctionRedefinitionFactory(getFunctionInfo(), getOwnerClass(),
//                getComplexityLimit(), getStatementLimit(), getOperatorLimit(), getLevel(),
//                getFlags())
//    }

//    fun getInterfaceFactory(): Factory<Interface> {
//        return InterfaceFactory(getName(), getMemberFunctionsLimit(),
//                getFunctionsArgLimit(), getLevel())
//    }

//    fun getKlassFactory(): Factory<Klass> {
//        return KlassFactory(getName(), getComplexityLimit(),
//                getMemberFunctionsLimit(), getFunctionsArgLimit(), getStatementLimit(),
//                getOperatorLimit(), getLevel())
//    }

    fun getLimitedExpressionFactory(): Factory<IRNode> {
        return LimitedExpressionFactory(getComplexityLimit(), getOperatorLimit(),
                getOwnerClass(), getResultType(), getExceptionSafe(), getNoConsts())
    }

    fun getLiteralFactory(): Factory<Literal> {
        return LiteralFactory(getResultType())
    }

    fun getLocalVariableFactory(): Factory<LocalVariable> {
        return LocalVariableFactory(getResultType(), getFlags())
    }

    fun getLogicOperatorFactory(): Factory<Operator> {
        return LogicOperatorFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(), getExceptionSafe(), getNoConsts())
    }

    fun getLoopingConditionFactory(limiter: Literal, forward: Boolean): Factory<LoopingCondition> {
        return LoopingConditionFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
                getLocalVariable(), limiter, forward)
    }

//    fun getNonStaticMemberVariableFactory(): Factory<NonStaticMemberVariable> {
//        return NonStaticMemberVariableFactory(getComplexityLimit(), getOperatorLimit(),
//                getOwnerClass(), /*getVariableType()*/getResultType(), getFlags(), getExceptionSafe())
//    }

    fun getNothingFactory(): Factory<NothingNode> {
        return NothingFactory()
    }

    fun getPrintVariablesFactory(): Factory<PrintVariables> {
        return PrintVariablesFactory(getOwnerClass(), getLevel())
    }

    fun getRangeOperatorFactory(): Factory<RangeOperator> {
        return RangeOperatorFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(), getExceptionSafe(), getNoConsts())
    }

    fun getReturnFactory(): Factory<Return> {
        return ReturnFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
                getResultType(), getExceptionSafe())
    }

//    fun getThrowFactory(): Factory<Throw> {
//        return ThrowFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(), getResultType(), getExceptionSafe())
//    }

    fun getStatementFactory(): Factory<Statement> {
        return StatementFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
                getExceptionSafe(), getNoConsts())
    }

//    fun getStaticConstructorDefinitionFactory(): Factory<StaticConstructorDefinition> {         //TODO: delete?
//        return StaticConstructorDefinitionFactory(getOwnerClass(), getComplexityLimit(),
//                getStatementLimit(), getOperatorLimit(), getLevel())
//    }
//
//    fun getStaticMemberVariableFactory(): Factory<StaticMemberVariable> {
//        return StaticMemberVariableFactory(getOwnerClass(), /*getVariableType()*/getResultType(), getFlags())     //TODO: delete?
//    }

    fun getVariableDeclarationBlockFactory(): Factory<VariableDeclarationBlock> {
        return VariableDeclarationBlockFactory(getOwnerClass(), getComplexityLimit(),
                getOperatorLimit(), getLevel(), getExceptionSafe())
    }

    fun getVariableDeclarationFactory(): Factory<VariableDeclaration> {
        return VariableDeclarationFactory(getOwnerClass(), getIsStatic(), getIsLocal(), getResultType())
    }

    fun getVariableFactory(): Factory<VariableBase> {
        return VariableFactory(getComplexityLimit(), getOperatorLimit(), getOwnerClass(),
                getResultType(), getIsConstant(), getIsInitialized(), getExceptionSafe(), getNoConsts())
    }

    fun getVariableInitializationFactory(): Factory<VariableInitialization> {
        return VariableInitializationFactory(getOwnerClass(), getIsConstant(), getIsStatic(),
                getIsLocal(), getComplexityLimit(), getOperatorLimit(), getExceptionSafe())
    }

//    fun getTryCatchBlockFactory(): Factory<TryCatchBlock> {
//        return TryCatchBlockFactory(getOwnerClass(), getResultType(),
//                getComplexityLimit(), getStatementLimit(), getOperatorLimit(),
//                getLevel(), subBlock.orElse(false), getCanHaveBreaks(),
//                getCanHaveContinues(), getCanHaveReturn())
//    }

    /** setters */

    fun setArgumentType(value: Type?): IRNodeBuilder {
        if (value != null) argumentType = Optional.of(value)
        else argumentType = Optional.empty()
        return this
    }

    fun setVariableNumber(value: Int): IRNodeBuilder {
        variableNumber = Optional.of(value)
        return this
    }

    fun setComplexityLimit(value: Long): IRNodeBuilder {
        complexityLimit = Optional.of(value)
        return this
    }

    fun setOperatorLimit(value: Int): IRNodeBuilder {
        operatorLimit = Optional.of(value)
        return this
    }

    fun setStatementLimit(value: Int): IRNodeBuilder {
        statementLimit = Optional.of(value)
        return this
    }

    fun setOwnerClass(value: Type?): IRNodeBuilder {
        if (value != null) ownerClass = Optional.of(value)
        else ownerClass = Optional.empty()
        return this
    }

    fun setResultType(value: Type): IRNodeBuilder {
        resultType = Optional.of(value)
        return this
    }

    // TODO: check if safe is always true in current implementation
    fun setExceptionSafe(value: Boolean): IRNodeBuilder {
        safe = Optional.of(value)
        return this
    }

    // TODO: check is noConsts is always false in current implementation
    fun setNoConsts(value: Boolean): IRNodeBuilder {
        noConsts = Optional.of(value)
        return this
    }

    fun setOperatorKind(value: OperatorKind): IRNodeBuilder {
        opKind = Optional.of(value)
        return this
    }

    fun setLevel(value: Long): IRNodeBuilder {
        level = Optional.of(value)
        return this
    }

    fun setSubBlock(value: Boolean): IRNodeBuilder {
        subBlock = Optional.of(value)
        return this
    }

    fun setCanHaveBreaks(value: Boolean): IRNodeBuilder {
        canHaveBreaks = Optional.of(value)
        return this
    }

    fun setCanHaveContinues(value: Boolean): IRNodeBuilder {
        canHaveContinues = Optional.of(value)
        return this
    }

    fun setCanHaveReturn(value: Boolean): IRNodeBuilder {
        canHaveReturn = Optional.of(value)
        return this
    }

    fun setCanHaveThrow(value: Boolean): IRNodeBuilder {
        canHaveThrow = Optional.of(value)
        return this
    }

    fun setPrefix(value: String): IRNodeBuilder {
        prefix = Optional.of(value)
        return this
    }

    fun setMemberFunctionsLimit(value: Int): IRNodeBuilder {
        memberFunctionsLimit = Optional.of(value)
        return this
    }

    fun setMemberFunctionsArgLimit(value: Int): IRNodeBuilder {
        memberFunctionsArgLimit = Optional.of(value)
        return this
    }

    fun setLocalVariable(value: LocalVariable): IRNodeBuilder {
        localVariable = Optional.of(value)
        return this
    }

    fun setIsLocal(value: Boolean): IRNodeBuilder {
        isLocal = Optional.of(value)
        return this
    }

    fun setIsStatic(value: Boolean): IRNodeBuilder {
        isStatic = Optional.of(value)
        return this
    }

    fun setIsInitialized(value: Boolean): IRNodeBuilder {
        isInitialized = Optional.of(value)
        return this
    }

    fun setIsConstant(value: Boolean): IRNodeBuilder {
        isConstant = Optional.of(value)
        return this
    }

    fun setName(value: String): IRNodeBuilder {
        name = Optional.of(value)
        return this
    }

    fun setFlags(value: Int): IRNodeBuilder {
        flags = Optional.of(value)
        return this
    }

    fun setFunctionInfo(value: FunctionInfo): IRNodeBuilder {
        functionInfo = Optional.of(value)
        return this
    }

    /** getters */

    private fun getArgumentType(): Type? {
        return argumentType.orElse(null)
    }

    private fun getVariableNumber(): Int {
        return variableNumber.orElseThrow {
            IllegalArgumentException(
                    "Variable number wasn't set")
        }
    }

    private fun getComplexityLimit(): Long {
        return complexityLimit.orElseThrow {
            IllegalArgumentException(
                    "Complexity limit wasn't set")
        }
    }

    private fun getOperatorLimit(): Int {
        return operatorLimit.orElseThrow {
            IllegalArgumentException(
                    "Operator limit wasn't set")
        }
    }

    private fun getStatementLimit(): Int {
        return statementLimit.orElseThrow {
            IllegalArgumentException(
                    "Statement limit wasn't set")
        }
    }

    private fun getOwnerClass(): Type? {
        return ownerClass.orElse(null)
    }

    private fun getResultType(): Type {
        return resultType.orElseThrow { IllegalArgumentException("Return type wasn't set") }
    }

    private fun getExceptionSafe(): Boolean {
        return safe.orElseThrow { IllegalArgumentException("Safe wasn't set") }
    }

    private fun getNoConsts(): Boolean {
        return noConsts.orElseThrow { IllegalArgumentException("NoConsts wasn't set") }
    }

    private fun getOperatorKind(): OperatorKind {
        return opKind.orElseThrow { IllegalArgumentException("Operator kind wasn't set") }
    }

    private fun getLevel(): Long {
        return level.orElseThrow { IllegalArgumentException("Level wasn't set") }
    }

    private fun getPrefix(): String {
        return prefix.orElseThrow { IllegalArgumentException("Prefix wasn't set") }
    }

    private fun getMemberFunctionsLimit(): Int {
        return memberFunctionsLimit.orElseThrow {
            IllegalArgumentException("memberFunctions limit wasn't set")
        }
    }

    private fun getMemberFunctionsArgLimit(): Int {
        return memberFunctionsArgLimit.orElseThrow {
            IllegalArgumentException("memberFunctionsArg limit wasn't set")
        }
    }

    private fun getLocalVariable(): LocalVariable {
        return localVariable.orElseThrow {
            IllegalArgumentException("local variable wasn't set")
        }
    }

    private fun getIsLocal(): Boolean {
        return isLocal.orElseThrow { IllegalArgumentException("isLocal wasn't set") }
    }

    private fun getIsStatic(): Boolean {
        return isStatic.orElseThrow { IllegalArgumentException("isStatic wasn't set") }
    }

    private fun getIsInitialized(): Boolean {
        return isInitialized.orElseThrow {
            IllegalArgumentException("isInitialized wasn't set")
        }
    }

    private fun getIsConstant(): Boolean {
        return isConstant.orElseThrow { IllegalArgumentException("isConstant wasn't set") }
    }

    private fun getCanHaveReturn(): Boolean {
        return canHaveReturn.orElseThrow {
            IllegalArgumentException("canHaveReturn wasn't set")
        }
    }

    private fun getCanHaveBreaks(): Boolean {
        return canHaveBreaks.orElseThrow {
            IllegalArgumentException("canHaveBreaks wasn't set")
        }
    }

    private fun getCanHaveContinues(): Boolean {
        return canHaveContinues.orElseThrow {
            IllegalArgumentException("canHaveContinues wasn't set")
        }
    }

    private fun getName(): String {
        return name.orElseThrow { IllegalArgumentException("Name wasn't set") }
    }

    private fun getFlags(): Int {
        return flags.orElseThrow { IllegalArgumentException("Flags wasn't set") }
    }

    private fun getFunctionInfo(): FunctionInfo {
        return functionInfo.orElseThrow {
            IllegalArgumentException("FunctionInfo wasn't set")
        }
    }
}