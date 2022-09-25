package com.example.morsetranslate

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import android.widget.Toast
import kotlin.experimental.and
import kotlin.math.abs
import kotlin.math.sin

class MorsePlayer(hertz: Int, speed: Int) {

    private var SAMPLE_RATE = 8000
    private var mDuration : Double = 0.0
    private var mWpmSpeed : Int = 0
    private var mToneHertz : Int = 0
    private var mNumSamples : Int = 0
    private lateinit var mSample : DoubleArray
    private var mDitSnd = arrayOf<Byte>()
    private var mDahSnd = arrayOf<Byte>()
    private var mPauseInnerSnd = arrayOf<Byte>()
    private lateinit var mCurrentMessage : String
    private var mSignaler = AKASignaler()

    init {
        mWpmSpeed = speed
        mToneHertz = hertz
        buildSounds()
    }

    private fun buildSounds(){
        mDuration = ((1200 / mWpmSpeed) * .001)

        mNumSamples = ((mDuration * SAMPLE_RATE - 1).toInt())

        var sineMagnitude = 1.0
        var CUTOFF = 0.1
        var phaseAngle = 2 * Math.PI / (SAMPLE_RATE/mToneHertz)

        while (sineMagnitude > CUTOFF){
            mNumSamples++
            sineMagnitude = abs(sin(phaseAngle*mNumSamples))

        }

        mSample = DoubleArray(mNumSamples)
        //mDitSnd = arrayOf(mDitSnd[2 * mNumSamples])
        //mDahSnd = arrayOf(mDahSnd[6 * mNumSamples])
        //mPauseInnerSnd = arrayOf(mPauseInnerSnd[2 * mNumSamples])
        // mPauseInnerSnd[2 * mNumSamples]

        for (i in 0..mNumSamples){
            mSample[i] = sin(phaseAngle * i)
            Log.d("SAM_POSITION", mSample[i].toString())
        }

        var idx = 0
        for (dVal in mSample){
            val vval = ((dVal * 32767)).toInt().toShort()
            mDitSnd[idx++] = (vval and 0x00ff).toByte()
            // mDitSnd[idx++] = ((vval and 0xff).toByte() shr 8)
        }

        for (i in 0..mNumSamples*6){
            mDahSnd[i] = mDitSnd[i % mDitSnd.size]
        }

        for (i in 0..mNumSamples*2){
            mPauseInnerSnd[i] = 0
        }

    }
    fun playMorse(morseText : String){
        Log.d("MORSE" , "Playing morse code..")

        mCurrentMessage = morseText
        var msgSize = 0
        val characters = mCurrentMessage.trim()

        for (i in characters.indices){
            when{
                characters[i].toString().equals(".") -> msgSize += mPauseInnerSnd.size
                characters[i].toString().equals("-") -> msgSize += mDitSnd.size
                characters[i].toString().equals(" ") -> msgSize += mDahSnd.size
            }
        }

        mSignaler.mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
        AudioFormat.CHANNEL_OUT_MONO,  AudioFormat.ENCODING_PCM_16BIT, msgSize, AudioTrack.MODE_STREAM)

        mSignaler.msgSize = msgSize

        mSignaler.mAudioTrack.play()

        for (i in characters.indices){
            when{
                characters[i].toString().equals("-") -> mSignaler.mAudioTrack.write(mPauseInnerSnd.toByteArray(),0,mPauseInnerSnd.size)
                characters[i].toString().equals(".") -> mSignaler.mAudioTrack.write(mDitSnd.toByteArray(),0,mPauseInnerSnd.size)
                characters[i].toString().equals(" ") -> mSignaler.mAudioTrack.write(mDahSnd.toByteArray(),0,mPauseInnerSnd.size)
            }
        }
        return
    }
}