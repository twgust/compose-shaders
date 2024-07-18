package com.example.myapplication.features.bitmaps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.BaseViewModel
import com.example.myapplication.MainActivityEvent
import com.example.myapplication.MainActivityEventChannel
import com.example.myapplication.R
import com.example.myapplication.services.ScreenRecordingService
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BitmapLoaderVm(
    val context: Context,
    sessionToken: Int,
) : BaseViewModel<BitmapUiState>(BitmapUiState()) {

    private lateinit var serviceBinder: ScreenRecordingService

    private val _event = MutableLiveData<MainActivityEventChannel<MainActivityEvent>>()
    val event: LiveData<MainActivityEventChannel<MainActivityEvent>> get() = _event

    private val bitmaps get() = run { state.value.bitmaps }

    init {
        viewModelScope.launch {
            addStarterBitmaps(context)
        }
    }

    fun onAppWarmedUp(binder: ScreenRecordingService) {

    }

    fun onEvent(uiEvent: BitmapUIEvent) {
        when (uiEvent) {
            is BitmapUIEvent.CropByPixelsSelected -> { /* TODO */
            }

            is BitmapUIEvent.StartRecording -> attemptStartScreenRecording()
            is BitmapUIEvent.StopRecording -> attemptStopScreenRecording()

            is BitmapUIEvent.OpenNativeImageGallery ->
                viewModelScope.launch {
                    emitActivityEvent(MainActivityEvent.AddUserPicture)
                }

            is BitmapUIEvent.LoadImageFromNativeImgGallery ->
                viewModelScope.launch {
                    addBitmap(uiEvent.uri)
                }
        }
    }

    private fun emitActivityEvent(message: MainActivityEvent) {
        _event.value = MainActivityEventChannel(message)
    }

    private fun attemptStartScreenRecording() {
        Log.w("gustavdebug", "attempting to start screen recording...")
        if (serviceBinder.readyToRecord) {
            Log.i("gustavdebug", "starting screen recording!")
            serviceBinder.startScreenRecording()
        } else {
            Log.e("gustavdebug", "Error: could not start screen recording, bad init")
        }
    }

    private fun attemptStopScreenRecording() {
        Log.i("gustavdebug", "attempting to stop screen recording...")
        if (serviceBinder.isRecording) {
            Log.i("gustavdebug", "stopping screen recording!")
            serviceBinder.stopRecording()
        } else {
            Log.e("gustavdebug", "Error: could not stop screen recording, bad state")
        }
    }

    private fun addStarterBitmaps(context: Context) {
        updateState {
            this.copy(isLoading = true)
        }
        try {
            val mutableList = mutableListOf<Bitmap>().apply {
                addAll(state.value.bitmaps)
            }

            val fields = R.drawable::class.java.fields
            for (field in fields) {
                try {
                    val resourceId = field.getInt(null)
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        inSampleSize = 2
                    }
                    val bitmap = BitmapFactory.decodeResource(
                        context.resources,
                        resourceId,
                        options
                    )
                    mutableList.add(bitmap)
                    Log.i(
                        "gustavdebug",
                        "bitmap size ${bitmap.byteCount} @index:  ${mutableList.indexOf(bitmap)}"
                    )

                } catch (e: Exception) {
                    Log.w("gustavdebug", "Error loading bitmap: $e")
                }
            }

            // Convert to immutable list and emit new state
            val list: PersistentList<Bitmap> = mutableList.toPersistentList()
            updateState {
                this.copy(
                    isLoading = false,
                    bitmaps = list
                )
            }

        } catch (exception: Exception) {
            Log.w("gustavdebug", "addBitmap: $exception ")
        }
    }

    private fun addBitmap(uri: Uri) {
        updateState {
            this.copy(isLoading = true)
        }
        try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inSampleSize = 2
            }
            val bitmap = decodeBitmapFromUri(context, uri, options)
            bitmap?.let {
                val updatedList = mutableListOf<Bitmap>()
                updatedList.addAll(bitmaps)
                updatedList.add(bitmap)
                updateState {
                    this.copy(bitmaps = updatedList.toPersistentList())
                }
            } ?: { /* TODO Error Handling with user feedback */ }
        } catch (exception: Exception) {
            Log.w("gustavdebug", "addBitmap: $exception ")
        }
        updateState {
            this.copy(isLoading = false)
        }
    }

    private fun decodeBitmapFromUri(
        context: Context,
        uri: Uri,
        options: BitmapFactory.Options? = null,
    ): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        fun provideFactory(
            context: Context,
            sessionToken: Int,
            ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                BitmapLoaderVm(context, sessionToken)
            }
        }
    }
}