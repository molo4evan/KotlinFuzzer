package ir.variables

import ir.IRNode
import ir.Initialization
import information.VariableInfo

class VariableInitialization(info: VariableInfo, initExpr: IRNode): Initialization(info, initExpr)