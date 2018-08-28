package utils
import information.FunctionInfo
import information.Symbol
import information.TypeList
import information.VariableInfo
import ir.*
import ir.functions.FunctionCall
import ir.operators.BinaryOperator
import ir.operators.OperatorKind
import ir.types.Type
import ir.variables.LocalVariable
import ir.variables.NonStaticMemberVariable
import ir.variables.StaticMemberVariable
import ir.variables.VariableInitialization

object PrintingUtils {
    val EOL = Literal("\n", TypeList.STRING)

    fun align(l: Long): String {    //adding tabulation
        val shift = StringBuilder()
        for (i in 0 until l) {
            shift.append("    ")
        }
        return shift.toString()
    }

    fun printVariablesAsBlock(node: PrintVariables): Block {
        val owner = node.owner
        val nodes = mutableListOf<IRNode>()

        val resultInfo = VariableInfo("result", owner, TypeList.STRING, VariableInfo.LOCAL)
        nodes.add(Statement(VariableInitialization(resultInfo, Literal("[", TypeList.STRING))))
        val resultVar = LocalVariable(resultInfo)

        val vars = node.vars

        val printerClass = Type("utils.Printer")

        val thisInfo: VariableInfo
        var thisVar: LocalVariable? = null
        if (owner != null) {
            thisInfo = VariableInfo("this", owner, owner, VariableInfo.LOCAL or VariableInfo.INITIALIZED)
            thisVar = LocalVariable(thisInfo)
        }

        for (i in 0 until vars.size) {
            val v = vars[i] as VariableInfo
            if (!v.isiInitialized()) continue
            nodes.add(Statement(BinaryOperator(
                    OperatorKind.COMPOUND_ADD,
                    TypeList.STRING,
                    resultVar,
                    Literal((if (v.owner != null) "${v.owner.getName()}." else "") + v.name + " = ", TypeList.STRING)
            )))
            val argInfo = VariableInfo("arg", printerClass, if (!v.type.isBuiltIn()) TypeList.ANY else v.type, VariableInfo.LOCAL or VariableInfo.INITIALIZED)
            val printInfo = FunctionInfo("print", printerClass, TypeList.STRING, 0, Symbol.PUBLIC or Symbol.STATIC, argInfo)

            val varInfo = VariableInfo(v)
            val arg = if(v.owner != null) {
                if (v.isStatic()) {
                    StaticMemberVariable(v.owner, varInfo)
                } else {
                    NonStaticMemberVariable(thisVar!!, varInfo)
                }
            } else {
                LocalVariable(varInfo)
            }

            val call = FunctionCall(printerClass, printInfo, listOf(arg))
            nodes.add(Statement(BinaryOperator(OperatorKind.COMPOUND_ADD, TypeList.STRING, resultVar, call)))
            if (i < vars.size - 1) {
                nodes.add(Statement(BinaryOperator(OperatorKind.COMPOUND_ADD, TypeList.STRING, resultVar, EOL)))
            }
        }
        nodes.add(Statement(BinaryOperator(OperatorKind.COMPOUND_ADD, TypeList.STRING, resultVar, Literal("]\n", TypeList.STRING))))

        val printInfo = FunctionInfo("print", null, TypeList.STRING, 0, Symbol.PUBLIC or Symbol.STATIC)
        val print = FunctionCall(owner, printInfo, listOf(resultVar))

        nodes.add(print)

        return Block(owner, TypeList.STRING, nodes, 1)
    }
}