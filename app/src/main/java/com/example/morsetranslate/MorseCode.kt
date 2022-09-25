package com.example.morsetranslate

import java.util.*
import kotlin.collections.HashMap

class MorseCode() {

    var mAlpha = arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
        "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v" ,"w" , "x", "y", "z",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
        "!", ",", "?", "\n", "   ", "/", "(", ")", "&", ":", ";", "=", "@")

    var mMorse = arrayOf(".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-",
        ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-" ,".--" , "-..-", "-.--", "--..",
        ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.", "-----",
        "-.-.--", "--..--", "..--..", "\n", "   ", "-..-.", "-.--.", "-.--.-", ".-...", "---...", "-.-.-.", "-...-", ".--.-.")

    private var ALPHA_TO_MORSE = HashMap<String, String>()
    private var MORSE_TO_ALPHA = HashMap<String, String>()

    init {
        for (i in mAlpha.indices){
            ALPHA_TO_MORSE.put(mAlpha[i], mMorse[i])
            MORSE_TO_ALPHA.put(mMorse[i], mAlpha[i])
        }
    }

    public fun morseToAlpha(morseCode : String): String {
        var builder = StringBuilder()
        var words = morseCode.trim().split("   ")

        for (word in words){
            for (letter in word.split(" ")){
                var alpha = MORSE_TO_ALPHA[letter]
                builder.append(alpha)
            }
            builder.append(" ")
        }

        return builder.toString()
    }

    public fun alphaToMorse(text : String) : String{
        var builder = StringBuilder()

        var words = text.trim().split(" ")

        for (word in words){
            for (i in word.indices){
                var morse = ALPHA_TO_MORSE[word.substring(i, i+1).toLowerCase(Locale.ROOT)]
                builder.append(morse).append(" ")
            }
            builder.append("  ")
        }

        return builder.toString()
    }
}
