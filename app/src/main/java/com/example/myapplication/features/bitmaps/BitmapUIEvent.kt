package com.example.myapplication.features.bitmaps

import android.net.Uri
import androidx.compose.ui.unit.IntOffset

sealed interface BitmapUIEvent {
    data object OpenNativeImageGallery: BitmapUIEvent
    data class LoadImageFromNativeImgGallery(val uri: Uri): BitmapUIEvent
    data object StopRecording: BitmapUIEvent
    data object StartRecording: BitmapUIEvent
    data class CropByPixelsSelected(val intOffset: IntOffset): BitmapUIEvent
}