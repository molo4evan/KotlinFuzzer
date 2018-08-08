package factories.utils

import exceptions.ProductionFailedException
import utils.ProductionParams

// an utility class to limit steps in the production of an expression
object ProductionLimiter {

    private var limit: Int = -1

    fun setUnlimited() {
        limit = -1
    }

    // initialize limit state
    fun setLimit() {
        limit = ProductionParams.productionLimit?.value() ?: throw Exception("Option productionLimit not initialized")
    }

    // iterate a limit, throwing exception in case it hit
    fun limitProduction() {
        if (limit > 0) {
            limit--
        }
        if (limit != -1 && limit <= 0) {
            throw ProductionFailedException()
        }
    }
}