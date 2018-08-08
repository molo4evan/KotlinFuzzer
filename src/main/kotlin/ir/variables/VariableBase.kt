package ir.variables

import ir.IRNode
import information.VariableInfo

abstract class VariableBase(val variableInfo: VariableInfo): IRNode(variableInfo.type)