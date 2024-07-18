package com.example.myapplication.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.myapplication.features.bitmaps.BitmapUiState
import com.example.myapplication.pages.ShaderArgs

@Composable
fun CustomShaderCard(
    bitmaps: BitmapUiState,
    index: Int,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)?
) {
    CustomContentCard(modifier) {
        CustomShaderBackground(
            shaderArgs = ShaderArgs(),
            bitmapState = bitmaps,
            index = index,
        ) {
            content?.invoke()
        }
    }
}

@Composable
fun CustomContentCard(
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)?
) {
    ElevatedCard(
        modifier = Modifier.then(modifier)
    ) {
        content?.invoke()
    }
}

@Composable
fun DefaultCustomContentCard(
    modifier: Modifier = Modifier,
    height: Int = 250,
    content: (@Composable () -> Unit)?
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(height.dp)
            .then(modifier)
    ) {
        content?.invoke()
    }
}


@Composable
fun CustomShaderBackground(
    shaderArgs: ShaderArgs,
    modifier: Modifier = Modifier,
    bitmapState: BitmapUiState,
    index: Int,
    content: (@Composable () -> Unit)? = null,
) {
    Log.i("gustavdebug", "$shaderArgs")
    bitmapState.bitmaps[index].let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.then(modifier)
        ) {
            // custom content goes on top of runtimeshader
            Box(modifier =  Modifier.zIndex(1f)) {
                content?.invoke()
            }
            RuntimeShaderView(
                shaderArgs = shaderArgs,
                modifier = Modifier.zIndex(-1f).then(modifier),
            ) {
                Log.w("gustavdebug", "drawing bitmap $index")
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(modifier),
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

@Composable
fun DefaultShaderBackground(
    bitmapState: BitmapUiState,
    index: Int,
    content: (@Composable () -> Unit)? = null,
) {
    bitmapState.bitmaps[index].let {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier =  Modifier.zIndex(1f)) {
                content?.invoke()
            }
            RuntimeShaderView(modifier = Modifier.zIndex(-1f)) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}