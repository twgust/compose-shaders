package com.example.myapplication.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingScreen() {
    Box(
    modifier = Modifier
    .fillMaxSize()
    .background(MaterialTheme.colorScheme.surface)
    .clickable { null }
    ) {
        Log.i("gustavdebug", "Loading Screen Active...")
        CircularProgressIndicator()
    }
}
