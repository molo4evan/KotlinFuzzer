package IR.Loops

import IR.IRNode
import IR.Initialization
import Information.VariableInfo

class CounterInitializer(varInfo: VariableInfo, initExpr: IRNode): Initialization(varInfo, initExpr)