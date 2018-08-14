
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
import java.util.*
import java.util.concurrent.TimeUnit

val MINUTES_TO_WAIT = 3L
val MAX_WAIT_TIME = TimeUnit.MINUTES.toMillis(MINUTES_TO_WAIT)

fun main(args: Array<String>) {
    if (!args.isEmpty() && (args[0] == "-h" || args[0] == "--help")){
        showHelp()
        return
    }
    initializeTestGenerators(args)
    var counter = 0
    System.out.printf(" %13s | %8s | %11s | %8s |%n", "start time", "count", "generating",
            "running")
    System.out.printf(" %13s | %8s | %11s | %8s |%n", "---", "---", "---", "---")
    val generators = getTestGenerators()
    val names = mutableListOf<String>()
    do {
        var start = System.currentTimeMillis()
        print("[" + LocalTime.now() + "] |")
        val name = "Test_$counter"
        val irTree = generateIRTreeWithoutOOP(name)
        names.add(irTree.first.getName())
        System.out.printf(" %8d |", counter)
        val generationTime = System.currentTimeMillis() - start
        System.out.printf(" %11d |", generationTime)
        start = System.currentTimeMillis()
        val generatorThread = Thread {
            for (generator in generators) {
                generator.accept(irTree.first, irTree.second)
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
            System.out.printf(" %8d |%n", runTime)
            if (runTime < MAX_WAIT_TIME) {
                counter++
            }
        }
    } while (counter < ProductionParams.numberOfTests?.value() ?: throw NotInitializedOptionException("numberOfTests"))

    val noErrors = printBadCompilsAndRuns(generators, names)
    if (noErrors) println("No compilation or running errors on all tests")
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

fun generateIRTreeWithoutOOP(name: String): Pair<IRNode, IRNode> {
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
                setLevel(0).
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

fun printBadCompilsAndRuns(gens: List<TestsGenerator>, names: List<String>): Boolean {
    var allCorrect = true
    for (i in 0 until (ProductionParams.numberOfTests?.value() ?: throw NotInitializedOptionException("numberOfTests"))) {
        for (gen in gens) {
            val compliation = gen.generatorDir.resolve(names[i]).resolve("compile").resolve("${names[i]}.exit").toFile()
            val runtime = gen.generatorDir.resolve(names[i]).resolve("runtime").resolve("${names[i]}.exit").toFile()
            var comReader: Scanner? = null
            var runReader: Scanner? = null
            try {
                comReader = Scanner(compliation)
                if (comReader.nextInt() != 0) {
                    println("$gen: compilation error in ${names[i]} folder")
                    allCorrect = false
                }

                runReader = Scanner(runtime)
                if (runReader.nextInt() != 0) {
                    println("$gen: program running error in ${names[i]} folder")
                    allCorrect = false
                }
            } finally {
                comReader?.close()
                runReader?.close()
            }
        }
    }
    return allCorrect
}

fun showHelp(){
    println("Option formats:")
    println("-<short name> <expected value>")
    println("--<long name> <expected value>")
    println("Example: --dimensions-limit 4\n")
    println("Also you can add options by its long names to file \"conf/default.properties\" according to pattern <option name>=<option value>\n")
    println(" <short name> |         <long name>        |     <expected value>    |      <default value>     | <description>")
    ProductionParams.register()
    val options = OptionResolver.getRegisteredOptions()
    for (option in options) {
        val def = option.defaultValue
        val expected = when (def) {
            is Char -> "char symbol"
            is Int -> "integer number    "
            is Long -> "long integer     "
            is Double -> "number from 0.0 to 1.0"
            is String -> "string        "
            is Boolean -> "true/false (optionally)"
            else -> ""
        }
        val space_str = StringBuilder()
        for (i in 0 until (24 - option.defaultValue.toString().length) / 2){
            space_str.append(" ")
        }
        if (option.haveShort()) System.out.printf("           -%c | %-26s | %23s | %24s | %s\n",
                option.shortName, option.longName, expected, option.defaultValue.toString() + space_str.toString(), option.description)
        else System.out.printf(" %12s | %-26s | %23s | %24s | %s\n", "", option.longName, expected, option.defaultValue.toString() + space_str.toString(), option.description)
    }
}