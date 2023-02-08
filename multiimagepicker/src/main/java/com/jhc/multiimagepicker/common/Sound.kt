package com.jhc.multiimagepicker.common

import android.media.AudioAttributes
import android.media.SoundPool

class Sound() {

    private val soundPool = SoundPool.Builder().setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
    ).build()

    private val shutterSound: Int by lazy {
        soundPool.load("/system/media/audio/ui/" + "camera_click.ogg", 1)
    }

    init { shutterSound }

    fun playShutter(){
        soundPool.play(shutterSound, 1F, 1F, 0, 0, 1F)
    }

}