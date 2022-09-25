package com.example.morsetranslate

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class MorseWriter(context : Context) {

    val mContext = context
    var mArrayMorseWrite = arrayListOf<Char>()
    var mArraySize = 0
    lateinit var mTxtVTextWrote : TextView

    @SuppressLint("ClickableViewAccessibility")
    fun alertDialogOpen(){
        var builder = AlertDialog.Builder(mContext)
        var view = View.inflate(mContext, R.layout.write_morse_dialog, null)
        var imgVClose = view.findViewById<ImageView>(R.id.imgVCloseMorseDialog)
        mTxtVTextWrote = view.findViewById<TextView>(R.id.txtVMorseWrote)
        var btnWriteMorse = view.findViewById<Button>(R.id.btnWriteMorse)

        builder.setView(view)
        builder.setCancelable(false)
        var dialog = builder.create()
        dialog.show()

        imgVClose.setOnClickListener { dialog.dismiss() }

        var startTime = 0L
        var endTime = 0L

        btnWriteMorse.setOnTouchListener{ v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> startTime = System.currentTimeMillis()
                MotionEvent.ACTION_BUTTON_PRESS -> startTime = System.currentTimeMillis()
                MotionEvent.ACTION_UP ->  endTime = System.currentTimeMillis() - startTime
                MotionEvent.ACTION_CANCEL -> endTime = System.currentTimeMillis() - startTime
            }
            writeMorse(startTime, endTime)

            true
        }

    }

    private fun writeMorse(startTime: Long, endTime: Long) {
        Log.d("WRITE", endTime.toString())

        if (endTime > 0){
            if (endTime in 1..100){
                mArrayMorseWrite.add('.')
            }else if (endTime in 101..600){
                mArrayMorseWrite.add('-')
            }
        }

        mTxtVTextWrote.text = mArrayMorseWrite.toString()
    }



}