package providers.testsgenerators

import exceptions.UnsuccessfullRunningException
import ir.IRNode
import providers.visitors.KotlinCodeVisitor
import utils.ProductionParams
import java.io.IOException

class KotlinCodeGenerator(
        suffix: String
): TestGenerator(suffix) {
    companion object {
        val DEFAULT_SUFFIX = "kotlin_tests"
    }

    constructor(): this(DEFAULT_SUFFIX)

    override fun accept(main: IRNode, other: IRNode?) {
        val mainName = main.getName()
        generateSources(main, other)
        if (ProductionParams.joinMode?.value() == true || ProductionParams.jvmMode?.value() == true) {
            compileKotlinFileJVM(mainName)
            runProgramJVM(mainName)
        }
        if (ProductionParams.joinMode?.value() == true || ProductionParams.nativeMode?.value() == true) {
            compileKotlinFileNative(mainName)
            runProgramNative(mainName)
        }
        if (ProductionParams.joinMode?.value() == true || ProductionParams.jsMode?.value() == true) {
            compileKotlinFileJS(mainName)
            runProgramJS(mainName)
        }
    }

    private fun generateSources(main: IRNode, other: IRNode?){
        val mainName = main.getName()
        val code = StringBuilder()
        val vis = KotlinCodeVisitor()
        if (other != null) {
            code.append(other.accept(vis))
        }
        code.append(main.accept(vis))
        ensureExisting(generatorDir.resolve(mainName))
        writeFile(generatorDir.resolve(mainName), "$mainName.kt", code.toString())
    }

    private fun compileKotlinFileJVM(mainName: String) {
        val pb = ProcessBuilder(
                KOTLINC_JVM,
                generatorDir.resolve(mainName).resolve("$mainName.kt").toString(),
                "-nowarn",
                "-include-runtime",
                "-d", generatorDir.resolve(mainName).resolve("$mainName.jar").toString(),
                "-cp", generatorDir.toString()
        )
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("compile").resolve("jvm"))
            runProcess(pb, generatorDir.resolve(mainName).resolve("compile").resolve("jvm").resolve(mainName).toString())
        } catch (e: IOException) {
            throw UnsuccessfullRunningException("Can't compile sources ", e)
        } catch (e: InterruptedException) {
            throw UnsuccessfullRunningException("Can't compile sources ", e)
        }
    }

    private fun compileKotlinFileNative(mainName: String) {
        val pb = ProcessBuilder(
                KOTLINC_NATIVE,
                generatorDir.resolve(mainName).resolve("$mainName.kt").toString(),
                "-nowarn",
                "-o", generatorDir.resolve(mainName).resolve("$mainName.kexe").toString(),
                "-l", generatorDir.resolve("Printer.klib").toString()
        )
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("compile").resolve("native"))
            /*val exit = */runProcess(pb, generatorDir.resolve(mainName).resolve("compile").resolve("native").resolve(mainName).toString())
           /* if (exit != 0) throw UnsuccessfullRunningException()*/
        } catch (e: IOException) {
            throw UnsuccessfullRunningException("Can't compile sources ", e)
        } catch (e: InterruptedException) {
            throw UnsuccessfullRunningException("Can't compile sources ", e)
        }
    }

    private fun compileKotlinFileJS(mainName: String) {
        val pb = ProcessBuilder(
                KOTLINC_JS,
                generatorDir.resolve(mainName).resolve("$mainName.kt").toString(),
                "-output", generatorDir.resolve(mainName).resolve("$mainName.js").toString(),
                "-libraries", generatorDir.toString()
        )
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("compile").resolve("js"))
            /*val exit = */runProcess(pb, generatorDir.resolve(mainName).resolve("compile").resolve("js").resolve(mainName).toString())
            /* if (exit != 0) throw UnsuccessfullRunningException()*/
        } catch (e: IOException) {
            throw UnsuccessfullRunningException("Can't compile sources ", e)
        } catch (e: InterruptedException) {
            throw UnsuccessfullRunningException("Can't compile sources ", e)
        }
    }
}