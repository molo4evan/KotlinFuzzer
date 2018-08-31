package factories.control_flow

import factories.Factory
import ir.control_flow.Continue

class ContinueFactory: Factory<Continue>() {
    override fun produce() = Continue()
}