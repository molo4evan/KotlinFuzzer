package factories.control_flow.loops

import exceptions.NotInitializedOptionException
import factories.Factory
import information.TypeList
import ir.Literal
import ir.Statement
import ir.control_flow.loops.CounterManipulator
import ir.operators.BinaryOperator
import ir.operators.OperatorKind
import ir.variables.LocalVariable
import utils.ProductionParams
import utils.PseudoRandom

class CounterManipulatorFactory(private val counter: LocalVariable): Factory<CounterManipulator>() {
    override fun produce(): CounterManipulator {
        val step = PseudoRandom.randomNotZero(ProductionParams.stepLimit?.value() ?: throw NotInitializedOptionException("stepLimit"))
        val lit = Literal(step, TypeList.INT)
        val manipulator = BinaryOperator(OperatorKind.COMPOUND_ADD, counter.getResultType(), counter, lit)
        return CounterManipulator(Statement(manipulator))
    }
}