package IR

class Statement(body: IRNode): IRNode(body.getResultType()) {
    init {
        addChild(body)
    }

    override fun complexity() = getChild(0).complexity()
}