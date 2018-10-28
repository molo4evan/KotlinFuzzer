package factories.controlflow

import factories.Factory
import ir.controlflow.Break

class BreakFactory: Factory<Break>() {
    override fun produce() = Break()
}