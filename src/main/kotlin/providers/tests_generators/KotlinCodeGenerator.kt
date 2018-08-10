package providers.tests_generators

import ir.IRNode
import providers.visitors.KotlinCodeVisitor

class KotlinCodeGenerator(
        prefix: String,
        preRunActions: (String) -> Array<String>,
        jtDriverOptions: String
): TestsGenerator(prefix, preRunActions, jtDriverOptions) {
    constructor(): this(DEFAULT_SUFFIX, {arrayOf("@compile $it.java")}, "")  //???  TODO: change


    companion object {
        private val DEFAULT_SUFFIX = "kotlin_tests"
    }

    override fun accept(main: IRNode, other: IRNode?) {
        val mainName = main.getName()
        generateSources(main, other)
        //uncompleted
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
        //ensureExisting(generatorDir)
        writeFile(generatorDir, "$mainName.kt", code.toString())
    }
}