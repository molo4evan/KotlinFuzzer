package ir.operators

enum class OperatorKind(val priority: Int, val isPrefix: Boolean) {
    /** a += b  */
    COMPOUND_ADD(1),
    /** a -= b  */
    COMPOUND_SUB(1),
    /** a *= b  */
    COMPOUND_MUL(1),
    /** a /= b  */
    COMPOUND_DIV(1),
    /** a %= b  */
    COMPOUND_MOD(1),
    /** a = b  */
    ASSIGN(1),
    /**  a || b  */
    OR(3),
    /** a or b  */
    BIT_OR(5),
    /** a xor b  */
    BIT_XOR(6),
    /** a && b  */
    AND(7),
    /** a & b  */
    BIT_AND(7),
    /** a == b  */
    EQ(8),
    /** a != b  */
    NE(8),
    /** a > b  */
    GT(9),
    /** a < b  */
    LT(9),
    /** a >= b  */
    GE(9),
    /** a <= b  */
    LE(9),
    /** a shr b  */
    SHR(10),
    /** a shl b  */
    SHL(10),
    /** a ushr b  */
    USHR(10),
    /** a + b  */
    ADD(11),
    /** a.toString() + b  */
    STRADD(11),
    /** a - b  */
    SUB(11),
    /** a * b  */
    MUL(12),
    /** a / b  */
    DIV(12),
    /** a % b  */
    MOD(12),
    /** smth as SomeClass */
    CAST(13),
    /** !a  */
    NOT(14),
    /** +a  */
    UNARY_PLUS(14),
    /** -a  */
    UNARY_MINUS(14),
    /** --a  */
    PRE_DEC(15, true),
    /** a--  */
    POST_DEC(15, false),
    /** ++a  */
    PRE_INC(16, true),
    /** a++  */
    POST_INC(16, false);

    constructor(priority: Int): this(priority, true)

    fun isUnary() = this == NOT ||
                        this == UNARY_PLUS ||
                        this == UNARY_MINUS ||
                        this == PRE_INC ||
                        this == POST_INC ||
                        this == PRE_DEC ||
                        this == POST_DEC

    fun isBinary() = !isUnary()
}