package IR.Variables

import IR.IRNode
import Information.VariableInfo

abstract class VariableBase(val variableInfo: VariableInfo): IRNode(variableInfo.type)