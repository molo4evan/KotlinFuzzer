package factories

abstract class Factory<out IRNode> {
    abstract fun produce(): IRNode
}