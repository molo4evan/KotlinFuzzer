package utils

import ir.types.Type
import java.util.stream.Collectors

/**
 * Utility functions for type system
 */
object TypeUtil {
    /**
     * Gets a list of implicitly castable types to a given one from the collection of types
     *
     * @param types a collection to get types from
     * @param type  a target type which result type could be implicitly cast to
     * @return      a result collection of types that match given conditions
     */
    fun getImplicitlyCastable(types: Collection<Type>, type: Type): Collection<Type> {
        return types.stream()
                .filter { t -> t.canImplicitlyCastTo(type) }
                .collect(Collectors.toList())
    }

    /**
     * Gets a list of explicitly castable types to a given one from the collection of types
     *
     * @param types a collection to get types from
     * @param type  a target type which result type could be explicitly cast to
     * @return      a result collection of types that match given conditions
     */
    fun getExplicitlyCastable(types: Collection<Type>, type: Type): Collection<Type> {
        return types.stream()
                .filter { t -> t.canExplicitlyCastTo(type) }
                .collect(Collectors.toList())
    }

    /**
     * Gets a list of more capacious types than a given one from the collection of types
     *
     * @param types a collection to get types from
     * @param type  a type to filter given types by capacity
     * @return      a result collection of types that match given conditions
     */
//    fun getMoreCapaciousThan(types: Collection<Type>, type: Type): List<Type> {
//        return types.stream()
//                .filter { it.isMoreCapaciousThan(type) }
//                .collect(Collectors.toList<Any>())
//    }

    /**
     * Gets a list of less or equal capacious types than a given one from the collection of types
     *
     * @param types a collection to get types from
     * @param type  a type to filter given types by capacity
     * @return      a result collection of types that match given conditions
     */
//    fun getLessCapaciousOrEqualThan(types: Collection<Type>, type: Type): List<Type> {
//        return types.stream()
//                .filter { t -> !t.isMoreCapaciousThan(type) || t == type }
//                .collect(Collectors.toList())
//    }
}
