
import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import exceptions.UnsuccessfullRunningException
import factories.utils.IRNodeBuilder
import information.Symbol
import information.SymbolTable
import information.TypeList
import ir.IRNode
import providers.testsgenerators.TestGenerator
import utils.OptionResolver
import utils.ProductionParams
import utils.PseudoRandom
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.nio.file.Path
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

const val MINUTES_TO_WAIT = 1L  //Process running time limit
const val OVERHEAD = 100L       //The overhead for running thread to avoid unexpected interruption of thread (and leaving hanged test process in memory)
var MAX_WAIT_TIME = 0L
const val SECONDS_TO_CLOSE = 5L //Time to destroy the process before the "destroyForcibly" invocation

const val JVM_DEVIATION = 1
const val NATIVE_DEVIATION = 1
const val JS_DEVIATION = 1

const val ARITHMETIC_EXIT = 136 // Native runtime ArithmeticException exit code (not an authentic information)

fun main(args: Array<String>) {
    if (!args.isEmpty() && (args[0] == "-h" || args[0] == "--help")){
        showHelp()
        return
    }
    initializeTestGenerators(args)

    val modsNum = if (ProductionParams.joinMode?.value() == true) 3 else {
        var amount = 0
        if (ProductionParams.jvmMode?.value() == true) amount++
        if (ProductionParams.nativeMode?.value() == true) amount++
        if (ProductionParams.jsMode?.value() == true) amount++
        amount
    }
    if (modsNum == 0) throw Error("Compilation mode not specified")
    MAX_WAIT_TIME = TimeUnit.MINUTES.toMillis(MINUTES_TO_WAIT * modsNum) + OVERHEAD

    if (ProductionParams.joinMode?.value() == true) println("JOIN MODE") else {
        if (ProductionParams.jvmMode?.value() == true) println("JVM MODE")
        if (ProductionParams.nativeMode?.value() == true) println("NATIVE MODE")
        if (ProductionParams.jsMode?.value() == true) println("JS MODE")
    }

    var counter = 0
    System.out.printf(" %14s | %8s | %11s | %8s | %11s |%n", "start time", "count", "generating",
            "running", "status")
    System.out.printf(" %14s | %8s | %11s | %8s | %11s |%n", "---", "---", "---", "---", "---")

    val generators = getTestGenerators()
    TestGenerator.ensureExisting(TestGenerator.getRoot())
    if (generators.isEmpty()) {
        throw Error("No generators loaded")
    }
    else {
        val gen = generators[0]
        TestGenerator.deleteRecursively(gen.generatorDir.toFile())
        gen.extractPrinter()
        try {
            if (ProductionParams.joinMode?.value() == true || ProductionParams.jvmMode?.value() == true) {
                gen.compilePrinterJVM()
            }
            if (ProductionParams.joinMode?.value() == true || ProductionParams.nativeMode?.value() == true) {
                gen.compilePrinterNative()
            }
            if (ProductionParams.joinMode?.value() == true || ProductionParams.jsMode?.value() == true) {
                gen.compilePrinterJS()
            }
        } finally {
            gen.deletePrinter()
        }
    }

    val names = mutableListOf<String>()
    do {
        var start = System.currentTimeMillis()
        System.out.printf(" %14s |", "[${LocalTime.now()}]")
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
            val runTime = System.currentTimeMillis() - start
            System.out.printf(" %8d |", runTime)
            System.out.printf(" interrupted |%n")
        } else {
            val runTime = System.currentTimeMillis() - start
            System.out.printf(" %8d |", runTime)
            val status = if (allCorrect) "success" else "ERROR"
            System.out.printf(" %11s |%n", status)
            if (runTime < MAX_WAIT_TIME) {
                counter++
            }
        }
    } while (counter < ProductionParams.numberOfTests?.value() ?: throw NotInitializedOptionException("numberOfTests"))

    analyzeResults(generators, names)
}

fun initializeTestGenerators(args: Array<String>) {
    ProductionParams.register()
    OptionResolver.parse(args)                          //TODO: cover IllegalArgumentException
    PseudoRandom.reset(ProductionParams.seed?.value())
    println("Current seed: ${PseudoRandom.currentSeed}")
//    TypesParser.parseTypesAndMethods(ProductionParams.classesFile?.value() ?: throw NotInitializedOptionException("classesFile"),
//            ProductionParams.excludeMethodsFile?.value() ?: throw NotInitializedOptionException("excludedMethodsFile"))
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
                setMemberFunctionsArgLimit(ProductionParams.functionsArgLimit?.value() ?: throw NotInitializedOptionException("functionsArgLimit")).
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

fun analyzeResults(gens: List<TestGenerator>, names: List<String>) {
    var allCorrect = true
    var divByZero = false
    var runtimeErrors = 0
    var compileErrors = 0
    var hangs = 0
    var diffCases = 0

    val modsNum = if (ProductionParams.joinMode?.value() == true) 3 else {
        var amount = 0
        if (ProductionParams.jvmMode?.value() == true) amount++
        if (ProductionParams.nativeMode?.value() == true) amount++
        if (ProductionParams.jsMode?.value() == true) amount++
        amount
    }

    val crashes = TestGenerator.getRoot().resolve("crashes").resolve(PseudoRandom.currentSeed)
    TestGenerator.deleteRecursively(crashes.toFile())
    TestGenerator.ensureExisting(crashes)
    val crashReport = crashes.resolve("report.txt").toFile()
    if (!crashReport.exists()) crashReport.createNewFile()

    val reportDir = TestGenerator.getRoot().resolve("reports")
    TestGenerator.ensureExisting(reportDir)
    val report = reportDir.resolve("report_${PseudoRandom.currentSeed}.txt").toFile()
    if (report.exists()) report.delete()
    report.createNewFile()

    val regularWriter = BufferedWriter(FileWriter(report))
    val crashWriter = BufferedWriter(FileWriter(crashReport))

    val writers = mutableListOf<BufferedWriter>()
    writers.add(regularWriter)
    writers.add(crashWriter)

    val numTests = (ProductionParams.numberOfTests?.value() ?: throw NotInitializedOptionException("numberOfTests"))
    regularWriter.write("$numTests tests generated\n")
    crashWriter.write("$numTests tests generated\n")

    for (i in 0 until numTests) {
        next_gen@ for (gen in gens) {
            val compliation = gen.generatorDir.resolve(names[i]).resolve("compile")
            val jvmCompile = compliation.resolve("jvm").resolve("${names[i]}.exit").toFile()
            val nativeCompile = compliation.resolve("native").resolve("${names[i]}.exit").toFile()
            val jsCompile = compliation.resolve("js").resolve("${names[i]}.exit").toFile()

            val runtime = gen.generatorDir.resolve(names[i]).resolve("runtime")
            val jvmRuntime = runtime.resolve("jvm").resolve("${names[i]}.exit").toFile()
            val nativeRuntime = runtime.resolve("native").resolve("${names[i]}.exit").toFile()
            val jsRuntime = runtime.resolve("js").resolve("${names[i]}.exit").toFile()

            var jvmReader: Scanner? = null
            var nativeReader: Scanner? = null
            var jsReader: Scanner? = null

            var compileDifference = 0
            var runtimeDifference = 0
            try {
                if (jvmCompile.exists()) {
                    jvmReader = Scanner(jvmCompile)
                    if (jvmReader.nextInt() != 0) {
                        writeInfo("$gen: <Kotlin JVM> compilation error in ${names[i]} folder", writers)
                        allCorrect = false
                        compileErrors++
                        compileDifference = compileDifference xor JVM_DEVIATION

                        copyBuggyFile(crashes, gen, names[i])
                    }
                }

                if (nativeCompile.exists()) {
                    nativeReader = Scanner(nativeCompile)
                    if (nativeReader.nextInt() != 0) {
                        writeInfo("$gen: <Kotlin/Native> compilation error in ${names[i]} folder", writers)
                        allCorrect = false
                        compileErrors++
                        compileDifference = compileDifference xor NATIVE_DEVIATION

                        copyBuggyFile(crashes, gen, names[i])
                    }
                }

                if (jsCompile.exists()) {
                    jsReader = Scanner(jsCompile)
                    if (jsReader.nextInt() != 0) {
                        writeInfo("$gen: <Kotlin JS> compilation error in ${names[i]} folder", writers)
                        allCorrect = false
                        compileErrors++
                        compileDifference = compileDifference xor JS_DEVIATION

                        copyBuggyFile(crashes, gen, names[i])
                    }
                }

                try {
                    if (jvmRuntime.exists()) {
                        jvmReader = Scanner(jvmRuntime)
                        val exit = jvmReader.nextLine()
                        if (exit == "interrupted" || exit.toInt() != 0) {
                            if (runtime.resolve("jvm").resolve("${names[i]}.err").toFile().exists()) {
                                jvmReader.close()
                                jvmReader = Scanner(runtime.resolve("jvm").resolve("${names[i]}.err").toFile())
                                while (jvmReader.hasNextLine()) {
                                    val str = jvmReader.nextLine()
                                    if (str.contains("ArithmeticException")) {
                                        if (str.contains("/ by zero")) {
                                            jvmReader.close()
                                            divByZero = true
                                            throw ProductionFailedException()
                                        }
                                    }
                                }

                                if (exit == "interrupted") {
                                    if (ProductionParams.ignoreHanging?.value()?.not() == true) {
                                        writeInfo("$gen: <Kotlin JVM> program hanged in ${names[i]} folder", writers)
                                        hangs++
                                    }

                                } else {
                                    writeInfo("$gen: <Kotlin JVM> program running error in ${names[i]} folder", writers)
                                    runtimeErrors++

                                    copyBuggyFile(crashes, gen, names[i])
                                }
                                allCorrect = false
                                runtimeDifference = runtimeDifference xor JVM_DEVIATION
                            } else {
                                writeInfo("$gen: <Kotlin JVM> program running error in ${names[i]} folder, cannot find output file", writers)
                                runtimeErrors++

                                copyBuggyFile(crashes, gen, names[i])
                            }
                        }
                    }
                } catch (ex: ProductionFailedException) {}  //TODO: hardcode

                try {
                    if (jsRuntime.exists()) {
                        jsReader = Scanner(jsRuntime)
                        val exit = jsReader.nextLine()
                        if (exit == "interrupted" || exit.toInt() != 0) {
                            if (runtime.resolve("js").resolve("${names[i]}.err").toFile().exists()) {
                                jsReader.close()
                                jsReader = Scanner(runtime.resolve("js").resolve("${names[i]}.err").toFile())
                                while (jsReader.hasNextLine()) {
                                    val str = jsReader.nextLine()
                                    if (str.contains("ArithmeticException")) {
                                        if (str.contains("/ by zero")) {
                                            jsReader.close()
                                            divByZero = true
                                            throw ProductionFailedException()
                                        }
                                    }
                                }

                                if (exit == "interrupted") {
                                    if (ProductionParams.ignoreHanging?.value()?.not() == true) {
                                        writeInfo("$gen: <Kotlin JS> program hanged in ${names[i]} folder", writers)
                                        hangs++
                                    }

                                } else {
                                    writeInfo("$gen: <Kotlin JS> program running error in ${names[i]} folder", writers)
                                    runtimeErrors++

                                    copyBuggyFile(crashes, gen, names[i])
                                }
                                allCorrect = false
                                runtimeDifference = runtimeDifference xor JS_DEVIATION
                            } else {
                                writeInfo("$gen: <Kotlin JS> program running error in ${names[i]} folder, cannot find output file", writers)
                                runtimeErrors++

                                copyBuggyFile(crashes, gen, names[i])
                            }
                        }
                    }
                } catch (ex: ProductionFailedException) {}  //TODO: hardcode

                if (nativeRuntime.exists()) {
                    nativeReader = Scanner(nativeRuntime)
                    val exit = nativeReader.nextLine()
                    if (exit == "interrupted" || exit.toInt() != 0) {
                        if (exit.toInt() == ARITHMETIC_EXIT && divByZero) continue@next_gen
                        if (runtime.resolve("native").resolve("${names[i]}.out").toFile().exists()) {
                            nativeReader.close()
                            nativeReader = Scanner(runtime.resolve("native").resolve("${names[i]}.out").toFile())
                            while (nativeReader.hasNextLine()) {
                                if (nativeReader.nextLine().contains("ArithmeticException") && divByZero) {       // it may theoretically work incorrect if the
                                    nativeReader.close()                                                                // KotlinJVM produces "division by zero" and
                                    continue@next_gen                                                                   // Kotlin/Native produces ArithmeticException
                                }                                                                                       // in the other part of code in the same time...
                            }
                            if (exit == "interrupted") {
                                if (ProductionParams.ignoreHanging?.value()?.not() == true) {
                                    writeInfo("$gen: <Kotlin/Native> program hanged in ${names[i]} folder", writers)
                                    hangs++
                                }
                            } else {
                                writeInfo("$gen: <Kotlin/Native> program running error in ${names[i]} folder: exit code ${exit.toInt()}", writers)
                                runtimeErrors++

                                copyBuggyFile(crashes, gen, names[i])
                            }
                            allCorrect = false
                            runtimeDifference = runtimeDifference xor NATIVE_DEVIATION
                        }
                        else {
                            writeInfo("$gen: <Kotlin/Native> program running error in ${names[i]} folder, cannot find output file", writers)
                            runtimeErrors++

                            copyBuggyFile(crashes, gen, names[i])
                        }
                    }
                }

                if (modsNum > 1){
                    if (compileDifference != 0) {
                        allCorrect = false
                        writeInfo("$gen: different behaviour of compilers while compiling ${names[i]}", writers)
                        diffCases++

                        copyBuggyFile(crashes, gen, names[i])
                    }
                    if (runtimeDifference != 0) {
                        allCorrect = false
                        writeInfo("$gen: different behaviour of compilers while running ${names[i]}", writers)
                        diffCases++

                        copyBuggyFile(crashes, gen, names[i])
                    }
                    val jvm_out = gen.generatorDir.resolve(names[i]).resolve("runtime").resolve("jvm").resolve("${names[i]}.out").toFile()
                    val native_out = gen.generatorDir.resolve(names[i]).resolve("runtime").resolve("native").resolve("${names[i]}.out").toFile()
                    val js_out = gen.generatorDir.resolve(names[i]).resolve("runtime").resolve("js").resolve("${names[i]}.out").toFile()

                    jvmReader = null
                    nativeReader = null
                    jsReader = null

                    if (jvm_out.exists()) jvmReader = Scanner(jvm_out)
                    if (native_out.exists()) nativeReader = Scanner(native_out)
                    if (js_out.exists()) jsReader = Scanner(js_out)

                    var different = false
                    while (jvmReader?.hasNextLine() != false && nativeReader?.hasNextLine() != false && jsReader?.hasNextLine() != false){
                        val jvmLine = jvmReader?.nextLine()
                        val nativeLine = nativeReader?.nextLine()
                        val jsLine = jsReader?.nextLine()

                        if (
                                (jvmLine != null && jsLine != null && jvmLine != jsLine) ||
                                (jvmLine != null && nativeLine != null && jvmLine != nativeLine) ||
                                (jsLine != null && nativeLine != null && jsLine != nativeLine)
                        ) {
                            different = true
                            break
                        }
                    }
                    if (different || jvmReader?.hasNextLine() != false || nativeReader?.hasNextLine() != false || jsReader?.hasNextLine() != false) {
                        allCorrect = false
                        writeInfo("$gen: different program outputs depending on the compiler while running ${names[i]}", writers)
                        diffCases++

                        copyBuggyFile(crashes, gen, names[i])
                    }
                }
            } finally {
                jvmReader?.close()
                nativeReader?.close()
                jsReader?.close()
            }
        }
    }

    if (allCorrect) {
        val str = "No compilation or running errors${if (divByZero) " (except for division by zero)" else ""} on all tests${if (modsNum > 1) ", no difference in behaviour" else ""}"
        writeInfo(str, writers)
        TestGenerator.deleteRecursively(crashes.toFile())
    } else {
        val hangInfo = if (ProductionParams.ignoreHanging?.value()?.not() == true) ", hangs: $hangs" else ""
        val diffInfo = if (modsNum > 1) ", cases of different behaviour: $diffCases" else ""
        writeInfo("\nCompile errors: $compileErrors, runtime errors: $runtimeErrors$hangInfo$diffInfo", writers)

        val input = FileInputStream(report)

        val output = FileOutputStream(crashReport)
        var len: Int
        val buf = ByteArray(1024) {0}
        len = input.read(buf)
        while (len > 0) {
            output.write(buf, 0, len)
            len = input.read(buf)
        }
        input.close()
        output.close()
    }

    for (writer in writers) {
        writer.close()
    }
}

fun writeInfo(msg: String, writers: List<BufferedWriter>) {
    println(msg)
    for (writer in writers) {
        writer.write("$msg\n")
    }
}

fun copyBuggyFile(to: Path, gen: TestGenerator, name: String) {
    val srcRoot = gen.generatorDir.resolve(name)
    val mainDest = to.resolve(name)

    if (!mainDest.toFile().exists()) {
        TestGenerator.ensureExisting(mainDest)
        val input = FileInputStream(srcRoot.resolve("$name.kt").toFile())
        val output = FileOutputStream(mainDest.resolve("$name.kt").toFile())
        var len: Int
        val buf = ByteArray(1024) {0}
        len = input.read(buf)
        while (len > 0) {
            output.write(buf, 0, len)
            len = input.read(buf)
        }
        input.close()
        output.close()
    }
    if (!mainDest.resolve("compile").toFile().exists()) {
        TestGenerator.ensureExisting(mainDest.resolve("compile"))
        copyFolder(srcRoot.resolve("compile"), mainDest)
    }
    if (!mainDest.resolve("runtime").toFile().exists()) {
        TestGenerator.ensureExisting(mainDest.resolve("runtime"))
        copyFolder(srcRoot.resolve("runtime"), mainDest)
    }
}

fun copyFolder(from: Path, to: Path) {
    if (from.toFile().isDirectory) {
        for (file in from.toFile().listFiles()) {
            copyFolder(file.toPath(), to.resolve(from.fileName))
        }
    } else {
        val inputStream = FileInputStream(from.toString())
        val dest = to.resolve(from.fileName)
        TestGenerator.ensureExisting(dest.parent)
        if (!dest.toFile().exists()) dest.toFile().createNewFile()
        val outputStream = FileOutputStream(dest.toString())
        val buf = ByteArray(1024) {0}
        var len = inputStream.read(buf)
        while (len > 0) {
            outputStream.write(buf, 0, len)
            len = inputStream.read(buf)
        }
        inputStream.close()
        outputStream.close()
    }
}

fun showHelp(){
    println("Option formats:")
    println("-<short name> <expected value>")
    println("--<long name> <expected value>")
    println("Example: --dimensions-limit 4\n")
    println("Also you can add options by its long names to file defined by \'property-file\' option according to pattern <option name>=<option value>\n")
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