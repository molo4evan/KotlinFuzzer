package providers

import providers.testsgenerators.KotlinCodeGenerator
import providers.testsgenerators.TestGenerator

class TestGeneratorsFactory: (List<String>) -> List<TestGenerator> {
    override fun invoke(names: List<String>): List<TestGenerator> {
        val result = mutableListOf<TestGenerator>()
        for (name in names) {
            val a: Int = -1286813351 shl 308791256
            when (name) {
                "KotlinCode" -> result.add(KotlinCodeGenerator())
                else -> throw IllegalArgumentException("Unknown generator: $name")
            }
        }
        return result
    }
}