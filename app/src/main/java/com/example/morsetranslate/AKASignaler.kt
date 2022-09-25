package com.example.morsetranslate

import android.media.AudioTrack
import android.util.Log

class AKASignaler() {
    lateinit var mAudioTrack : AudioTrack
    var msgSize = 0

    fun killAudioTrack(){
        if (mAudioTrack != null){
            Log.d("MORSE", "stopping all sound and releasing audioTrack resources...")
            mAudioTrack.stop()
            mAudioTrack.flush()

            mAudioTrack.release()

        }else{
            Log.d("MORSE", "Null audioTrack, nothing to kill off. What a pity.")
        }
    }
}