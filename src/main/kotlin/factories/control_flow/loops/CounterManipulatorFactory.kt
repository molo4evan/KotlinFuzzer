package factories.control_flow.loops

import factories.Factory
import ir.Statement
import ir.control_flow.loops.CounterManipulator
import ir.operators.OperatorKind
import ir.operators.UnaryOperator
import ir.variables.LocalVariable

class CounterManipulatorFactory(private val counter: LocalVariable): Factory<CounterManipulator>() {
    override fun produce(): CounterManipulator {
        // We'll keep it simple for the time being..
        val manipulator = UnaryOperator(OperatorKind.POST_INC, counter)
        return CounterManipulator(Statement(manipulator))
    }
}