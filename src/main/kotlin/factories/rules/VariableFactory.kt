package factories.rules

import factories.Factory
import factories.utils.IRNodeBuilder
import information.Symbol
import information.VariableInfo
import ir.types.Type
import ir.variables.VariableBase

internal class VariableFactory(
        complexityLimit: Long,
        operatorLimit: Int,
        ownerClass: Type?,
        resultType: Type,
        constant: Boolean,
        initialized: Boolean,
        exceptionSafe: Boolean,
        noconsts: Boolean
) : Factory<VariableBase>() {
    private val rule: Rule<VariableBase>

    init {
        var flags = Symbol.NONE
        if (constant) {
            flags = flags or VariableInfo.CONST
        }
        if (initialized) {
            flags = flags or VariableInfo.INITIALIZED
        }
        rule = Rule("variable")
        val b = IRNodeBuilder.setResultType(resultType)
                .setFlags(flags)
                .setComplexityLimit(complexityLimit)
                .setOperatorLimit(operatorLimit)
                .setOwnerClass(ownerClass)
                .setExceptionSafe(exceptionSafe)
        //rule.add("non_static_member_variable", b.getNonStaticMemberVariableFactory())     //TODO: uncomment
        //rule.add("static_member_variable", b.getStaticMemberVariableFactory())
        rule.add("local_variable", b.getLocalVariableFactory())
    }

    override fun produce(): VariableBase {
        return rule.produce()
    }
}