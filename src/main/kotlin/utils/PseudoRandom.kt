package utils

import java.lang.reflect.Field
import java.util.*

object PseudoRandom {
    private lateinit var random: Random
    private var SEED_FIELD: Field
    var currentSeed: String = ""

    init {
        try {
            SEED_FIELD = Random::class.java.getDeclaredField("seed")
            SEED_FIELD.isAccessible = true
        } catch (ex: ReflectiveOperationException){
            throw Error("Can't get seed field: $ex", ex)
        }
        reset(null)
    }

    fun reset(seed: String?){
        val newSeed = if (seed == null || seed.isEmpty()) System.currentTimeMillis().toString()
        else seed
        currentSeed = newSeed
        random = Random(newSeed.hashCode().toLong())
    }

    fun random() = random.nextDouble()

    // uniformly distributed boolean
    fun randomBoolean() = random.nextBoolean()

    // non-uniformly distributed boolean. 0 probability - never true, 1 - always true
    fun randomBoolean(probability: Double) = random.nextDouble() < probability

    fun randomNotZero(limit: Long): Long {
        val result = (limit * random.nextDouble()).toLong()
        return if (result > 0L) result else 1L
    }

    fun randomNotZero(limit: Int): Int {
        val result = (limit * random.nextDouble()).toInt()
        return if (result > 0) result else 1
    }

    fun shuffle(list: List<*>){
        Collections.shuffle(list, random)
    }

    fun randomNotNegative(limit: Int): Int {
        val result = (limit * random.nextDouble()).toInt()
        return Math.abs(result)
    }

    fun <T> randomElement(collection: Collection<T>): T {
        if (collection.isEmpty())
            throw NoSuchElementException("Empty, no element can be randomly selected")
        return if (collection is List<*>)
            randomElement(collection as List<T>)
        else {
            var ix = random.nextInt(collection.size)
            val iterator = collection.iterator()
            while (ix > 0) {
                ix--
                iterator.next()
            }
            iterator.next()
        }
    }

    fun <T> randomElement(list: List<T>): T {
        if (list.isEmpty())
            throw NoSuchElementException("Empty, no element can be randomly selected")
        return list[random.nextInt(list.size)]
    }

    fun <T> randomElement(array: Array<T>): T {
        if (array.isEmpty())
            throw NoSuchElementException("Empty, no element can be randomly selected")
        return array[random.nextInt(array.size)]
    }
}