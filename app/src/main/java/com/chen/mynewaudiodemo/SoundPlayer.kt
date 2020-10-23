package com.chen.mynewaudiodemo

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri


/**
 * 声音播放
 */
class SoundPlayer {

    private var mediaPlayer: MediaPlayer? = null

    private constructor() {
        mediaPlayer = MediaPlayer()
    }

    companion object {

        private var soundPlayer: SoundPlayer? = null
        var currentSysVom = 0
        fun getInstance(): SoundPlayer {
            if (soundPlayer == null) {
                synchronized(SoundPlayer::class.java) {
                    if (soundPlayer == null) {
                        soundPlayer =
                            SoundPlayer()
                    }
                }
            }
            return soundPlayer!!
        }

    }

    //播放
    fun play(path: String) {

        try {

            if (mediaPlayer == null) {
                return
            }

            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }

            mediaPlayer?.reset()

            mediaPlayer?.setDataSource(
                MyApplication.instance().applicationContext,
                Uri.parse(path)
            )

            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer?.setLooping(false) // 不循环播放

            mediaPlayer?.prepare()//准备开始播放 播放的逻辑是c代码在新的线程里面执行。
            mediaPlayer?.start()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getVoiceDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun setAudioPosition(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun setPauseStatus() {
        mediaPlayer?.pause()
    }

    fun setStartStatus() {
        mediaPlayer?.start()
    }

    //停止播放
    fun stop() {
        if (mediaPlayer == null || mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            soundPlayer = null
        }
    }


}