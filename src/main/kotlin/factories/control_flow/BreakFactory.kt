package factories.control_flow

import factories.Factory
import ir.control_flow.Break

class BreakFactory: Factory<Break>() {
    override fun produce() = Break()
}