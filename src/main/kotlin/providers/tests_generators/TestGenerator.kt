package providers.tests_generators

import MINUTES_TO_WAIT
import exceptions.NotInitializedOptionException
import exceptions.UnsuccessfullRunningException
import ir.IRNode
import utils.ProductionParams
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

abstract class TestGenerator protected constructor(         //TODO: add compiling and running in separate threads for native and jvm
        suffix: String,
        protected val preRunActions: (String) -> Array<String>,
        protected val jtDriverOptions: String
): BiConsumer<IRNode, IRNode?> {
    val generatorDir: Path
    protected val classPath: String
    private var printerTmp: File? = null

    init {
        generatorDir = getRoot().resolve(suffix)
        classPath = getRoot().toString() + File.pathSeparator + generatorDir
    }

    override fun toString(): String {
        return this::class.simpleName ?: this::class.qualifiedName ?: this::class.toString()
    }

    protected constructor(suffix: String): this(suffix, { emptyArray<String>()}, "")

    companion object {
        private val KOTLIN_BIN = getKotlinHome()
        val KOTLINC_JVM = Paths.get(KOTLIN_BIN, "kotlinc-jvm").toString()
        val KOTLIN = Paths.get(KOTLIN_BIN, "kotlin").toString()
        private val KOTLIN_NATIVE_BIN = if (ProductionParams.useNative?.value() == true || ProductionParams.joinTest?.value() == true) {
            ((ProductionParams.nativePath?.value() ?: throw NotInitializedOptionException("nativePath")) + "/bin/")
        } else {
            ""
        }
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

        fun deleteRecursively(candidate: File) {
            if (candidate.exists() && candidate.isDirectory) {
                for (file in candidate.listFiles()) {
                    deleteRecursively(file)
                }
            }
            candidate.delete()
        }

        fun runProcess(pb: ProcessBuilder, name: String): Int {
            pb.redirectError(File("$name.err"))
            pb.redirectOutput(File("$name.out"))
            var process: Process? = null
            try {
                process = pb.start()
                val inTime = process.waitFor(MINUTES_TO_WAIT, TimeUnit.MINUTES)
                return if (inTime) {
                    var file: FileWriter? = null
                    try {
                        file = FileWriter("$name.exit")
                        file.write(process.exitValue().toString())
                    } finally {
                        file?.close()
                    }
                    process.exitValue()
                } else {
                    var file: FileWriter? = null
                    try {
                        file = FileWriter("$name.exit")
                        file.write("interrupted")
                    } finally {
                        file?.close()
                    }
                    -1
                }
            } finally {
                process?.destroyForcibly()
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

    fun extractPrinter(){
        val printerScanner = Scanner(this.javaClass.classLoader.getResourceAsStream("utils/Printer.kt"))
        printerTmp = File("utils/Printer.kt")
        if (printerTmp!!.exists()) deletePrinter()
        ensureExisting(printerTmp!!.toPath().parent)
        printerTmp!!.createNewFile()
        val writer = BufferedWriter(FileWriter(printerTmp!!))
        while (printerScanner.hasNextLine()) {
            val str = printerScanner.nextLine() + "\n"
            writer.write(str)
        }
        writer.close()
    }

    fun deletePrinter() {
        printerTmp?.delete()
        printerTmp?.parentFile?.delete()
    }

    fun compilePrinterJVM() {
        val root = getRoot()
        if (printerTmp == null) throw Error("Printer is not extracted")
        val pb = ProcessBuilder(KOTLINC_JVM, printerTmp!!.absolutePath, "-verbose", "-d", generatorDir.toString())
        try {
            val exitCode = runProcess(pb, root.resolve("Printer").toString())
            if (exitCode != 0) {
                throw Error("Printer compilation returned exit code $exitCode")
            }
        } catch (e: IOException) {
            throw UnsuccessfullRunningException("Can't compile printer", e)
        } catch (e: InterruptedException) {
            throw UnsuccessfullRunningException("Can't compile printer", e)
        }
    }

    fun compilePrinterNative() {    //TODO: refactor
        val root = getRoot()
        if (printerTmp == null) throw Error("Printer is not extracted")
        val pb = ProcessBuilder(KOTLINC_NATIVE, printerTmp!!.absolutePath, "-p", "library", "-o", generatorDir.resolve("Printer").toString())
        try {
            val exitCode = runProcess(pb, root.resolve("Printer").toString())
            if (exitCode != 0) {
                throw Error("Printer compilation returned exit code $exitCode")
            }
        } catch (e: IOException) {
            throw UnsuccessfullRunningException("Can't compile printer", e)
        } catch (e: InterruptedException) {
            throw UnsuccessfullRunningException("Can't compile printer", e)
        }
    }

    fun runProgramJVM(mainName: String) {
        val pb = ProcessBuilder(KOTLIN, "-cp", "$classPath/$mainName:$generatorDir", "${mainName}Kt")
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("runtime").resolve("jvm"))
            runProcess(pb, generatorDir.resolve(mainName).resolve("runtime").resolve("jvm").resolve(mainName).toString())
        } catch (ex: InterruptedException) {
            throw UnsuccessfullRunningException("Can't run generated program ", ex)
        } catch (ex: IOException) {
            throw UnsuccessfullRunningException("Can't run generated program ", ex)
        }
    }

    fun runProgramNative(mainName: String) {
        val pb = ProcessBuilder(generatorDir.resolve(mainName).resolve("$mainName.kexe").toString())
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("runtime").resolve("native"))
            runProcess(pb, generatorDir.resolve(mainName).resolve("runtime").resolve("native").resolve(mainName).toString())
        } catch (ex: InterruptedException) {
            throw UnsuccessfullRunningException("Can't run generated program ", ex)
        } catch (ex: IOException) {
            throw UnsuccessfullRunningException("Can't run generated program ", ex)
        }
    }
}