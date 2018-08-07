package Factories

abstract class Factory<out IRNode> {
    abstract fun produce(): IRNode
}