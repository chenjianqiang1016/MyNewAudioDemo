package com.chen.mynewaudiodemo

import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.imageResource

class MainActivity : AppCompatActivity(), MySlideLineView.MySlideLinePercentageListener {

    private val PlayAudioStepCode: Int = 20201022

    private val DelayTime: Long = 1000

    private var mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {

            super.handleMessage(msg)

            when (msg?.what) {

                PlayAudioStepCode -> {

                    if (play_audio_slide_line != null && play_audio_slide_line.getIsNowSlide().not()) {
                        play_audio_slide_line?.setStepMove(
                            isAllowMove = true,
                            isAdd = true,
                            stepNum = 1
                        )
                    }

                }

            }
        }
    }


    private var audioUrl: String = ""

    //音频播放的总时间。单位是毫秒
    private var audioTotalTime: Int = 0

    //秒时间。由音频的duration转换来的
    private var totalSecondTime: Int = 0

    private var currentSecondTime: Int = 0

    //音频是否正在播放
    private var audioIsPlaying: Boolean = true
    //音频是否结束了
    private var audioIsFinish: Boolean = false


    private var isCanClickPlay: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initClickListener()

        audioUrl = "https://...test.mp3"

        play_audio_start_pause_iv?.imageResource = R.mipmap.ic_player_bofang

        audioIsPlaying = false
        audioIsFinish = false

        isCanClickPlay = false

        audioTotalTime = 0

        setViewStatus()

        play_audio_slide_line?.setMySlideLinePercentageListener(this)

        play_audio_title_tv?.postDelayed({

            SoundPlayer.getInstance().play(audioUrl)
            audioTotalTime = SoundPlayer.getInstance().getVoiceDuration()

            audioIsPlaying = true
            isCanClickPlay = true

            setViewStatus()

            durationToSecondTime()

            play_audio_total_time?.text = handleTimeShow(totalSecondTime)
            play_audio_time?.text = handleTimeShow(0)

            //传进去的是 秒
            play_audio_slide_line?.setMaxValue(audioTotalTime / 1000f)

            mHandler.sendEmptyMessageDelayed(PlayAudioStepCode, DelayTime)


        }, 1000)

//        SoundPlayer.getInstance().play(audioUrl)
//
//        audioTotalTime = SoundPlayer.getInstance().getVoiceDuration()
//
//        durationToSecondTime()
//
//        play_audio_total_time?.text = handleTimeShow(totalSecondTime)
//        play_audio_time?.text = handleTimeShow(0)
//
//        Log.e("时长（毫秒）：", "${SoundPlayer.getInstance().getVoiceDuration()}")
//
//        //传进去的是 秒
//        play_audio_slide_line?.setMaxValue(audioTotalTime / 1000f)
//
//
//        mHandler.sendEmptyMessageDelayed(PlayAudioStepCode, DelayTime)


    }

    /**
     * 初始化点击事件
     */
    private fun initClickListener() {

        //播放、暂停按钮
        play_audio_start_pause_iv?.setOnClickListener {

            if (isCanClickPlay.not()) {
                return@setOnClickListener
            }

            if (audioIsPlaying) {
                //音频正在播放，需要暂停

                cancelHandler()
                audioPause()

            } else {
                //音频没有播放，需要开始播放

                if (audioIsFinish) {
                    //音频已经播放完了

                    SoundPlayer.getInstance().stop()

                    currentSecondTime = 0
                    play_audio_time?.text = handleTimeShow(0)

                    play_audio_slide_line?.resetViewStatus()
                    SoundPlayer.getInstance().play(audioUrl)

                } else {
                    //音频没有播放完
                    audioStart()
                }

                startHandler()

            }
            audioIsPlaying = !audioIsPlaying
            setViewStatus()

        }

        //后退5秒
        play_audio_hou_tui?.setOnClickListener {

            if (isCanClickPlay.not()) {
                return@setOnClickListener
            }

            handleHouTuiOrKuaiJin(false)

        }

        //快进5秒
        play_audio_kuai_jin?.setOnClickListener {

            if (isCanClickPlay.not()) {
                return@setOnClickListener
            }

            handleHouTuiOrKuaiJin(true)
        }

    }

    /**
     * 处理后退、快进
     */
    private fun handleHouTuiOrKuaiJin(isKuaiJin: Boolean) {

        if (audioIsFinish) {
            //音频已经播放完了，就不做处理了
            return
        }

        if ((isKuaiJin.not() && currentSecondTime == 0) || (isKuaiJin && currentSecondTime == totalSecondTime)) {
            /**
             * 以下两种情况，都不做处理
             * 1、点击后退，并且现在的时间位置是0
             * 2、点击快进，并且现在的时间位置是最大值
             */
            return
        }

        cancelHandler()

        if (audioIsPlaying.not()) {
            audioStart()
            audioIsPlaying = true
            setViewStatus()
        } else {

        }

        play_audio_slide_line?.setStepMove(
            isAllowMove = true,
            isAdd = isKuaiJin,
            stepNum = 5
        )

    }

    /**
     * 设置控件的状态
     */
    private fun setViewStatus() {

        if (audioIsPlaying) {
            play_audio_start_pause_iv?.imageResource = R.mipmap.ic_player_zant
        } else {
            play_audio_start_pause_iv?.imageResource = R.mipmap.ic_player_bofang
        }

    }

    /**
     * 音频播放结束
     */
    private fun handleAudioFinish() {
        cancelHandler()
        SoundPlayer.getInstance().stop()
        play_audio_slide_line?.setCanSlide(false)
        audioIsPlaying = false
        audioIsFinish = true

        setViewStatus()

        currentSecondTime = totalSecondTime

        play_audio_time?.text = handleTimeShow(totalSecondTime)
    }

    /**
     * 将音频时长转换成秒
     */
    private fun durationToSecondTime() {

        //转换成秒的时间
        var t: Int = audioTotalTime / 1000
        //转换成秒后，多余出来的时间
        val e: Int = audioTotalTime % 1000

        if (e != 0) {
            t++
        }

        totalSecondTime = t
    }

    /**
     * 处理时间的展示
     */
    private fun handleTimeShow(timeValue: Int): String {

        if (timeValue == 0) {
            return "00:00"
        }

        //分
        val m: Int = timeValue / 60
        //秒
        val s: Int = timeValue % 60

        //分钟值转换成的字符串
        val mS: String = if (m >= 10) {
            "$m"
        } else {
            "0$m"
        }

        //秒值转换成的字符串
        val sS: String = if (s >= 10) {
            "$s"
        } else {
            "0$s"
        }

        return "${mS}:${sS}"

    }


    /**
     * 开始
     */
    override fun audioStart() {
        SoundPlayer.getInstance().setStartStatus()
    }

    /**
     * 暂停
     */
    override fun audioPause() {
        SoundPlayer.getInstance().setPauseStatus()
    }

    /**
     * 手指滑动结束后，停留位置的百分比
     *
     * Attempt to perform seekTo in wrong state: mPlayer=0x0, mCurrentState=1
     * 该方法可以只可以在【 Prepared, Paused, Started,PlaybackCompleted】 状态进行调用
     */
    override fun slidePercentageResult(nowPercentage: Float, min: Float, max: Float) {
        if (nowPercentage >= max) {
            //当前的比例，超过了最大值，表示结束了。停止刷新进度、关闭剩声音
            handleAudioFinish()
        } else {
            audioIsFinish = false
            audioIsPlaying = true
            play_audio_slide_line?.setCanSlide(true)
            //还不到结束，取消之前的handler，重新延迟发送
            startHandler()

            val nowPlayTime: Int = (audioTotalTime * nowPercentage).toInt()

            currentSecondTime = (totalSecondTime * nowPercentage).toInt()

            play_audio_time?.text = handleTimeShow(currentSecondTime)

            SoundPlayer.getInstance().setAudioPosition(nowPlayTime)

        }

    }

    /**
     * 跟随音频，自动移动步子
     */
    override fun moveResult(
        nowPercentage: Float,
        isStart: Boolean,
        isEnd: Boolean,
        isAdd: Boolean,
        stepNum: Int
    ) {

        //只要不是结束，就允许触摸滑动
        play_audio_slide_line?.setCanSlide(isEnd.not())

        if (isEnd) {
            handleAudioFinish()
        } else {
            audioIsFinish = false
            audioIsPlaying = true

            if (isAdd) {
                currentSecondTime += stepNum
            } else {
                currentSecondTime -= stepNum
            }

            if (isStart || currentSecondTime <= 0) {
                currentSecondTime = 0
            } else if (isEnd || currentSecondTime >= totalSecondTime) {
                currentSecondTime = totalSecondTime
            }

            play_audio_time?.text = handleTimeShow(currentSecondTime)

            if (stepNum == 5 && nowPercentage != 1f && isEnd.not()) {
                SoundPlayer.getInstance().setAudioPosition((audioTotalTime * nowPercentage).toInt())
            }

            mHandler.sendEmptyMessageDelayed(PlayAudioStepCode, DelayTime)

        }

    }

    private fun startHandler() {
        cancelHandler()
        mHandler.sendEmptyMessageDelayed(PlayAudioStepCode, DelayTime)
    }

    private fun cancelHandler() {
        mHandler.removeMessages(PlayAudioStepCode)
    }

    override fun onPause() {
        super.onPause()

        audioIsPlaying = false
        setViewStatus()
        cancelHandler()
        SoundPlayer.getInstance().setPauseStatus()

    }

    override fun onDestroy() {
        SoundPlayer.getInstance().stop()
        super.onDestroy()
    }

}
