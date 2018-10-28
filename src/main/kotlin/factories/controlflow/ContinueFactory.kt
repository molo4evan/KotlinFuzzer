package factories.controlflow

import factories.Factory
import ir.controlflow.Continue

class ContinueFactory: Factory<Continue>() {
    override fun produce() = Continue()
}