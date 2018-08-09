package utils

import utils.OptionResolver.Option

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
    //public static BooleanOption disableFinals = OptionResolver.addBooleanOption("disable-finals", "Don\'t use finals");
    var disableFinalClasses: Option<Boolean>? = null
    var disableFinalMethods: Option<Boolean>? = null
    var disableFinalVariables: Option<Boolean>? = null
    var disableIf: Option<Boolean>? = null
    var disableWhen: Option<Boolean>? = null
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
    var functionCallsPercent: Option<Double>? = null

    //TODO: ADD INITIALIZATION!!!
    fun register() {
        productionLimit = OptionResolver.addIntOption('l', "production-limit", 100, "Limit on steps in the production of an expression")
        dataMemberLimit = OptionResolver.addIntOption('v', "data-member-limit", 10, "Upper limit on data members")
        statementLimit = OptionResolver.addIntOption('s', "statement-limit", 30, "Upper limit on statements in function")
        testStatementLimit = OptionResolver.addIntOption('e', "test-statement-limit", 300, "Upper limit on statements in test() function")
        operatorLimit = OptionResolver.addIntOption('o', "operator-limit", 50, "Upper limit on operators in a statement")
        complexityLimit = OptionResolver.addLongOption('x', "complexity-limit", 10000000, "Upper limit on complexity")
        memberFunctionsLimit = OptionResolver.addIntOption('m', "member-functions-limit", 15, "Upper limit on member functions")
        memberFunctionsArgLimit = OptionResolver.addIntOption('a', "member-functions-arg-limit", 5, "Upper limit on the number of member function args")
        stringLiteralSizeLimit = OptionResolver.addIntOption("string-literal-size-limit", 10, "Upper limit on the number of chars in string literal")
        classesLimit = OptionResolver.addIntOption('c', "classes-limit", 12, "Upper limit on the number of classes")
        implementationLimit = OptionResolver.addIntOption('i', "implementation-limit", 3, "Upper limit on a number of interfaces a class can implement")
        dimensionsLimit = OptionResolver.addIntOption('d', "dimensions-limit", 3, "Upper limit on array dimensions")
        floatingPointPrecision = OptionResolver.addIntOption("fp-precision", 8, "A non-negative decimal integer used to restrict the number of digits after the decimal separator")
        minCfgDepth = OptionResolver.addIntOption("min-cfg-depth", 2, "A non-negative decimal integer used to restrict the lower bound of depth of control flow graph")
        maxCfgDepth = OptionResolver.addIntOption("max-cfg-depth", 3, "A non-negative decimal integer used to restrict the upper bound of depth of control flow graph")
        enableStrictFP = OptionResolver.addBooleanOption("enable-strict-fp", "Add strictfp attribute to test class")
        printComplexity = OptionResolver.addBooleanOption("print-complexity", "Print complexity of each statement")
        printHierarchy = OptionResolver.addBooleanOption("print-hierarchy", "Print resulting class hierarchy")
        //disableFinals = OptionResolver.addBooleanOption("disable-finals", "Don\'t use finals");
        disableFinalClasses = OptionResolver.addBooleanOption("disable-final-classes", "Don\'t use final classes")
        disableFinalMethods = OptionResolver.addBooleanOption("disable-final-methods", "Don\'t use final methods")
        disableFinalVariables = OptionResolver.addBooleanOption("disable-final-variabless", "Don\'t use final variables")
        disableIf = OptionResolver.addBooleanOption("disable-if", "Don\'t use conditionals")
        disableWhen = OptionResolver.addBooleanOption("disable-switch", "Don\'t use switch")
        disableWhile = OptionResolver.addBooleanOption("disable-while", "Don\'t use while")
        disableDoWhile = OptionResolver.addBooleanOption("disable-do-while", "Don\'t use do-while")
        disableFor = OptionResolver.addBooleanOption("disable-for", "Don\'t use for")
        disableFunctions = OptionResolver.addBooleanOption("disable-functions", "Don\'t use functions")
        disableVarsInBlock = OptionResolver.addBooleanOption("disable-vars-in-block", "Don\'t generate variables in blocks")
        disableExprInInit = OptionResolver.addBooleanOption("disable-expr-in-init", "Don\'t use complex expressions in variable initialization")
        disableExternalSymbols = OptionResolver.addBooleanOption("disable-external-symbols", "Don\'t use external symbols")
        addExternalSymbols = OptionResolver.addStringOption("add-external-symbols", "all", "Add symbols for listed classes (comma-separated list)")
        disableInheritance = OptionResolver.addBooleanOption("disable-inheritance", "Disable inheritance")
        disableDowncasts = OptionResolver.addBooleanOption("disable-downcasts", "Disable downcasting of objects")
        disableStatic = OptionResolver.addBooleanOption("disable-static", "Disable generation of static objects and functions")
        disableInterfaces = OptionResolver.addBooleanOption("disable-interfaces", "Disable generation of interfaces")
        disableClasses = OptionResolver.addBooleanOption("disable-classes", "Disable generation of classes")
        disableNestedBlocks = OptionResolver.addBooleanOption("disable-nested-blocks", "Disable generation of nested blocks")
        disableArrays = OptionResolver.addBooleanOption("disable-arrays", "Disable generation of arrays")
        enableFinalizers = OptionResolver.addBooleanOption("enable-finalizers", "Enable finalizers (for stress testing)")
        chanceExpressionIndex = OptionResolver.addIntOption("chance-expression-index", 0, "A non negative decimal integer used to restrict chane of generating expression in array index while creating or accessing by index")
        testbaseDir = OptionResolver.addStringOption("testbase-dir", ".", "Testbase dir")
        numberOfTests = OptionResolver.addIntOption('n', "number-of-tests", 0, "Number of test classes to generate")
        seed = OptionResolver.addStringOption("seed", "", "Random seed")
        specificSeed = OptionResolver.addLongOption('z', "specificSeed", 0L, "A seed to be set for specific test generation(regular seed still needed for initialization)")
        classesFile = OptionResolver.addStringOption('f', "classes-file", "conf/classes.lst", "File to read classes from")
        excludeMethodsFile = OptionResolver.addStringOption('r', "exclude-methods-file", "conf/exclude.methods.lst", "File to read excluded methods from")
        generators = OptionResolver.addStringOption("generators", "", "Comma-separated list of generator names")
        generatorsFactories = OptionResolver.addStringOption("generatorsFactories", "", "Comma-separated list of generators factories class names")
    }
}