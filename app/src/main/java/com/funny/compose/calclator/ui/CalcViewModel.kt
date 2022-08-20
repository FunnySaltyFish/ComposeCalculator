package com.funny.compose.calclator.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.jarvisjin.finexpr.expr.ExprException
import io.github.jarvisjin.finexpr.expr.Expression

/**
 * @author  FunnySaltyFish
 * @date    2022/8/11 10:36
 */

class CalcViewModel : ViewModel(){
    var formulaText by mutableStateOf("")
    var resultText by mutableStateOf("")

    companion object {
        private const val TAG = "CalcViewModel"
    }

    fun click(char: Char){
        when(char){
            'C' -> kotlin.run {
                formulaText = ""
                resultText = ""
            }
            '⌫' -> formulaText = formulaText.dropLast(1)
            '=' -> calc()
            else -> formulaText += char
        }
    }

    val histories = mutableStateListOf<Pair<String,String>>()

    private fun calc(){
        if (formulaText.isBlank()) return
        val exp = Expression(formulaText)

        resultText = try {
            exp.calculate().toEngineeringString().also {
                Log.d(TAG, "calc: ${exp.usedFunction}")
                histories.add(formulaText to it)
            }
        }catch (e: ExprException){
            e.localizedMessage ?: "计算错误"
        }catch (e: ArithmeticException){
            e.localizedMessage ?: "计算错误"
        }
    }

    val symbols = arrayOf(
        charArrayOf('C','(',')','/'),
        charArrayOf('7','8','9','*'),
        charArrayOf('4','5','6','-'),
        charArrayOf('1','2','3','+'),
        charArrayOf('⌫','0','.','=')
    )

    val symbolsHorizontal = arrayOf(
        charArrayOf('7','8','9','*','C'),
        charArrayOf('4','5','6','-','⌫'),
        charArrayOf('1','2','3','+','/'),
        charArrayOf('.','0','(',')','=')
    )
}