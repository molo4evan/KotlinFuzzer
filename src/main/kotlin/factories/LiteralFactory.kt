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
            TypeList.CHAR -> literal = Literal(((PseudoRandom.randomDouble() * ('z'.toShort() - 'A'.toShort())) + 'A'.toShort()).toChar(), resultType)   //TODO: min/max values???
            TypeList.BYTE -> literal = Literal(((PseudoRandom.randomDouble() * (Byte.MAX_VALUE - Byte.MIN_VALUE)).toByte() + Byte.MIN_VALUE).toByte(), resultType)
            TypeList.SHORT -> literal = Literal(((PseudoRandom.randomDouble() * (Short.MAX_VALUE - Short.MIN_VALUE)).toShort() + Short.MIN_VALUE).toShort(), resultType)
            TypeList.INT -> literal = Literal((PseudoRandom.randomDouble() * Int.MAX_VALUE).toInt(), resultType)
            TypeList.LONG -> literal = Literal((PseudoRandom.randomDouble() * Long.MAX_VALUE).toLong(), resultType)
            TypeList.FLOAT -> {
                val value = String.format("%.${ProductionParams.floatingPointPrecision?.value() ?: 0}EF", ((PseudoRandom.randomDouble() * (Float.MAX_VALUE - Float.MIN_VALUE)) + Float.MIN_VALUE).toFloat()).toFloat()
                literal = Literal(value, resultType)
            }
            TypeList.DOUBLE -> {
                val value = String.format("%.${ProductionParams.floatingPointPrecision?.value() ?: 0}E", ((PseudoRandom.randomDouble() * (Double.MAX_VALUE - Double.MIN_VALUE)) + Double.MIN_VALUE)).toDouble()
                literal = Literal(value, resultType)
            }
            TypeList.STRING -> {
                val size = (PseudoRandom.randomDouble() * (ProductionParams.stringLiteralSizeLimit?.value() ?: throw NotInitializedOptionException("stringLiteralSizeLimit"))).toInt()
                val str = charArrayOf()
                for (i in 0 until size) str[i] = ((('z' - 'a') * PseudoRandom.randomDouble()).toInt() + 'a'.toInt()).toChar()
                literal = Literal(String(str), resultType)
            }
            else -> throw ProductionFailedException()
        }

        return literal
    }
}