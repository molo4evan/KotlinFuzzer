package exceptions

class UnsuccessfullRunningException(msg: String, cause: Exception): Error(msg, cause)