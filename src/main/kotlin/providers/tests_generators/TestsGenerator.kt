package providers.tests_generators

import exceptions.NotInitializedOptionException
import ir.IRNode
import utils.ProductionParams
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.BiConsumer

abstract class TestsGenerator protected constructor(
        suffix: String,
        protected val preRunActions: (String) -> Array<String>,
        protected val jtDriverOptions: String
): BiConsumer<IRNode, IRNode?> {
    protected val generatorDir: Path

    init {
        generatorDir = getRoot().resolve(suffix)
    }

    protected constructor(suffix: String): this(suffix, { emptyArray<String>()}, "")

    companion object {
//        protected val JAVA_BIN = getJavaPath()
//        protected val JAVAC = Paths.get(JAVA_BIN, "javac").toString()
//        protected val JAVA = Paths.get(JAVA_BIN, "java").toString()

        fun getRoot() = Paths.get(ProductionParams.testbaseDir?.value() ?: throw NotInitializedOptionException("testbaseDir"))

        fun writeFile(target: Path, fileName: String, content: String) {
            var file: FileWriter? = null
            try {
                val fileInstance = target.resolve(fileName).toFile()
                fileInstance.createNewFile()
                file = FileWriter(fileInstance)
                file.write(content)
            } catch (ex: IOException) {
                ex.printStackTrace()
            } finally {
                file?.close()
            }
        }
    }

    //TODO: uncompleted
}