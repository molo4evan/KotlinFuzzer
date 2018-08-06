package IR.Loops

import IR.Block
import IR.IRNode
import IR.Statement
import kotlin.math.max

// header;                       // [subblock]
// statement1, statement2;       // for (statement; condition; statement) {     //TODO: loop.condition == to,
// body1;                        //    [subblock with breaks]
//    mutate(x);
// body2;                        //    [subblock with breaks and continues]
// body3;                        //    [subblock with breaks]
// }

class For(level: Long, private val loop: Loop, private val thisLoopIterLimit: Long,
          header: Block, from: Statement, step: Statement,
          body1: Block, body2: Block, body3: Block
          /*, private val reverse: Boolean = false, private val exclude: Boolean = false*/): IRNode(body1.getResultType()) {       //TODO: Statements??? Settings for looping???

    enum class ForPart{
        HEADER,
        FROM,
        STEP,
        BODY1,
        BODY2,
        BODY3
    }

    init {
        this.level = level
        setChild(ForPart.HEADER.ordinal, header)
        setChild(ForPart.FROM.ordinal, from)
        setChild(ForPart.STEP.ordinal, step)
        setChild(ForPart.BODY1.ordinal, body1)
        setChild(ForPart.BODY2.ordinal, body2)
        setChild(ForPart.BODY3.ordinal, body3)
    }

    override fun complexity(): Long {
        val header = getChild(ForPart.HEADER.ordinal)
        val statement1 = getChild(ForPart.FROM.ordinal)
        val statement2 = getChild(ForPart.STEP.ordinal)
        val body1 = getChild(ForPart.BODY1.ordinal)
        val body2 = getChild(ForPart.BODY2.ordinal)
        val body3 = getChild(ForPart.BODY3.ordinal)

        return loop.initializer.complexity() + header.complexity()+ statement1.complexity() + thisLoopIterLimit *
                (loop.condition.complexity()
                + statement2.complexity()
                + body1.complexity()
                + loop.manipulator.complexity()
                + body2.complexity()
                + body3.complexity())
    }

    override fun countDepth() = max(level, super.countDepth())

    override fun removeSelf(): Boolean {
        val header = children[ForPart.HEADER.ordinal]
        val siblings = parent!!.children
        var index = siblings.indexOf(this)
        siblings[index++] = loop.initializer
        if (header is Block){
            siblings.addAll(index, header.children)
        } else {
            siblings[index] = header
        }
        return true
    }
}