package providers

import providers.tests_generators.KotlinCodeGenerator
import providers.tests_generators.TestsGenerator

class TestGeneratorsFactory: (List<String>) -> List<TestsGenerator> {
    override fun invoke(names: List<String>): List<TestsGenerator> {
        val result = mutableListOf<TestsGenerator>()
        for (name in names) {
            when (name) {
                "KotlinCode" -> result.add(KotlinCodeGenerator())
                else -> throw IllegalArgumentException("Unknown generator: $name")
            }
        }
        return result
    }
}