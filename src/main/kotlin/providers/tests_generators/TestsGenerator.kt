package providers.tests_generators

import MINUTES_TO_WAIT
import exceptions.NotInitializedOptionException
import ir.IRNode
import utils.ProductionParams
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

abstract class TestsGenerator protected constructor(
        suffix: String,
        protected val preRunActions: (String) -> Array<String>,
        protected val jtDriverOptions: String
): BiConsumer<IRNode, IRNode?> {
    val generatorDir: Path
    protected val classPath: String

    init {
        generatorDir = getRoot().resolve(suffix)
        classPath = getRoot().toString() + File.pathSeparator + generatorDir
    }

    protected constructor(suffix: String): this(suffix, { emptyArray<String>()}, "")

    companion object {
        private val KOTLIN_BIN = getKotlinHome()
        val KOTLINC_JVM = Paths.get(KOTLIN_BIN, "kotlinc-jvm").toString()
        val KOTLIN = Paths.get(KOTLIN_BIN, "kotlin").toString()
        private val KOTLIN_NATIVE_BIN = (ProductionParams.nativePath?.value() ?: throw NotInitializedOptionException("nativePath")) + "/bin/"
        val KOTLINC_NATIVE = Paths.get(KOTLIN_NATIVE_BIN, "kotlinc-native").toString()


        fun getRoot() = Paths.get(ProductionParams.testbaseDir?.value() ?: throw NotInitializedOptionException("testbaseDir"))

        fun writeFile(target: Path, fileName: String, content: String) {
            var file: FileWriter? = null
            try {
                file = FileWriter(target.resolve(fileName).toFile())
                file.write(content)
            } catch (ex: IOException) {
                ex.printStackTrace()
            } finally {
                file?.close()
            }
        }

        fun ensureExisting(path: Path) {
            if (Files.notExists(path)) {
                try {
                    Files.createDirectories(path)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }

        fun runProcess(pb: ProcessBuilder, name: String): Int {
            pb.redirectError(File("$name.err"))
            pb.redirectOutput(File("$name.out"))
            val process = pb.start()
            return if (process.waitFor(MINUTES_TO_WAIT, TimeUnit.MINUTES)) {
                var file: FileWriter? = null
                try {
                    file = FileWriter("$name.exit")
                    file.write(process.exitValue().toString())
                } finally {
                    file?.close()
                }
                process.exitValue()
            } else {
                process.destroyForcibly()
                -1
            }
        }

        private fun getKotlinHome(): String {
            val env = arrayOf("KOTLIN_HOME", "JDK_HOME", "BOOTDIR")
            for (name in env){
                val path = System.getenv(name)
                if (path != null && !path.isEmpty()) {
                    return "$path/bin/"
                }
            }
            return ""
        }
    }

    fun compilePrinterJVM() {
        val root = getRoot()
        val pb = ProcessBuilder(KOTLINC_JVM, "src/main/kotlin/utils/Printer.kt", "-d", generatorDir.toString())
        try {
            val exitCode = runProcess(pb, root.resolve("Printer").toString())
            if (exitCode != 0) {
                throw Error("Printer compilation returned exit code $exitCode")
            }
        } catch (e: IOException) {
            throw Error("Can't compile printer", e)
        } catch (e: InterruptedException) {
            throw Error("Can't compile printer", e)
        }
    }

    fun runProgramJVM(mainName: String) {
        val pb = ProcessBuilder(KOTLIN, "-cp", "$classPath/$mainName:$generatorDir", "${mainName}Kt")
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("runtime"))
            runProcess(pb, generatorDir.resolve(mainName).resolve("runtime").resolve(mainName).toString())
        } catch (ex: InterruptedException) {
            throw Error("Can't run generated program ", ex)
        } catch (ex: IOException) {
            throw Error("Can't run generated program ", ex)
        }
    }

    fun runProgramNative(mainName: String) {
        val pb = ProcessBuilder(generatorDir.resolve(mainName).resolve("$mainName.kexe").toString())
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("runtime"))
            runProcess(pb, generatorDir.resolve(mainName).resolve("runtime").resolve(mainName).toString())
        } catch (ex: InterruptedException) {
            throw Error("Can't run generated program ", ex)
        } catch (ex: IOException) {
            throw Error("Can't run generated program ", ex)
        }
    }
}