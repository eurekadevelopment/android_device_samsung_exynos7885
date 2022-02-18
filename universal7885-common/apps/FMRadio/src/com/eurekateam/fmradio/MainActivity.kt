package com.eurekateam.fmradio

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.dlyt.yanndroid.oneui.view.SeekBar
import java.io.*
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(),
    View.OnClickListener, SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener {
    private lateinit var mAudioManager: AudioManager
    private lateinit var mStreamSwitch : Button
    private lateinit var mFMSwitch : Button
    private lateinit var mSeekUpBtn: Button
    private lateinit var mSeekDownBtn : Button
    private lateinit var mBandSelect : TextView
    private var mFMThread: Thread? = null
    private var fd = -1
    private var mCurrentFreq = 0
    private lateinit var mNativeFMInterface: NativeFMInterface
    private lateinit var  tracks : LongArray
    private lateinit var mContext : Context
    private lateinit var mTitle : TextView
    private lateinit var mScanBtn : Button
    private lateinit var mScanStopBtn : Button
    private lateinit var mSeekBar: SeekBar
    private var mCurrentIndex = 0
    private val format: DecimalFormat = DecimalFormat("0.#")
    private var mVolume : Int = 0
    private lateinit var scanThread: Thread
    private var mScanThread: Thread? = null
    private lateinit var mIntent: Intent
    private var mServiceRegistered = false
    private lateinit var mFilePath : File
    private var mLastBackPress: Long = 0L
    private var mScanning = false
    private lateinit var mErrorIntent : Intent
    private var mFreqInitialized = false
    private lateinit var mDebugStringThread: Thread
    private lateinit var mDebugInfo : TextView
    private var mDebugThread : Thread? = null
    private lateinit var mQuitButton: Button
    private var mQuit = false
    private var mHeadSetPlugged : Boolean = false
    private val mMainHandler : Handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("fmradioioctl_jni")
        setContentView(R.layout.activity_main)
        mContext = this
        mFilePath = mContext.filesDir
        mNativeFMInterface = NativeFMInterface()
        mIntent = Intent(mContext, FMRadioService::class.java)
        mErrorIntent = Intent(mContext, ErrorHandler::class.java)
        mErrorIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        mCurrentFreq = mNativeFMInterface.getFMLower(fd)
        mTitle = findViewById(R.id.fm_radio_title)
        mStreamSwitch = findViewById(R.id.fm_output)
        mFMSwitch = findViewById(R.id.fmswitch)
        mSeekBar = findViewById(R.id.fm_freq_seekbar)
        mSeekDownBtn = findViewById(R.id.seek_down)
        mSeekUpBtn = findViewById(R.id.seek_up)
        mBandSelect = findViewById(R.id.band_select)
        mScanBtn = findViewById(R.id.scan_through)
        mScanStopBtn = findViewById(R.id.stop_scan)
        mDebugInfo = findViewById(R.id.debug_info)
        mQuitButton = findViewById(R.id.quit_fm)
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        mStreamSwitch.setOnClickListener(this)
        mFMSwitch.setOnClickListener(this)
        mSeekDownBtn.setOnClickListener(this)
        mSeekUpBtn.setOnClickListener(this)
        mQuitButton.setOnClickListener(this)
        mScanBtn.setOnClickListener(this)
        mScanStopBtn.isEnabled = false
        mStreamSwitch.text = if(!mAudioManager.isSpeakerphoneOn)
            getString(R.string.wired_earphones) else getString(R.string.phone_speaker)
        val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        val mBtFilter = IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(mBtHeadsetReceiver, mBtFilter)
        registerReceiver(mWiredHeadsetReceiver, receiverFilter)
        val mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (i in mAudioDeviceInfo.indices){
            if (mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                mHeadSetPlugged = true
                Log.i(TAG, "onCreate: Wired Headphones detected")
            }
        }
        fd = mNativeFMInterface.openFMDevice()
        if (fd == -1){
            Log.e(TAG, "onCreate: CANNOT OPEN /dev/radio0!!!")
            mErrorIntent.action = RADIO_DEVICE_IO_FAIL
            startActivity(mErrorIntent)
            finish()
        }
        mErrorIntent.putExtra("fd", fd)
        if (!mHeadSetPlugged){
            Toast.makeText(mContext, "Wired Headphones not detected", Toast.LENGTH_SHORT).show()
            Thread{
                Thread.sleep(1500)
                mainExecutor.execute {
                    mErrorIntent.action = NO_WIRED_HEADPHONES
                    mNativeFMInterface.setFMMute(fd, true)
                    startActivity(mErrorIntent)
                    finish()
                }
            }.start()
        }
        mScanStopBtn.setOnClickListener(this)
        if (File(mFilePath.absolutePath + "/fm_radio_volume").exists()) {
            mVolume = if (readFromFile("fm_radio_volume").isEmpty()){
                10
            }else{
                readFromFile("fm_radio_volume").toInt()
            }
        }
        Thread{
            tracks = mNativeFMInterface.getFMTracks(fd)
            tracks = removeZeros(tracks)
            val mPreFreq = if (File(mFilePath.absolutePath + "/fm_radio_freq_current").exists()){
                readFromFile("fm_radio_freq_current")
            }else{
                tracks[0].toString()
            }
            if (DEBUG) Log.i(TAG, "onCreate: file value = $mPreFreq")
            for (i in tracks.indices){
                if (tracks[i] == mPreFreq.toLong()){
                    mNativeFMInterface.setFMFreq(fd,mPreFreq.toInt())
                    mBandSelect.text = String.format(getString(R.string.mhz), mPreFreq.toFloat() / 1000)
                    mCurrentFreq = mPreFreq.toInt()
                    mCurrentIndex = i
                    break
                }
            }
            if (File(mFilePath.absolutePath + "/fm_radio_state").exists()){
                if(readFromFile("fm_radio_state") == "1"){
                    mainExecutor.execute {
                        mFMSwitch.text = getString(R.string.enabled)
                    }
                    mAudioManager.setParameters(FM_RADIO_ON)
                    mNativeFMInterface.setFMBoot(fd)
                    mNativeFMInterface.setFMFreq(fd, mCurrentFreq)
                    mFMThread = Thread {
                        mNativeFMInterface.setFMThread(fd,true)
                    }
                    mFMThread!!.start()
                    mDebugThread = Thread(mDebugStringThread)
                    mDebugThread!!.start()
                }else{
                    mainExecutor.execute {
                        mFMSwitch.text = getString(R.string.disabled)
                    }
                    mFMThread = null
                    mDebugThread = null
                    mAudioManager.setParameters(FM_RADIO_OFF)
                    mNativeFMInterface.setFMThread(fd,false)
                }
            }
            mSeekBar.max = tracks.size - 1
            mSeekBar.min = 0
            mSeekBar.progress = mCurrentIndex
            mSeekBar.setOnSeekBarChangeListener(this)
            mAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            if (DEBUG)
                Log.d(TAG, "onCreate: reading $mFilePath/headphones")
            if (File("$mFilePath/headphones").exists()){
                mAudioManager.isSpeakerphoneOn = false
                mStreamSwitch.text = getString(R.string.wired_earphones)
            }else{
                mAudioManager.isSpeakerphoneOn = true
                mStreamSwitch.text = getString(R.string.phone_speaker)
            }
            mFreqInitialized = true
        }.start()
        mFMThread = Thread {
            mNativeFMInterface.setFMThread(fd,true)
        }
        scanThread = Thread {
            mFreqInitialized = false
            val mOriginalIndex = mCurrentIndex
            mainExecutor.execute {
                mScanStopBtn.isEnabled = true
                mSeekBar.isEnabled = false
            }
            var count = 0
            mNativeFMInterface.setFMMute(fd, false)
            while (count < tracks.size && mScanning) {
                mNativeFMInterface.setFMFreq(fd, tracks[count].toInt())
                mainExecutor.execute {
                    mBandSelect.text = String.format(getString(R.string.mhz), format.format(tracks[count].toFloat() / 1000))
                    mSeekBar.progress = mCurrentIndex
                }
                mCurrentFreq = tracks[count].toInt()
                mCurrentIndex = count
                Thread.sleep(1500)
                count++
            }
            mainExecutor.execute {
                mScanBtn.isEnabled = true
                mScanStopBtn.isEnabled = false
                mSeekBar.isEnabled = true
            }
            if (mScanning)
                mCurrentIndex = mOriginalIndex

            mNativeFMInterface.setFMFreq(fd, tracks[mCurrentIndex].toInt())
            mBandSelect.text = String.format(getString(R.string.mhz), format.format(tracks[mCurrentIndex].toFloat() / 1000))
            mCurrentFreq = tracks[mCurrentIndex].toInt()
            mSeekBar.progress = mCurrentIndex
            mScanning = false
            mFreqInitialized = true
        }
        mAudioManager.requestAudioFocus(mFocusRequest)
        mNativeFMInterface.setFMVolume(fd, mVolume)
        mDebugStringThread = Thread{
            while (true){
                mainExecutor.execute {
                    mDebugInfo.text = String.format(DEBUG_STRING, fd, mNativeFMInterface.getRMSSI(fd))
                }
                Thread.sleep(1000)
            }
        }

    }
    private val mWiredHeadsetReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
                mHeadSetPlugged = false
                val mAudioDeviceInfo = mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (i in mAudioDeviceInfo.indices){
                    if (mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        mAudioDeviceInfo[i].type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES){
                        mHeadSetPlugged = true
                    }
                }
                if (!mHeadSetPlugged){
                    Log.w(TAG, "onReceive: Headset Unplugged")
                    Toast.makeText(mContext, "HeadSet disconnected", Toast.LENGTH_SHORT).show()
                    mErrorIntent.action = NO_WIRED_HEADPHONES
                    unregisterReceiver(this)
                    mNativeFMInterface.setFMMute(fd, true)
                    startActivity(mErrorIntent)
                    finish()
                }
            }
        }
    }
    private val mBtHeadsetReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR)){
                    BluetoothAdapter.STATE_CONNECTED -> {
                        Log.i(TAG, "onReceive: BT Connected, SCO ${mAudioManager.isBluetoothScoOn}")
                        Toast.makeText(mContext, "Warning: FM via BT not supported", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothAdapter.STATE_DISCONNECTED -> {
                        Log.i(TAG, "onReceive: BT Disconnected SCO ${mAudioManager.isBluetoothScoOn}")
                    }
                }
            }
        }
    }
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val mHandler = Handler(Looper.getMainLooper())
        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_UP) {
                    if (mVolume < 15)
                        mVolume += 1
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    mNativeFMInterface.setFMVolume(fd, mVolume)
                    mTitle.text = String.format("Volume %d", mVolume)
                    mHandler.postDelayed({
                        mTitle.text = getString(R.string.fm_radio)
                    },1500)
                }
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_UP) {
                    if (mVolume > 0)
                        mVolume -= 1
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    mNativeFMInterface.setFMVolume(fd, mVolume)
                    mTitle.text = String.format("Volume %d", mVolume)
                    mHandler.postDelayed({
                        mTitle.text = getString(R.string.fm_radio)
                    },1500)
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }
    override fun onClick(v: View) {
        if (v.id == R.id.fmswitch){
            if(mDebugThread == null){
                mAudioManager.setParameters(FM_RADIO_ON)
                mNativeFMInterface.setFMBoot(fd)
                mNativeFMInterface.setFMFreq(fd, mCurrentFreq)
                mFMSwitch.text = getString(R.string.enabled)
                writeToFile("fm_radio_state", "1")
                mFMThread = Thread {
                    mNativeFMInterface.setFMThread(fd,true)
                }
                mFMThread!!.start()
                mDebugThread = Thread(mDebugStringThread)
                mDebugThread!!.start()
            }else{
                mFMThread = null
                mDebugThread = null
                mAudioManager.setParameters(FM_RADIO_OFF)
                mNativeFMInterface.setFMThread(fd,false)
                writeToFile("fm_radio_state", "0")
                mFMSwitch.text = getString(R.string.disabled)
            }
        }else if (v.id == R.id.fm_output) {
            /***
             * Some Notes about ABOX
             * service call media.audio_policy 5 i32 <mode> i32 2
             * when mode 1 -> normal
             * mode 2 -> telephony mode
             * mode 3 -> terminate telephony mode
             * mode 4 -> nothing
             * from 5 -> doesn't exist
             */
            mAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            if (mStreamSwitch.text == getString(R.string.wired_earphones)) {
                Log.i(TAG, "New Output: Speaker")
                mAudioManager.isSpeakerphoneOn = true
                File("$mFilePath/headphones").delete()
                mStreamSwitch.text = getString(R.string.phone_speaker)
            } else {
                Log.i(TAG, "New Output: Headset")
                mAudioManager.isSpeakerphoneOn = false
                File("$mFilePath/headphones").createNewFile()
                mStreamSwitch.text = getString(R.string.wired_earphones)
            }
            mAudioManager.setParameters(FM_RADIO_OFF)
            mAudioManager.setParameters(FM_RADIO_ON)
            mAudioManager.mode = AudioManager.MODE_NORMAL
        }else {
            var mExit = false
            if (!mFreqInitialized && v.id != R.id.stop_scan) {
                Toast.makeText(mContext, "FM Radio is still scanning...", Toast.LENGTH_SHORT).show()
            } else {
                if (tracks.isEmpty()) {
                    Log.e(TAG, "onClick: tracks is empty!!!")
                    mErrorIntent.action = TRACKS_EMPTY
                    startActivity(mErrorIntent)
                }
                mNativeFMInterface.setFMMute(fd, true)
                when (v.id) {
                    R.id.seek_up -> mCurrentIndex += 1
                    R.id.seek_down -> mCurrentIndex -= 1
                    R.id.scan_through -> {
                        mScanning = true
                        mScanBtn.isEnabled = false
                        mScanThread = null
                        mScanThread = Thread(scanThread)
                        mScanThread!!.start()
                        mExit = true
                    }
                    R.id.stop_scan -> {
                        mScanning = false
                        mNativeFMInterface.setFMMute(fd, false)
                        mExit = true
                    }
                    R.id.quit_fm -> {
                        mQuit = true
                        finish()
                    }
                    else -> throw IllegalArgumentException("Unexpected Onclick Event!!")
                }
                if (!mExit)
                    updateFreq()
            }
        }
    }


    private fun updateFreq(){
        if (mCurrentIndex < 0 || mCurrentIndex >= tracks.size) mCurrentIndex = 0
        mCurrentFreq = tracks[mCurrentIndex].toInt()
        writeToFile("fm_radio_freq_current", mCurrentFreq.toString())
        mNativeFMInterface.setFMFreq(fd, mCurrentFreq)
        mainExecutor.execute {
            mBandSelect.text = String.format("%s Mhz", format.format(mCurrentFreq.toFloat() / 1000))
            mSeekBar.progress = mCurrentIndex
        }
        mNativeFMInterface.setFMMute(fd, false)
    }
    companion object {
        // Abox flags
        private const val FM_RADIO_ON = "l_fmradio_mode=on"
        private const val FM_RADIO_OFF = "l_fmradio_mode=off"
        private const val TAG = "FMRadio"

        private const val DEBUG = true

        // Backgroud service intents
        private const val PACKAGENAME = "com.eurekateam.fmradio"
        private const val BEGIN_BG_SERVICE = "$PACKAGENAME.STARTBG"
        private const val STOP_BG_SERVICE = "$PACKAGENAME.STOPBG"
        private const val QUIT = "$PACKAGENAME.QUIT"

        // Failures
        private const val RADIO_DEVICE_IO_FAIL = "$PACKAGENAME.RADIO.IO"
        private const val TRACKS_EMPTY = "$PACKAGENAME.TRACKS"
        private const val NO_WIRED_HEADPHONES = "$PACKAGENAME.NO_HEADPHONE"
        private const val DEBUG_STRING = "fd = %d, rssi = %d"
    }

    private fun removeZeros(array : LongArray): LongArray{
        var count = 0
        if (DEBUG) Log.d(TAG, "removeZeros: Got array")
        for (i in array.indices){
            if(array[i] != 0L){
                count++
            }
        }
        if (DEBUG) Log.d(TAG, "removeZeros: Removed ${array.size - count} of zeros")
        return array.sliceArray(0 until count)
    }

    override fun onBackPressed() {
        if (SystemClock.uptimeMillis() - mLastBackPress in 0..1500){
            super.onBackPressed()
        }else{
            Toast.makeText(mContext, "Press back again to quit", Toast.LENGTH_SHORT).show()
        }
        mLastBackPress = SystemClock.uptimeMillis()
    }
    override fun onPause() {
        super.onPause()
        if (mQuit){
            mNativeFMInterface.closeFMDevice(fd)
            mIntent.action = QUIT
            startService(mIntent)
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
            writeToFile("fm_radio_state","0")
        }else {
            if (readFromFile("fm_radio_state") == "1" && mFreqInitialized) {
                Log.i(TAG, "onPause: Starting background service")
                for (i in tracks.indices){
                    if (tracks[i] == mCurrentFreq.toLong()){
                        mCurrentIndex = i
                    }
                }
                Log.d(TAG, "onPause: params: fd=$fd, " +
                        "tracks.size=${tracks.size}, mCurrentIndex: $mCurrentIndex")
                mIntent.action = BEGIN_BG_SERVICE
                mIntent.putExtra("fd", fd)
                mIntent.putExtra("tracks", tracks)
                mIntent.putExtra("freq",  mCurrentFreq )
                mIntent.putExtra("CurrentIndex",  mCurrentIndex )
                startService(mIntent)
                mServiceRegistered = true
            }
        }
        writeToFile("fm_radio_volume", mVolume.toString())
    }

    private val mFocusRequest: AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
        setAudioAttributes(AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        })
        setAcceptsDelayedFocusGain(true)
        setOnAudioFocusChangeListener(this@MainActivity, mMainHandler)
        build()
    }
    // implementing OnAudioFocusChangeListener to react to focus changes
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (DEBUG)
                    Log.i(TAG, "onAudioFocusChange: AudioFocus Gain")
                mNativeFMInterface.setFMVolume(fd, mVolume)
                mAudioManager.setParameters(FM_RADIO_ON)
                mNativeFMInterface.setFMFreq(fd, mCurrentFreq)
                mFMSwitch.text = getString(R.string.enabled)
                writeToFile("fm_radio_state", "1")
                mFMThread = Thread {
                    mNativeFMInterface.setFMThread(fd,true)
                }
                mFMThread!!.start()
                mDebugThread = Thread(mDebugStringThread)
                mDebugThread!!.start()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (DEBUG)
                    Log.i(TAG, "onAudioFocusChange: AudioFocus Loss")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (DEBUG)
                    Log.i(TAG, "onAudioFocusChange: AudioFocus Loss (Can Duck)")
                mNativeFMInterface.setFMVolume(fd, 2)
            }
        }
    }
    override fun onRestart() {
        super.onRestart()
        if (mServiceRegistered) {
            mAudioManager.requestAudioFocus(mFocusRequest)
            Log.i(TAG, "onRestart: Stopping service")
            mIntent.action = STOP_BG_SERVICE
            startService(mIntent)
            mCurrentFreq = readFromFile("fm_radio_freq_current").toInt()
            if(readFromFile("fm_radio_state") == "1"){
                mFMThread = Thread {
                    mNativeFMInterface.setFMThread(fd,true)
                }
                mFMThread!!.start()
                mDebugThread = null
                mDebugThread = Thread(mDebugStringThread)
                mDebugThread!!.start()
                mFMSwitch.text = getString(R.string.enabled)
            }else{
                mFMThread = null
                mDebugThread = null
                mNativeFMInterface.setFMThread(fd,false)
                mFMSwitch.text = getString(R.string.disabled)
            }
            mServiceRegistered = false
        }
    }

    private fun writeToFile(fileName : String, data: String){
        var os: OutputStream? = null
        val mData = data.padStart(6,'0')
        try {
            os = FileOutputStream(File(mFilePath.absolutePath + "/" + fileName))
            if (DEBUG) Log.i(TAG, "writeToFile: Writing $mData to $fileName")
            os.write(mData.toByteArray(), 0, mData.length)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private fun readFromFile(fileName: String): String{
        var os: InputStream? = null
        val mByteArray = ByteArray(6)
        try {
            os = FileInputStream(File(mFilePath.absolutePath + "/" + fileName))
            if (DEBUG) Log.i(TAG, "readFromFile: Reading from $fileName")
            os.read(mByteArray)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return String(mByteArray).trimStart('0')
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (DEBUG)
            Log.i(TAG, "onProgressChanged: Value changed to $progress, matching FM freq is ${tracks[progress]}")
        mBandSelect.text = String.format(getString(R.string.mhz),
            format.format(tracks[seekBar.progress].toFloat()/1000))
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        mNativeFMInterface.setFMMute(fd, true)
        if (DEBUG)
            Log.i(TAG, "onStartTrackingTouch: user touched.")

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        mCurrentIndex = seekBar.progress
        mCurrentFreq = tracks[seekBar.progress].toInt()
        Log.d(TAG, "onStopTrackingTouch: user stopped touching")
        writeToFile("fm_radio_freq_current", mCurrentFreq.toString())
        mNativeFMInterface.setFMFreq(fd, mCurrentFreq)
        mBandSelect.text = String.format("%s Mhz", format.format(mCurrentFreq.toFloat() / 1000))
        mNativeFMInterface.setFMMute(fd, false)
    }

}
