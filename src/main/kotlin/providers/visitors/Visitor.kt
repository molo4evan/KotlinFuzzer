package providers.visitors

import ir.*
import ir.control_flow.Break
import ir.control_flow.Continue
import ir.control_flow.If
import ir.control_flow.When
import ir.control_flow.loops.*
import ir.functions.*
import ir.operators.BinaryOperator
import ir.operators.CastOperator
import ir.operators.RangeOperator
import ir.operators.UnaryOperator
import ir.types.Type
import ir.variables.LocalVariable
import ir.variables.VariableDeclaration
import ir.variables.VariableDeclarationBlock


interface Visitor<T> {
    fun visit(node: ArgumentDeclaration): T
//    fun visit(node: ArrayCreation): T
//    fun visit(node: ArrayElement): T
//    fun visit(node: ArrayExtraction): T
    fun visit(node: BinaryOperator): T
    fun visit(node: Block): T
    fun visit(node: Break): T
    fun visit(node: CastOperator): T
////    fun visit(node: ClassDefinitionBlock): T
////    fun visit(node: ConstructorDefinition): T
////    fun visit(node: ConstructorDefinitionBlock): T
    fun visit(node: Continue): T
    fun visit(node: CounterInitializer): T
    fun visit(node: CounterManipulator): T
    fun visit(node: Declaration): T
    fun visit(node: DoWhile): T
    fun visit(node: For): T
    fun visit(node: FunctionCall): T
    fun visit(node: FunctionDeclaration): T
    fun visit(node: FunctionDeclarationBlock): T
    fun visit(node: FunctionDefinition): T
    fun visit(node: FunctionDefinitionBlock): T
    fun visit(node: MainFunction): T
////    fun visit(node: FunctionRedefinition): T
////    fun visit(node: FunctionRedefinitionBlock): T
    fun visit(node: If): T
    fun visit(node: Initialization): T
////    fun visit(node: Interface): T
////    fun visit(node: ClassNode): T
    fun visit(node: Literal): T
    fun visit(node: LocalVariable): T
    fun visit(node: LoopingCondition): T
////    fun visit(node: MainKlass): T   //TODO: delete?
////    fun visit(node: NonStaticMemberVariable): T
    fun visit(node: NothingNode): T
    fun visit(node: PrintVariables): T
    fun visit(node: RangeOperator): T
    fun visit(node: Return): T
//    fun visit(node: Throw): T
    fun visit(node: Statement): T
////    fun visit(node: StaticConstructorDefinition): T
////    fun visit(node: StaticMemberVariable): T
    fun visit(node: Type): T
//    fun visit(node: TypeArray): T
    fun visit(node: UnaryOperator): T
    fun visit(node: VariableDeclaration): T
    fun visit(node: VariableDeclarationBlock): T
    fun visit(node: When): T
    fun visit(node: While): T
//    fun visit(node: CatchBlock): T
//    fun visit(node: TryCatchBlock): T
    //TODO: add companion object block (for static functions and variables)
}