package factories.rules

import exceptions.ProductionFailedException
import factories.Factory
import ir.IRNode
import utils.PseudoRandom

class Rule<T : IRNode>(private val name: String) : Factory<T>(), Comparable<Rule<T>> {
    private val variants: MutableSet<RuleEntry> = mutableSetOf()
    private var limit: Int = -1

    override fun compareTo(other: Rule<T>) =  name.compareTo(other.name)

    fun add(ruleName: String, factory: Factory<T>, weight: Double = 1.0) {
        variants.add(RuleEntry(ruleName, factory, weight))
    }

    fun size() = variants.size

    override fun produce(): T {
        if (!variants.isEmpty()) {
            // Begin production.
            val rulesList = ArrayList(variants)
            PseudoRandom.shuffle(rulesList)

            while (!rulesList.isEmpty() && (limit == -1 || limit > 0)) {
                val sum = rulesList.stream()
                        .mapToDouble { r -> r.weight }
                        .sum()
                val rnd = PseudoRandom.random() * sum
                val iterator = rulesList.iterator()
                var ruleEntry: RuleEntry
                var weightAccumulator = 0.0
                do {
                    ruleEntry = iterator.next()
                    weightAccumulator += ruleEntry.weight
                    if (weightAccumulator >= rnd) {
                        break
                    }
                } while (iterator.hasNext())
                try {
                    return ruleEntry.produce()
                } catch (e: ProductionFailedException) {}

                iterator.remove()
                if (limit != -1) {
                    limit--
                }
            }
            //throw new ProductionFailedException();
        }
        // should probably throw exception here..
        //return getChildren().size() > 0 ? getChild(0).produce() : null;
        throw ProductionFailedException()
    }

    private inner class RuleEntry (
            private val name: String,
            private val factory: Factory<T>,
            internal val weight: Double
    ) : Factory<T>(), Comparable<RuleEntry> {

        override fun produce() = factory.produce()

        override fun compareTo(other: RuleEntry) = name.compareTo(other.name)
    }
}
