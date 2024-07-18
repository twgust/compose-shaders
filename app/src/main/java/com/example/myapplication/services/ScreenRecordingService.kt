package com.example.myapplication.services

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.myapplication.MainActivity
import com.example.myapplication.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

const val PROCESSED_MOVIE = "PROCESSED_MOVIE"
const val RAW_MOVIE = "RAW_MOVIE"

class ScreenRecordingService : Service() {
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var mediaProjection: MediaProjection
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var virtualDisplay: VirtualDisplay

    private lateinit var rawOutputFile: File
    private lateinit var processedOutputFile: File


    // UI EXPOSED FLAGS
    private val binder = ScreenRecordingBinder()

    private var _isRecording: Boolean = false
    val isRecording get() = _isRecording

    private var _isProcessing: Boolean = false
    val isProcessing get() = _isProcessing

    private var _readyToRecord: Boolean = false
    val readyToRecord: Boolean get() = _readyToRecord

    inner class ScreenRecordingBinder : Binder() {
        fun getService(): ScreenRecordingService = this@ScreenRecordingService
    }

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_OK) ?: Activity.RESULT_OK
        val data: Intent? = intent?.getParcelableExtra("data")

        // Log the resultCode and data to verify values
        Log.d("ScreenRecordingService", "ResultCode: $resultCode")
        Log.d("ScreenRecordingService", "Data: $data")

        if (data != null) {
            startForegroundService() // Start the foreground service before initializing media projection
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            mediaProjection.registerCallback(mediaProjectionCallback, null)
            _readyToRecord = true
        } else {
            Log.e("ScreenRecordingService", "Data is null")
        }

        return START_NOT_STICKY
    }

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            stopRecording()
            Log.d("ScreenRecordingService", "MediaProjection Stopped")
        }
    }

    private fun startForegroundService() {
        val channelId = "ScreenRecordingServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Screen Recording Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Screen Recording")
            .setContentText("Screen recording in progress")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    }

    fun startScreenRecording() {
        if(_isRecording) {
            mediaRecorder.stop()
        }
        Log.w("gusatvdebug", "Starting screen recorder!")
        initRecorder()
        Log.w("gusatvdebug", "Creating virtual display")
        val metrics = application.resources.displayMetrics
        Log.w(TAG, "startScreenRecording: width px ${metrics.widthPixels} height px ${metrics.heightPixels}", )
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels, // - 1000,
            resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder.surface,
            null,
            null
        )
        mediaRecorder.start()
        _isRecording = true
    }

    private fun initRecorder() {
        Log.w("gusatvdebug", "initrecorder!")
        val metrics = application.resources.displayMetrics

        mediaRecorder = MediaRecorder()
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        // Get the directory for public movies directory
        val moviesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Ensure the directory exists
        if (!moviesDir.exists()) {
            moviesDir.mkdirs()
        }

        // Define the output file within the Movies directory
        rawOutputFile = createFile(RAW_MOVIE)
        mediaRecorder.setOutputFile(rawOutputFile)

        mediaRecorder.setVideoSize(metrics.widthPixels, metrics.heightPixels)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024) // Increase the bit rate to 5Mbps
        mediaRecorder.setVideoFrameRate(60) // Set frame rate to 60fps
        try {
            mediaRecorder.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        if (this::mediaRecorder.isInitialized) {
            mediaRecorder.stop()
            mediaRecorder.reset()
        }
        if (this::virtualDisplay.isInitialized) {
            virtualDisplay.release()
        }
        processVideo()
    }

    fun processVideo() {

        val metrics = application.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val cropAmount = 120 // Adjust this value to specify how much you want to crop from both the top and bottom
        val cropHeight = height - 2 * cropAmount

        val processedFile = createFile(PROCESSED_MOVIE)
        // FFmpeg crop command to write to a temporary file and re-encode audio to AAC
        // FFmpeg crop command to write to a temporary file and re-encode both video and audio
        val cropCommand = arrayOf(
            "-i", rawOutputFile.absolutePath,
            "-vf", "crop=$width:$cropHeight:0:$cropAmount",
            "-c:v", "libx264", // Re-encode video to H.264
            "-preset", "fast", // Use a fast preset for encoding
            "-c:a", "aac", // Re-encode audio to AAC
            "-b:a", "128k", // Set audio bitrate
            "-y", // Add this flag to overwrite without asking
            processedFile.absolutePath
        )
        CoroutineScope(Dispatchers.IO).launch {
            val result = FFmpeg.execute(cropCommand)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopRecording()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    private fun createFile(name: String): File {
        // Get the directory for public movies directory
        val parentDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Ensure the directory exists
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        return File(parentDir, "$name.mp4")
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
}
