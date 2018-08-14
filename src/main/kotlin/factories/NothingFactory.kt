package factories

import ir.NothingNode

class NothingFactory: Factory<NothingNode>() {
    override fun produce() = NothingNode()
}