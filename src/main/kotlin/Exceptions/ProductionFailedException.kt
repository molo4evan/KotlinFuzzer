package Exceptions

class ProductionFailedException(msg: String?): Exception(msg, null, false, false){
    constructor(): this(null)
}