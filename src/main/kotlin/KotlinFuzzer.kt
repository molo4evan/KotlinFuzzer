
import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import information.Symbol
import information.SymbolTable
import information.TypeList
import ir.IRNode
import providers.tests_generators.TestsGenerator
import utils.OptionResolver
import utils.ProductionParams
import utils.PseudoRandom
import java.time.LocalTime
import java.util.concurrent.TimeUnit

val MINUTES_TO_WAIT = Integer.getInteger("jdk.test.lib.jittester", 3) //???
val MAX_WAIT_TIME = TimeUnit.MINUTES.toMillis(MINUTES_TO_WAIT.toLong())

fun main(args: Array<String>) {
    initializeTestGenerators(args)
    var counter = 0
    System.out.printf(" %13s | %8s | %8s | %8s |%n", "start time", "count", "generat",
            "running")
    System.out.printf(" %13s | %8s | %8s | %8s |%n", "---", "---", "---", "---")
    val generators = getTestGenerators()
    do {
        var start = System.currentTimeMillis()
        print("[" + LocalTime.now() + "] |")
        val name = "Test_$counter"
        val irTree = generateIRTreeWithoutOOP(name)
        System.out.printf(" %8d |", counter)
        val generationTime = System.currentTimeMillis() - start
        System.out.printf(" %8.0f |", generationTime)
        start = System.currentTimeMillis()
        val generatorThread = Thread {
            for (generator in generators) {
                //generate code
            }
        }
        generatorThread.start()
        try {
            generatorThread.join(MAX_WAIT_TIME)
        } catch (ex: InterruptedException) {
            throw Error ("Test generation interrupted: $ex", ex)
        }
        if (generatorThread.isAlive) {
            // maxTime reached, so, proceed to next test generation
            generatorThread.interrupt()
        } else {
            val runTime = System.currentTimeMillis() - start
            System.out.printf(" %8.0f |%n", runTime)
            if (runTime < MAX_WAIT_TIME) {
                counter++
            }
        }
    } while (counter < ProductionParams.numberOfTests?.value() ?: throw NotInitializedOptionException("numberOfTests"))
}

fun initializeTestGenerators(args: Array<String>) {
    val propertyFileOpt = OptionResolver.addStringOption(
            'p',
            "property-file",
            "conf/default.properties",
            "File to read properties from")
    ProductionParams.register()
    OptionResolver.parse(args, propertyFileOpt)
    PseudoRandom.reset(ProductionParams.seed?.value())
//    TypesParser.parseTypesAndMethods(ProductionParams.classesFile?.value() ?: throw NotInitializedOptionException("classesFile"),
//            ProductionParams.excludeMethodsFile?.value() ?: throw NotInitializedOptionException("excludedMethodsFile"))
    if (ProductionParams.specificSeed?.isSet() ?: throw NotInitializedOptionException("specificSeed")) {
        PseudoRandom.setCurrentSeed(ProductionParams.specificSeed!!.value())
    }
}

fun getTestGenerators(): List<TestsGenerator> {
    val result = mutableListOf<TestsGenerator>()
    var factoryClass: Class<*>
    var factory: (List<String>) -> List<TestsGenerator>
    val factoryClassNames = ProductionParams.generatorsFactories?.value()?.split(",") ?: throw NotInitializedOptionException("generatorsFactories")
    val generatorNames = ProductionParams.generators?.value()?.split(",") ?: throw NotInitializedOptionException("generators")

    for (factoryClassName in factoryClassNames) {
        try {
            factoryClass = Class.forName(factoryClassName)
            factory = factoryClass.newInstance() as (List<String>) -> List<TestsGenerator>
        } catch (ex: ReflectiveOperationException) {
            throw Error("Can't instantiate generators factory", ex)
        }
        result.addAll(factory(generatorNames))
    }
    return result
}

fun generateIRTreeWithoutOOP(name: String): Pair<IRNode, IRNode> {    //TODO: add top-level function calls from other functions
    SymbolTable.removeAll()
    TypeList.removeAll()

    val builder = IRNodeBuilder().setPrefix(name).setName(name).setLevel(0)
    val complexityLimit = ProductionParams.complexityLimit?.value() ?: throw NotInitializedOptionException("complexityLimit")

    val topLevelFunctions: IRNode
    val topFunComplexity = (PseudoRandom.random() * complexityLimit).toLong()
    try {
        topLevelFunctions = builder.setOwnerClass(null).
                setMemberFunctionsLimit(ProductionParams.memberFunctionsLimit?.value() ?: throw NotInitializedOptionException("memberFunctionsLimit")).
                setMemberFunctionsArgLimit(ProductionParams.memberFunctionsArgLimit?.value() ?: throw NotInitializedOptionException("memberFunctionsArgLimit")).
                setComplexityLimit(topFunComplexity).
                setStatementLimit(ProductionParams.statementLimit?.value() ?: throw NotInitializedOptionException("statementLimit")).
                setOperatorLimit(ProductionParams.operatorLimit?.value() ?: throw NotInitializedOptionException("operatorLimit")).
                setLevel(1).
                setFlags(Symbol.NONE).
                getFunctionDefinitionBlockFactory().produce()
    } catch (ex: ProductionFailedException) {
        ex.printStackTrace(System.out)
        throw ex
    }

    val mainFunction: IRNode
    val mainComplexity = (PseudoRandom.random() * complexityLimit).toLong()
    try {
        mainFunction = builder.setName(name).setComplexityLimit(mainComplexity).getMainFunctionFactory().produce()
    } catch (ex: ProductionFailedException) {
        ex.printStackTrace(System.out)
        throw ex
    }

    return Pair(mainFunction, topLevelFunctions)
}