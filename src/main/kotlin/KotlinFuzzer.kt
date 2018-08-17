
import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import factories.utils.IRNodeBuilder
import information.Symbol
import information.SymbolTable
import information.TypeList
import ir.IRNode
import providers.tests_generators.TestGenerator
import utils.OptionResolver
import utils.ProductionParams
import utils.PseudoRandom
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit

const val MINUTES_TO_WAIT = 3L
val MAX_WAIT_TIME = TimeUnit.MINUTES.toMillis(MINUTES_TO_WAIT)
const val JVM_DEVIATION = 1
const val NATIVE_DEVIATION = 1

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
    TestGenerator.ensureExisting(TestGenerator.getRoot())
    if (generators.isEmpty()) {
        throw Error("No generators loaded")
    }
    else {
        if (ProductionParams.joinTest?.value() == true) {
            generators[0].compilePrinterNative()
            generators[0].compilePrinterJVM()
        } else {
            if (ProductionParams.useNative?.value() == true) {
                generators[0].compilePrinterNative()
            } else {
                generators[0].compilePrinterJVM()
            }
        }
    }

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
    if (noErrors) println("No compilation or running errors on all tests${if (ProductionParams.joinTest?.value() == true) ", no difference in behaviour" else ""}")
}

fun initializeTestGenerators(args: Array<String>) {
    val propertyFileOpt = OptionResolver.addStringOption(
            'p',
            "property-file",
            "conf/default.properties",
            "File to read properties from")     //TODO: bad, move to ProductionParams
    ProductionParams.register()
    OptionResolver.parse(args, propertyFileOpt)
    PseudoRandom.reset(ProductionParams.seed?.value())
//    TypesParser.parseTypesAndMethods(ProductionParams.classesFile?.value() ?: throw NotInitializedOptionException("classesFile"),
//            ProductionParams.excludeMethodsFile?.value() ?: throw NotInitializedOptionException("excludedMethodsFile"))
    if (ProductionParams.specificSeed?.isSet() ?: throw NotInitializedOptionException("specificSeed")) {
        PseudoRandom.setCurrentSeed(ProductionParams.specificSeed!!.value())
    }
}

fun getTestGenerators(): List<TestGenerator> {
    val result = mutableListOf<TestGenerator>()
    var factoryClass: Class<*>
    var factory: (List<String>) -> List<TestGenerator>
    val factoryClassNames = ProductionParams.generatorsFactories?.value()?.split(",") ?: throw NotInitializedOptionException("generatorsFactories")
    val generatorNames = ProductionParams.generators?.value()?.split(",") ?: throw NotInitializedOptionException("generators")

    for (factoryClassName in factoryClassNames) {
        try {
            factoryClass = Class.forName(factoryClassName)
            factory = factoryClass.newInstance() as (List<String>) -> List<TestGenerator>
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

fun printBadCompilsAndRuns(gens: List<TestGenerator>, names: List<String>): Boolean {
    var allCorrect = true
    for (i in 0 until (ProductionParams.numberOfTests?.value() ?: throw NotInitializedOptionException("numberOfTests"))) {
        for (gen in gens) {
            val compliation = gen.generatorDir.resolve(names[i]).resolve("compile")
            val jvmCompile = compliation.resolve("jvm").resolve("${names[i]}.exit").toFile()
            val nativeCompile = compliation.resolve("native").resolve("${names[i]}.exit").toFile()
            val runtime = gen.generatorDir.resolve(names[i]).resolve("runtime")
            val jvmRuntime = runtime.resolve("jvm").resolve("${names[i]}.exit").toFile()
            val nativeRuntime = runtime.resolve("native").resolve("${names[i]}.exit").toFile()
            var jvmReader: Scanner? = null
            var nativeReader: Scanner? = null
            var compileDifference = 0
            var runtimeDifference = 0
            try {
                if (jvmCompile.exists()) {
                    jvmReader = Scanner(jvmCompile)
                    if (jvmReader.nextInt() != 0) {
                        println("$gen: <Kotlin JVM> compilation error in ${names[i]} folder")
                        allCorrect = false
                        compileDifference = compileDifference xor JVM_DEVIATION
                    }
                }

                if (nativeCompile.exists()) {
                    nativeReader = Scanner(nativeCompile)
                    if (nativeReader.nextInt() != 0) {
                        println("$gen: <Kotlin/Native> compilation error in ${names[i]} folder")
                        allCorrect = false
                        compileDifference = compileDifference xor NATIVE_DEVIATION
                    }
                }

                if (jvmRuntime.exists()) {
                    jvmReader = Scanner(jvmRuntime)
                    if (jvmReader.nextInt() != 0) {
                        println("$gen: <Kotlin JVM> program running error in ${names[i]} folder")
                        allCorrect = false
                        runtimeDifference = runtimeDifference xor JVM_DEVIATION
                    }
                }

                if (nativeRuntime.exists()) {
                    nativeReader = Scanner(nativeRuntime)
                    if (nativeReader.nextInt() != 0) {
                        println("$gen: <Kotlin/Native> program running error in ${names[i]} folder")
                        allCorrect = false
                        runtimeDifference = runtimeDifference xor NATIVE_DEVIATION
                    }
                }

                if (ProductionParams.joinTest?.value() == true){
                    if (compileDifference != 0) {
                        allCorrect = false
                        println("$gen: different compilers behaviour while compiling ${names[i]}")
                    }
                    if (runtimeDifference != 0) {
                        allCorrect = false
                        println("$gen: different compilers behaviour while running ${names[i]}")
                    }
                    jvmReader = Scanner(gen.generatorDir.resolve(names[i]).resolve("runtime").resolve("jvm").resolve("${names[i]}.out").toFile())
                    nativeReader = Scanner(gen.generatorDir.resolve(names[i]).resolve("runtime").resolve("native").resolve("${names[i]}.out").toFile())

                    var different = false
                    while (jvmReader.hasNextLine() && nativeReader.hasNextLine()) {
                        if (jvmReader.nextLine() != nativeReader.nextLine()) {
                            different = true
                            break
                        }
                    }
                    if (different || jvmReader.hasNextLine() xor nativeReader.hasNextLine()) {
                        allCorrect = false
                        println("$gen: different program outputs depending on the compiler while running ${names[i]}")
                    }
                }
            } finally {
                jvmReader?.close()
                nativeReader?.close()
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
        val spaceStr = StringBuilder()
        for (i in 0 until (24 - option.defaultValue.toString().length) / 2){
            spaceStr.append(" ")
        }
        if (option.haveShort()) System.out.printf("           -%c | %-26s | %23s | %24s | %s\n",
                option.shortName, option.longName, expected, option.defaultValue.toString() + spaceStr.toString(), option.description)
        else System.out.printf(" %12s | %-26s | %23s | %24s | %s\n", "", option.longName, expected, option.defaultValue.toString() + spaceStr.toString(), option.description)
    }
}