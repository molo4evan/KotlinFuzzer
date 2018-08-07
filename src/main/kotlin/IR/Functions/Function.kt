package IR.Functions

import IR.Types.Type
import IR.IRNode
import Information.FunctionInfo
import Information.SymbolTable
import Visitors.Visitor

class Function(owner: Type, val functionInfo: FunctionInfo, args: List<IRNode>): IRNode(functionInfo.type) {
    init {
        this.owner = owner
        addChildren(args)
    }

    override fun complexity(): Long {
        var argsComplexity = 0L
        for (child in children){
            argsComplexity += child.complexity()
        }
        var funComplexity = functionInfo.complexity

        /*if (functionInfo.isConstructor()) {
            // Sum complexities of all default constructors of parent classes
            for (parent in owner!!.getAllParents()) {
                val parentFuncs = SymbolTable.getAllCombined(parent, FunctionInfo::class.java)
                for (f in parentFuncs) {
                    val c = f as FunctionInfo
                    if (c.name.equals(c.owner.getName()) && c.argTypes.isEmpty()) {
                        funComplexity += c.complexity
                    }
                }
            }
            // TODO: Complexities of all non-static initializers should be also added..
        } else {*/
            if (owner == null) return argsComplexity + funComplexity
            for (child in owner!!.getAllChildren()) {
                val childFuncs = SymbolTable.getAllCombined(child, FunctionInfo::class)
                for (childFunc in childFuncs) {
                    if (childFunc.equals(functionInfo)) {
                        funComplexity = Math.max(funComplexity, (childFunc as FunctionInfo).complexity)
                    }
                }
            }
        //}
        return argsComplexity + funComplexity
    }

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}