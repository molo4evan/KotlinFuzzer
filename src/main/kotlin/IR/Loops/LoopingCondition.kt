package IR.Loops

import IR.IRNode

class LoopingCondition(val condition: IRNode): IRNode(condition.getResultType()) {
    init {
        addChild(condition)
    }

    override fun complexity() = condition.complexity()
}