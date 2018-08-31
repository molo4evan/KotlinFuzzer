package ir.operators

import exceptions.ProductionFailedException
import information.TypeList
import ir.IRNode
import providers.visitors.Visitor

class RangeOperator(        //TODO: adding ranges caused hanging...
        target: IRNode,
        from: IRNode,
        to: IRNode,
        step: IRNode,
        val forward: Boolean,
        val inclusive: Boolean,
        val opposite: Boolean
        ): Operator(OperatorKind.RANGE, TypeList.BOOLEAN) {
    init {
        if (!forward && !inclusive) throw ProductionFailedException("Can't create exclusive downTo")
        addChild(target)
        addChild(from)
        addChild(to)
        addChild(step)
    }

    enum class RangeParts {
        TARGET,
        FROM,
        TO,
        STEP
    }

    override fun complexity() = getChild(RangeParts.TARGET.ordinal).complexity() +
            getChild(RangeParts.FROM.ordinal).complexity() +
            getChild(RangeParts.TO.ordinal).complexity() +
            getChild(RangeParts.STEP.ordinal).complexity() + 1

    override fun <T> accept(visitor: Visitor<T>) = visitor.visit(this)
}