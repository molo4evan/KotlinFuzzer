package factories.operators

import factories.Factory
import ir.operators.Operator

abstract class OperatorFactory<T : Operator> protected constructor(
        protected val operatorPriority: Int,
        protected val complexityLimit: Long,
        protected var operatorLimit: Int,
        protected val exceptionSafe: Boolean,
        protected val noconsts: Boolean
) : Factory<T>()