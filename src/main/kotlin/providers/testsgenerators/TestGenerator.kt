package providers.testsgenerators

import MINUTES_TO_WAIT
import SECONDS_TO_CLOSE
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
        suffix: String
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


    companion object {
        private val KOTLIN_BIN = getKotlinHome()
        private val JAVA_BIN = getJavaHome()

        val KOTLINC_JVM = Paths.get(KOTLIN_BIN, checkWin("kotlinc-jvm")).toString()
        val JAVA = Paths.get(JAVA_BIN, checkWin("java")).toString()

        private val KOTLIN_NATIVE_BIN = if (ProductionParams.nativeMode?.value() == true || ProductionParams.joinMode?.value() == true) {
            ((ProductionParams.nativePath?.value() ?: throw NotInitializedOptionException("nativePath")) + "/bin/")
        } else {
            ""
        }
        val KOTLINC_NATIVE = Paths.get(KOTLIN_NATIVE_BIN, checkWin("kotlinc-native")).toString()

        val KOTLINC_JS = Paths.get(KOTLIN_BIN, checkWin("kotlinc-js")).toString()
        val KOTLIN_JS = if (ProductionParams.jsMode?.value() == true || ProductionParams.joinMode?.value() == true) {
            ProductionParams.jsPath?.value() ?: throw NotInitializedOptionException("jsPath")
        } else {
            ""
        }

        private fun checkWin(command: String): String {
            val win = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0
            return if (win) "$command.bat" else command
        }


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
                //println("\nProcess ${pb.command()[0]} start work")
                val inTime = process.waitFor(MINUTES_TO_WAIT, TimeUnit.MINUTES)
                return if (inTime) {
                    //println("Process ${pb.command()[0]} end work")
                    var file: FileWriter? = null
                    try {
                        file = FileWriter("$name.exit")
                        file.write(process.exitValue().toString())
                    } finally {
                        file?.close()
                    }
                    process.exitValue()
                } else {
                    process.destroy()
                    val died = process.waitFor(SECONDS_TO_CLOSE, TimeUnit.SECONDS)
                    //println("Process ${pb.command()[0]} stopped")
                    if (!died) {
                        process.destroyForcibly()
                        //println("Process ${pb.command()[0]} stopped forcibly")
                    }
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
                if (process?.isAlive == true) {
                    process.destroyForcibly()
                    //println("Process ${pb.command()[0]} stopped from finally")
                }
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

        private fun getJavaHome(): String {
            val env = arrayOf("JAVA_HOME", "JDK_HOME", "BOOTDIR")
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
        val pb = ProcessBuilder(
                KOTLINC_JVM,
                printerTmp!!.absolutePath,
                "-d",
                generatorDir.toString()
        )
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

    fun compilePrinterNative() {
        val root = getRoot()
        if (printerTmp == null) throw Error("Printer is not extracted")
        val pb = ProcessBuilder(
                KOTLINC_NATIVE,
                printerTmp!!.absolutePath,
                "-p",
                "library",
                "-o",
                generatorDir.resolve("Printer").toString()
        )
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

    fun compilePrinterJS() {
        val root = getRoot()
        if (printerTmp == null) throw Error("Printer is not extracted")
        val pb = ProcessBuilder(
                KOTLINC_JS,
                printerTmp!!.absolutePath,
                "-meta-info",
                "-output",
                generatorDir.resolve("Printer.js").toString()
        )
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
        val pb = ProcessBuilder(
                JAVA,
                "-cp",
                "$classPath/$mainName/$mainName.jar:$generatorDir",
                "${mainName}Kt"
        )
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

    fun runProgramJS(mainName: String) {
        val pb = ProcessBuilder(
                KOTLIN_JS,
                generatorDir.resolve(mainName).resolve("$mainName.js").toString()
        )
        try {
            ensureExisting(generatorDir.resolve(mainName).resolve("runtime").resolve("js"))
            runProcess(pb, generatorDir.resolve(mainName).resolve("runtime").resolve("js").resolve(mainName).toString())
        } catch (ex: InterruptedException) {
            throw UnsuccessfullRunningException("Can't run generated program ", ex)
        } catch (ex: IOException) {
            throw UnsuccessfullRunningException("Can't run generated program ", ex)
        }
    }
}