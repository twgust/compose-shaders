package com.example.myapplication.features.bitmaps

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class BitmapUiState(
    val isLoading: Boolean = true,
    val bitmaps: PersistentList<Bitmap> = persistentListOf(),
)