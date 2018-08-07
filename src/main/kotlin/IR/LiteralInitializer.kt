package IR

import IR.Types.Type


class LiteralInitializer(value: Any, type: Type): Literal(value, type) {
    init {
        addChild(type)      //TODO: check if it's needed  (original commentary)
    }
}