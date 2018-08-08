package exceptions

class NotInitializedOptionException(option: String): Exception("Option not initialized: $option")