package providers

import providers.tests_generators.KotlinCodeGenerator
import providers.tests_generators.TestGenerator

class TestGeneratorsFactory: (List<String>) -> List<TestGenerator> {
    override fun invoke(names: List<String>): List<TestGenerator> {
        val result = mutableListOf<TestGenerator>()
        for (name in names) {
            when (name) {
                "KotlinCode" -> result.add(KotlinCodeGenerator())
                else -> throw IllegalArgumentException("Unknown generator: $name")
            }
        }
        return result
    }
}