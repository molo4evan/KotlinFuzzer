package utils

import utils.OptionResolver.Option
import utils.OptionResolver.addBooleanOption

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
    // workaround: to reduce chance throwing ArrayIndexOutOfBoundsException
    var chanceExpressionIndex: Option<Double>? = null
    var testbaseDir: Option<String>? = null
    var numberOfTests: Option<Int>? = null
    var seed: Option<String>? = null
    var specificSeed: Option<Long>? = null
    var classesFile: Option<String>? = null
    var excludeMethodsFile: Option<String>? = null
    var generators: Option<String>? = null
    var generatorsFactories: Option<String>? = null
    var functionCallsPercent: Option<Double>? = null

    var useNative: Option<Boolean>? = null
    var nativePath: Option<String>? = null
    var joinTest: Option<Boolean>? = null

    fun register() {
        memberFunctionsArgLimit = OptionResolver.addIntOption('a', "member-functions-arg-limit", 5, "Upper limit on the number of member function args")
        classesLimit = OptionResolver.addIntOption('c', "classes-limit", 12, "Upper limit on the number of classes")
        dimensionsLimit = OptionResolver.addIntOption('d', "dimensions-limit", 3, "Upper limit on array dimensions")
        testStatementLimit = OptionResolver.addIntOption('e', "test-statement-limit", 300, "Upper limit on statements in test() function")
        classesFile = OptionResolver.addStringOption('f', "classes-file", "conf/classes.lst", "File to read classes from")
        implementationLimit = OptionResolver.addIntOption('i', "implementation-limit", 3, "Upper limit on a number of interfaces a class can implement")
        joinTest = addBooleanOption('j', "join-test", false, "Test and compare both of Kotlin JVM and Kotlin/Native compilation")
        productionLimit = OptionResolver.addIntOption('l', "production-limit", 100, "Limit on steps in the production of an expression")
        memberFunctionsLimit = OptionResolver.addIntOption('m', "member-functions-limit", 15, "Upper limit on member functions")
        useNative = addBooleanOption('n', "use-native", false, "Switch compilation from Kotlin JVM to Kotlin/Native, doesn\'t matters, if \'join-test\' option is set")
        operatorLimit = OptionResolver.addIntOption('o', "operator-limit", 50, "Upper limit on operators in a statement")
        excludeMethodsFile = OptionResolver.addStringOption('r', "exclude-methods-file", "conf/exclude.methods.lst", "File to read excluded methods from")
        statementLimit = OptionResolver.addIntOption('s', "statement-limit", 30, "Upper limit on statements in function")
        numberOfTests = OptionResolver.addIntOption('t', "number-of-tests", 0, "Number of test classes to generate")
        dataMemberLimit = OptionResolver.addIntOption('v', "data-member-limit", 10, "Upper limit on data members")
        complexityLimit = OptionResolver.addLongOption('x', "complexity-limit", 10000000, "Upper limit on complexity")
        specificSeed = OptionResolver.addLongOption('z', "specificSeed", 0L, "A seed to be set for specific test generation(regular seed still needed for initialization)")
        addExternalSymbols = OptionResolver.addStringOption("add-external-symbols", "all", "Add symbols for listed classes (comma-separated list)")
        chanceExpressionIndex = OptionResolver.addDoubleOption("chance-expression-index", 0.0, "A floating point number used to restrict chance of generating expression in array index while creating or accessing by index")
        disableArrays = OptionResolver.addBooleanOption("disable-arrays", "Disable generation of arrays")
        disableClasses = OptionResolver.addBooleanOption("disable-classes", "Disable generation of classes")
        disableDoWhile = OptionResolver.addBooleanOption("disable-do-while", "Don\'t use do-while")
        disableDowncasts = OptionResolver.addBooleanOption("disable-downcasts", "Disable downcasting of objects")
        disableExternalSymbols = OptionResolver.addBooleanOption("disable-external-symbols", "Don\'t use external symbols")
        disableExprInInit = OptionResolver.addBooleanOption("disable-expr-in-init", "Don\'t use complex expressions in variable initialization")
        disableFinalClasses = OptionResolver.addBooleanOption("disable-final-classes", "Don\'t use final classes")
        disableFinalMethods = OptionResolver.addBooleanOption("disable-final-methods", "Don\'t use final methods")
        disableFinalVariables = OptionResolver.addBooleanOption("disable-final-variabless", "Don\'t use final variables")
        disableFor = OptionResolver.addBooleanOption("disable-for", "Don\'t use for")
        disableFunctions = OptionResolver.addBooleanOption("disable-functions", "Don\'t use functions")
        disableIf = addBooleanOption("disable-if", "Don\'t use if")
        disableInheritance = OptionResolver.addBooleanOption("disable-inheritance", "Disable inheritance")
        disableInterfaces = OptionResolver.addBooleanOption("disable-interfaces", "Disable generation of interfaces")
        disableNestedBlocks = OptionResolver.addBooleanOption("disable-nested-blocks", "Disable generation of nested blocks")
        disableStatic = OptionResolver.addBooleanOption("disable-static", "Disable generation of static objects and functions")
        disableVarsInBlock = OptionResolver.addBooleanOption("disable-vars-in-block", "Don\'t generate variables in blocks")
        disableWhen = OptionResolver.addBooleanOption("disable-when", "Don\'t use when")
        disableWhile = OptionResolver.addBooleanOption("disable-while", "Don\'t use while")
        enableFinalizers = OptionResolver.addBooleanOption("enable-finalizers", "Enable finalizers (for stress testing)")
        enableStrictFP = OptionResolver.addBooleanOption("enable-strict-fp", "Add strictfp attribute to test class")
        floatingPointPrecision = OptionResolver.addIntOption("fp-precision", 8, "A non-negative decimal integer used to restrict the number of digits after the decimal separator")
        functionCallsPercent = OptionResolver.addDoubleOption("function-calls-percent", 0.1, "Percentage of function calls in generating expressions")
        generators = OptionResolver.addStringOption("generators", "", "Comma-separated list of generator names")
        generatorsFactories = OptionResolver.addStringOption("generators-factories", "", "Comma-separated list of generators factories fully qualified class names")
        maxCfgDepth = OptionResolver.addIntOption("max-cfg-depth", 3, "A non-negative decimal integer used to restrict the upper bound of depth of control flow graph")
        minCfgDepth = OptionResolver.addIntOption("min-cfg-depth", 2, "A non-negative decimal integer used to restrict the lower bound of depth of control flow graph")
        nativePath = OptionResolver.addStringOption("native-path", "", "Kotlin/Native home directory (used if \'use-native\' option is \'true\')")
        printComplexity = OptionResolver.addBooleanOption("print-complexity", "Print complexity of each statement")
        printHierarchy = OptionResolver.addBooleanOption("print-hierarchy", "Print resulting class hierarchy")
        seed = OptionResolver.addStringOption("seed", "", "User seed (for the same seed, the same sequence of tests are generated)")
        stringLiteralSizeLimit = OptionResolver.addIntOption("string-literal-size-limit", 10, "Upper limit on the number of chars in string literal")
        testbaseDir = OptionResolver.addStringOption("testbase-dir", ".", "Directory to which the output production will be placed")
    }
}