package factories

import exceptions.NotInitializedOptionException
import exceptions.ProductionFailedException
import information.TypeList
import ir.Literal
import ir.types.Type
import utils.ProductionParams
import utils.PseudoRandom

class LiteralFactory(private val resultType: Type): Factory<Literal>() {
    override fun produce(): Literal {
        val literal: Literal

        when(resultType){
            TypeList.BOOLEAN -> literal = Literal(PseudoRandom.randomBoolean(), TypeList.BOOLEAN)
            TypeList.CHAR -> literal = Literal(((PseudoRandom.random() * ('z'.toShort() - 'A'.toShort())) + 'A'.toShort()).toChar(), resultType)   //TODO: min/max values???
            TypeList.BYTE -> literal = Literal(((PseudoRandom.random() * (Byte.MAX_VALUE - Byte.MIN_VALUE)).toByte() + Byte.MIN_VALUE).toByte(), resultType)
            TypeList.SHORT -> literal = Literal(((PseudoRandom.random() * (Short.MAX_VALUE - Short.MIN_VALUE)).toShort() + Short.MIN_VALUE).toShort(), resultType)
            TypeList.INT -> literal = Literal((PseudoRandom.random() * Int.MAX_VALUE).toInt(), resultType)
            TypeList.LONG -> literal = Literal((PseudoRandom.random() * Long.MAX_VALUE).toLong(), resultType)   //TODO: BigDecimal?
            TypeList.FLOAT -> {
                val value = ("%." + (ProductionParams.floatingPointPrecision?.value() ?: 1) + "EF").format(((PseudoRandom.random() * (Float.MAX_VALUE - Float.MIN_VALUE)) + Float.MIN_VALUE).toFloat()).replace(',', '.').toFloat()
                literal = Literal(value, resultType)
            }
            TypeList.DOUBLE -> {
                val value = ("%." + (ProductionParams.floatingPointPrecision?.value() ?: 1) + "E").format(((PseudoRandom.random() * (Double.MAX_VALUE - Double.MIN_VALUE)) + Double.MIN_VALUE)).replace(',', '.').toDouble()
                literal = Literal(value, resultType)
            }
            TypeList.STRING -> {
                val size = (PseudoRandom.random() * (ProductionParams.stringLiteralSizeLimit?.value() ?: throw NotInitializedOptionException("stringLiteralSizeLimit"))).toInt()
                val str = StringBuilder()
                for (i in 0 until size) str.append(((('z' - 'a') * PseudoRandom.random()).toInt() + 'a'.toInt()).toChar())
                literal = Literal(str.toString(), resultType)
            }
            else -> throw ProductionFailedException()
        }

        return literal
    }
}