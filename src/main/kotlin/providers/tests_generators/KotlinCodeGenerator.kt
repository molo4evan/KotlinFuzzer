package providers.tests_generators

import exceptions.UnsuccessfullRunningException
import ir.IRNode
import providers.visitors.KotlinCodeVisitor
import utils.ProductionParams
import java.io.IOException

class KotlinCodeGenerator(
        prefix: String,
        preRunActions: (String) -> Array<String>,
        jtDriverOptions: String
): TestsGenerator(prefix, preRunActions, jtDriverOptions) {
    constructor(): this(DEFAULT_SUFFIX, {arrayOf("@compile $it.kt")}, "")  //???  TODO: figure out how prerun works


    companion object {
        private val DEFAULT_SUFFIX = "kotlin_tests"
    }

    override fun accept(main: IRNode, other: IRNode?) {
        val mainName = main.getName()
        generateSources(main, other)
        if (ProductionParams.useNative?.value() == true) {
            compileKotlinFileNative(mainName)
            runProgramNative(mainName)
        } else {
            try {
                compileKotlinFileJVM(mainName)
                runProgramJVM(mainName)
            } catch (ex: UnsuccessfullRunningException) {}
        }
    }

    private fun generateSources(main: IRNode, other: IRNode?){
        val mainName = main.getName()
        val code = StringBuilder()
        val vis = KotlinCodeVisitor()
        //code.append(getJtregHeader(mainName))
        if (other != null) {
            code.append(other.accept(vis))
        }
        code.append(main.accept(vis))
        ensureExisting(generatorDir.resolve(mainName))
        writeFile(generatorDir.resolve(mainName), "$mainName.kt", code.toString())
    }

    private fun compileKotlinFileJVM(mainName: String) {
        val pb = ProcessBuilder(KOTLINC_JVM, generatorDir.resolve(mainName).resolve("$mainName.kt").toString(), "-nowarn", "-d", generatorDir.resolve(mainName).toString())
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("compile"))
            runProcess(pb, generatorDir.resolve(mainName).resolve("compile").resolve(mainName).toString())
        } catch (e: IOException) {
            throw Error("Can't compile sources ", e)
        } catch (e: InterruptedException) {
            throw Error("Can't compile sources ", e)
        }
    }

    private fun compileKotlinFileNative(mainName: String) {
        val pb = ProcessBuilder(KOTLINC_NATIVE, generatorDir.resolve(mainName).resolve("$mainName.kt").toString(), "-nowarn", "-o", generatorDir.resolve(mainName).resolve("$mainName.kexe").toString())
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("compile"))
            val exit = runProcess(pb, generatorDir.resolve(mainName).resolve("compile").resolve(mainName).toString())
            if (exit != 0) throw UnsuccessfullRunningException()
        } catch (e: IOException) {
            throw Error("Can't compile sources ", e)
        } catch (e: InterruptedException) {
            throw Error("Can't compile sources ", e)
        }
    }
}