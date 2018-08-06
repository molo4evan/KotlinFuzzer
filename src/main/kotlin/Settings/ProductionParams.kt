package Settings

import Settings.OptionResolver.Option

object ProductionParams {
    var productionLimit: Option<Int>? = null
    var dataMemberLimit: Option<Int>? = null
    var statementLimit: Option<Int>? = null
    var testStatementLimit: Option<Int>? = null
    var operatorLimit: Option<Int>? = null
    var complexityLimit: Option<Long>? = null
    var memberFunctionsLimit: Option<Int>? = null
    var memberFunctionsArgLimit: Option<Int>? = null
    var stringLiteralSizeLimit: Option<Int>? = null
    var classesLimit: Option<Int>? = null
    var implementationLimit: Option<Int>? = null
    var dimensionsLimit: Option<Int>? = null
    var floatingPointPrecision: Option<Int>? = null
    var minCfgDepth: Option<Int>? = null
    var maxCfgDepth: Option<Int>? = null
    var enableStrictFP: Option<Boolean>? = null
    var printComplexity: Option<Boolean>? = null
    var printHierarchy: Option<Boolean>? = null
    //public static BooleanOption disableFinals = optionResolver.addBooleanOption("disable-finals", "Don\'t use finals");
    var disableFinalClasses: Option<Boolean>? = null
    var disableFinalMethods: Option<Boolean>? = null
    var disableFinalVariables: Option<Boolean>? = null
    var disableIf: Option<Boolean>? = null
    var disableSwitch: Option<Boolean>? = null
    var disableWhile: Option<Boolean>? = null
    var disableDoWhile: Option<Boolean>? = null
    var disableFor: Option<Boolean>? = null
    var disableFunctions: Option<Boolean>? = null
    var disableVarsInBlock: Option<Boolean>? = null
    var disableExprInInit: Option<Boolean>? = null
    var disableExternalSymbols: Option<Boolean>? = null
    var addExternalSymbols: Option<String>? = null
    var disableInheritance: Option<Boolean>? = null
    var disableDowncasts: Option<Boolean>? = null
    var disableStatic: Option<Boolean>? = null
    var disableInterfaces: Option<Boolean>? = null
    var disableClasses: Option<Boolean>? = null
    var disableNestedBlocks: Option<Boolean>? = null
    var disableArrays: Option<Boolean>? = null
    var enableFinalizers: Option<Boolean>? = null
    // workaraound: to reduce chance throwing ArrayIndexOutOfBoundsException
    var chanceExpressionIndex: Option<Int>? = null
    var testbaseDir: Option<String>? = null
    var numberOfTests: Option<Int>? = null
    var seed: Option<String>? = null
    var specificSeed: Option<Long>? = null
    var classesFile: Option<String>? = null
    var excludeMethodsFile: Option<String>? = null
    var generators: Option<String>? = null
    var generatorsFactories: Option<String>? = null

}