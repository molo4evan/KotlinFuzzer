package IR

import IR.ControlFlow.If
import IR.ControlFlow.When
import IR.Loops.DoWhile
import IR.Loops.For
import IR.Loops.While
import IR.Types.Type
import Visitors.Visitor
import kotlin.math.max

abstract class IRNode(private val resultType: Type?) {
    var parent: IRNode? = null
        private set

    val children = mutableListOf<IRNode>()

    var owner: Type? = null
        set(ow: Type?){
            field = ow
            if (null === ow){
                println(this::class.qualifiedName.toString() + " null")     // whyyyyy?????
                for (t in Thread.currentThread().stackTrace){
                    println(t)
                }
            }
        }

    private val isCDFDeviation: Boolean by lazy {
        this is If || this is When
                || this is For || this is While
                || this is DoWhile
                || (this is Block && this.parent is Block)
    }

    var level = 0L


    open fun getResultType(): Type? = resultType

    abstract fun <T> accept(visitor: Visitor<T>): T

    fun addChild(child: IRNode){
        children.add(child)
        child.parent = this
    }

     fun <T: IRNode> addChildren(children: List<T>){        // don't sure this is correct
        children.stream().forEach(this::addChild)
    }

    fun getChild(index: Int) = if (index < children.size) children[index] else throw ArrayIndexOutOfBoundsException(index)

    fun setChild(index: Int, child: IRNode){
        children[index] = child
        child.parent = this
    }

    open fun removeSelf() = parent?.children?.remove(this) ?: false

    /*fun resizeUpChildren(size: Int){
        For (i in children.size until size){    // ???
            children.add(null)
        }
    }*/

    open fun complexity() = 0L

    override fun toString() = getName()

    open fun getName() = this::class.qualifiedName.toString()

    open fun countDepth():Long {
        return countDepth(this.children)
    }

    fun getStackableLeaves(): List<IRNode> {
        val result = ArrayList<IRNode>()

        children.stream().forEach {
            if (countDepth() == level && it is Block){
                result.add(it)
            } else {
                result.addAll(it.getStackableLeaves())
            }
        }

        return result
    }

    private fun getDeviantBlocks(depth: Long): List<IRNode> {
        val result = ArrayList<IRNode>()

        children.stream().forEach {
            if (depth == level && isCDFDeviation){
                result.add(it)
            } else {
                result.addAll(getDeviantBlocks(depth))
            }
        }

        return result
    }

    companion object {
        fun countDepth(input: Collection<IRNode>) = input.stream().mapToLong { irNode -> irNode.countDepth() }.max().orElse(0L)

        fun tryToReduceNodesDepth(nodes: List<IRNode>, maxDepth: Long): Boolean {
            var allSucceed = true
            for (child in nodes){
                for (leaf in child.getDeviantBlocks(max(child.countDepth(), maxDepth + 1))){
                    if (child.countDepth() > maxDepth){
                        leaf.removeSelf()
                        val successfull = child.countDepth() > maxDepth
                        allSucceed = allSucceed and successfull
                    } else {
                        break
                    }
                }
            }
            return allSucceed
        }
    }

}