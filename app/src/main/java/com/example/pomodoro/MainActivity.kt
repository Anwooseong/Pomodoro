package com.example.pomodoro

import android.annotation.SuppressLint
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val remainMinutesTextView: TextView by lazy {
        findViewById(R.id.remainMinutesTextView)
    }

    private val remainSecondsTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }

    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }

    private val soundPool = SoundPool.Builder().build()  //SoundPool은 바로 생성못하고 Builder패턴으로 되어있음

    private var tickingSoundId : Int? = null
    private var bellSoundId : Int? = null

    private var currentCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initSounds()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()//autoResume를 사용하면 모든 활성화된 streamId를 재시작한다. 그냥 resume사용시 특정 streamId값을 파라미터로 줘서 특정 soundpool만 재시작한다.
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause() //autoPause를 사용하면 모든 활성화된 streamId를 중지한다. 그냥 pause사용시 특정 streamId값을 파라미터로 줘서 특정 soundpool만 중지한다.
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release() //메모리를 많이 잡아먹기 때문에 release를 통해 메모리에 load되었던 것이 해제
    }

    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if(fromUser){
                        updateRemainTime(progress * 60 * 1000L)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {  //새로운 타이머 조작하기 위한 오버라이드
                    stopCountDown()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {  //움직이다가 손을 땔 때 작동
                    seekBar ?: return@onStopTrackingTouch    //Elvis operator
                    if(seekBar.progress==0){
                        stopCountDown()
                    }else{
                        startCountDown()
                    }
                }
            }
        )
    }

    private fun initSounds(){
        tickingSoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)
    }

    private fun createCountDownTimer(initialMillis: Long): CountDownTimer {
        return object : CountDownTimer(initialMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
        }
    }

    private fun startCountDown(){
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)
        currentCountDownTimer?.let{
            it.start()
        }
        //currentCountDownTimer?.start()
        tickingSoundId?.let{
            soundPool.play(it, 1F, 1F, 0, -1, 1F)
        }
    }

    private fun stopCountDown(){
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null //굳이 넣어줄 필요는 없음 없다라는 의미를 위함
        soundPool.autoPause()
    }

    private fun completeCountDown(){
        updateRemainTime(0)
        updateSeekBar(0)
        soundPool.autoPause()
        bellSoundId?.let {
            soundPool.play(it, 1F, 1F, 0 , 0 ,1F)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateRemainTime(remainMillis: Long) {
        val remainSeconds = remainMillis/1000
        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60)
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60)
    }

    private fun updateSeekBar(remainMillis: Long){
        seekBar.progress = (remainMillis/1000/60).toInt()  //Long형태이기 때문에 Int형으로 변환
    }
}