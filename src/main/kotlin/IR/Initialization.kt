package IR

import Information.VariableInfo

abstract class Initialization protected constructor(val variableInfo: VariableInfo, initExpr: IRNode): IRNode(variableInfo.type) {
    init {
        addChild(initExpr)
    }

    override fun complexity() = getChild(0).complexity() + 1
}