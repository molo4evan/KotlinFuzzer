
import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import exceptions.UnsuccessfullRunningException
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
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

const val MINUTES_TO_WAIT = 1L  //Process running time limit
const val OVERHEAD = 100L       //The overhead for running thread to avoid unexpected interruption of thread (and leaving hanged test process in memory)
val MAX_WAIT_TIME = (if (ProductionParams.joinTest?.value() == true) TimeUnit.MINUTES.toMillis(MINUTES_TO_WAIT * 4) else TimeUnit.MINUTES.toMillis(MINUTES_TO_WAIT * 2)) + OVERHEAD
const val JVM_DEVIATION = 1
const val NATIVE_DEVIATION = 1

fun main(args: Array<String>) {
    if (!args.isEmpty() && (args[0] == "-h" || args[0] == "--help")){
        showHelp()
        return
    }
    initializeTestGenerators(args)
    var counter = 0
    System.out.printf(" %13s | %8s | %11s | %8s | %7s |%n", "start time", "count", "generating",
            "running", "status")
    System.out.printf(" %13s | %8s | %11s | %8s | %7s |%n", "---", "---", "---", "---", "---")

    val generators = getTestGenerators()
    TestGenerator.ensureExisting(TestGenerator.getRoot())
    if (generators.isEmpty()) {
        throw Error("No generators loaded")
    }
    else {
        generators[0].extractPrinter()
        try {
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
        } finally {
            generators[0].deletePrinter()
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
        var allCorrect = true
        val generatorThread = Thread {
            for (generator in generators) {
                try {
                    generator.accept(irTree.first, irTree.second)
                } catch (ex: UnsuccessfullRunningException) { allCorrect = false }
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
            System.out.printf(" %8d |", runTime)
            val status = if (allCorrect) "success" else "ERROR"
            System.out.printf(" %7s |%n", status)
            if (runTime < MAX_WAIT_TIME) {
                counter++
            }
        }
    } while (counter < ProductionParams.numberOfTests?.value() ?: throw NotInitializedOptionException("numberOfTests"))

    printBadCompilsAndRuns(generators, names)
}

fun initializeTestGenerators(args: Array<String>) {
    ProductionParams.register()
    OptionResolver.parse(args)                          //TODO: cover IllegalArgumentException
    PseudoRandom.reset(ProductionParams.seed?.value())
//    TypesParser.parseTypesAndMethods(ProductionParams.classesFile?.value() ?: throw NotInitializedOptionException("classesFile"),
//            ProductionParams.excludeMethodsFile?.value() ?: throw NotInitializedOptionException("excludedMethodsFile"))
    if (ProductionParams.specificSeed?.isSet() ?: throw NotInitializedOptionException("specificSeed")) {
        PseudoRandom.setCurrentSeed(ProductionParams.specificSeed!!.value())
    }
}

fun getTestGenerators(): List<TestGenerator> {
    val result = mutableListOf<TestGenerator>()
    var factoryClass: KClass<*>
    var factory: (List<String>) -> List<TestGenerator>
    val factoryClassNames = ProductionParams.generatorsFactories?.value()?.split(",") ?: throw NotInitializedOptionException("generatorsFactories")
    val generatorNames = ProductionParams.generators?.value()?.split(",") ?: throw NotInitializedOptionException("generators")

    for (factoryClassName in factoryClassNames) {
        try {
            factoryClass = Class.forName(factoryClassName).kotlin
            factory = factoryClass.createInstance() as (List<String>) -> List<TestGenerator>
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

fun printBadCompilsAndRuns(gens: List<TestGenerator>, names: List<String>) {
    var allCorrect = true
    var divByZero = false
    for (i in 0 until (ProductionParams.numberOfTests?.value() ?: throw NotInitializedOptionException("numberOfTests"))) {
        next_gen@ for (gen in gens) {
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
                    val exit = jvmReader.nextLine()
                    if (exit == "interrupted" || exit.toInt() != 0) {
                        jvmReader.close()
                        jvmReader = Scanner(runtime.resolve("jvm").resolve("${names[i]}.err").toFile())
                        while (jvmReader.hasNextLine()){
                            val str = jvmReader.nextLine()
                            if (str.contains("ArithmeticException") && ProductionParams.ignoreArithmeticExceptions?.value() == true){
                                jvmReader.close()
                                continue@next_gen
                            }
                            if (str.contains("/ by zero")) {
                                jvmReader.close()
                                divByZero = true
                                continue@next_gen
                            }
                        }

                        if (exit == "interrupted") println("$gen: <Kotlin JVM> program hanged in ${names[i]} folder")
                        else println("$gen: <Kotlin JVM> program running error in ${names[i]} folder")
                        allCorrect = false
                        runtimeDifference = runtimeDifference xor JVM_DEVIATION
                    }
                }

                if (nativeRuntime.exists()) {
                    nativeReader = Scanner(nativeRuntime)
                    val exit = nativeReader.nextLine()
                    if (exit == "interrupted" || exit.toInt() != 0) {
                        nativeReader.close()
                        nativeReader = Scanner(runtime.resolve("native").resolve("${names[i]}.out").toFile())
                        while (nativeReader.hasNextLine()){
                            if (nativeReader.nextLine().contains("ArithmeticException") && ProductionParams.ignoreArithmeticExceptions?.value() == true){
                                nativeReader.close()
                                continue@next_gen
                            }
                        }
                        if (exit == "interrupted") println("$gen: <Kotlin/Native> program hanged in ${names[i]} folder")
                        else println("$gen: <Kotlin/Native> program running error in ${names[i]} folder")
                        allCorrect = false
                        runtimeDifference = runtimeDifference xor NATIVE_DEVIATION
                    }
                }

                if (ProductionParams.joinTest?.value() == true){
                    if (compileDifference != 0) {
                        allCorrect = false
                        println("$gen: different behaviour of compilers while compiling ${names[i]}")
                    }
                    if (runtimeDifference != 0) {
                        allCorrect = false
                        println("$gen: different behaviour of compilers while running ${names[i]}")
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

    if (allCorrect) println("No compilation or running errors${if (divByZero) " (except for division by zero)" else ""} on all tests${if (ProductionParams.joinTest?.value() == true) ", no difference in behaviour" else ""}")
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