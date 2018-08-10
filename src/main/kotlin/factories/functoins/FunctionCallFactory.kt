package factories.functoins

import exceptions.ProductionFailedException
import factories.SafeFactory
import factories.utils.IRNodeBuilder
import information.FunctionInfo
import information.SymbolTable
import ir.IRNode
import ir.types.Type
import ir.functions.FunctionCall
import utils.PseudoRandom

class FunctionCallFactory(
        private val complexityLimit: Long,
        private val operatorLimit: Int,
        private val owner: Type?,
        private val resultType: Type,
        private val exceptionSafe: Boolean
): SafeFactory<FunctionCall>() {
    private val functionInfo = FunctionInfo("", null, resultType, 0, 0)

    override fun sproduce(): FunctionCall {
        // Currently no function is exception-safe
        if (exceptionSafe) {
            throw ProductionFailedException()
        }

        val allFunctions = SymbolTable.get(functionInfo.type, FunctionInfo::class)

        if (!allFunctions.isEmpty()){
            PseudoRandom.shuffle(allFunctions)
            val classHierarchy = owner?.getAllParents()

            for (function in allFunctions){
                val functionInfo = function as FunctionInfo

                // Don't try to construct abstract classes.
                if (functionInfo.owner != null && functionInfo.owner.isAbstract() && functionInfo.isConstructor()) {
                    continue
                }

                // We don't call methods from the same class which are not final, because if we
                // do this may produce an infinite recursion. Simple example:
                // class  A
                // {
                //     f1() { }
                //     f2() { f1(); }
                // }
                //
                // class B : A
                // {
                //    f1() { f2(); }
                // }
                //
                // However the same example is obviously safe for static and final functions
                // Also we introduce a special flag NONRECURSIVE to mark functions that                 //TODO: i don't think it's needed in Kotlin
                // are not overrided. We may also call such functions.

                // If it's a local call.. or it's a call using some variable to some object of some type in our hierarchy   //whaaaaat???
                var inHierarchy = false
                if (owner != null) {
                    if (owner == functionInfo.owner || classHierarchy != null && classHierarchy.contains(functionInfo.owner)) {
                        inHierarchy = classHierarchy!!.contains(functionInfo.owner)
                        if (!functionInfo.isFinal() && !functionInfo.isStatic() && !functionInfo.isNonRecursive()) {  //TODO: what happens here?
                            continue
                        }
                        if (inHierarchy && functionInfo.isPrivate()) {
                            continue
                        }
                    } else {
                        if (!functionInfo.isPublic() && !functionInfo.isInternal()) {
                            continue
                        }
                    }
                }
                if (functionInfo.complexity < complexityLimit - 1) {
                    try {
                        val accum = mutableListOf<IRNode>()
                        if (!functionInfo.argTypes.isEmpty()) {
                            // Here we should do some analysis here to determine if
                            // there are any conflicting functions due to possible
                            // constant folding.

                            // For example the following can be done:
                            // Scan all the hieirachy where the class is declared.
                            // If there are function with a same name and same number of args,
                            // then disable usage of foldable expressions in the args.
                            var noconsts = false
                            if (owner != null){
                                val allFunInClass = SymbolTable.getAllCombined(functionInfo.owner!!, FunctionInfo::class)
                                for (symbol in allFunInClass){
                                    val funct = symbol as FunctionInfo
                                    if (funct != functionInfo && funct.name == functionInfo.name && funct.argTypes.size == functionInfo.argTypes.size){
                                        noconsts = true
                                        break
                                    }
                                }
                            }
                            val argCompLimit = (complexityLimit - 1 - functionInfo.complexity) / functionInfo.argTypes.size
                            val argOpLimit = (operatorLimit - 1) / functionInfo.argTypes.size
                            val builder = IRNodeBuilder().
                                    setOwnerClass(owner).
                                    setComplexityLimit(argCompLimit).
                                    setOperatorLimit(argOpLimit).
                                    setExceptionSafe(exceptionSafe).
                                    setNoConsts(noconsts)
                            for (argType in functionInfo.argTypes){
                                accum.add(builder.setResultType(argType.type).getExpressionFactory().produce())
                            }
                        }
                        return FunctionCall(owner, functionInfo, accum)
                    } catch (ex: ProductionFailedException){}
                }
            }
        }
        throw ProductionFailedException()
    }
}