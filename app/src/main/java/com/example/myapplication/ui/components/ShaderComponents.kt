package com.example.myapplication.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.example.myapplication.data.AGSL_KALEIDOSCOPE_EFFECT
import com.example.myapplication.data.AGSL_RIPPLE_EFFECT
import com.example.myapplication.pages.ShaderArgs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RuntimeShaderView(
    modifier: Modifier = Modifier,
    shaderArgs: ShaderArgs = ShaderArgs(),
    content: (@Composable () -> Unit)? = null,
) {
    var floatUniformTime by remember { mutableFloatStateOf(0f) }
    val runtimeShader = GetShader(shader = shaderArgs.shaderEffect)
    LaunchedEffect(key1 = Unit) {
        launch {
            while (true) {
                floatUniformTime += 0.016f
                delay(16)
            }
        }
    }

    Box(
        // contentDescription = "",
        modifier = Modifier
            .then(modifier)
            .zIndex(1f)
            .graphicsLayer {
                runtimeShader.setFloatUniform("iTime", floatUniformTime)
                runtimeShader.setFloatUniform("timeMultiplier", shaderArgs.timeMultiplier)
                runtimeShader.setFloatUniform("rippleMultiplier", shaderArgs.rippleMultiplier)
                runtimeShader.setFloatUniform("zoomMultiplier", shaderArgs.zoomMultiplier)
                runtimeShader.setFloatUniform("dirMultiplier", shaderArgs.dirMultiplier)
                runtimeShader.setFloatUniform(
                    "iSize",
                    this.size.width,
                    this.size.width
                )
                clip = true
                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(
                        runtimeShader,
                        "composable",
                    )
                    .asComposeRenderEffect()
            }
            .background(Color.Transparent)
    ) {
        content?.invoke()
    }
}

@Composable
private fun GetShader(shader: ShaderEffect): RuntimeShader =
    when(shader) {
        ShaderEffect.RIPPLE -> RuntimeShader(AGSL_RIPPLE_EFFECT)
        ShaderEffect.KALEIDOSCOPE -> RuntimeShader(AGSL_KALEIDOSCOPE_EFFECT)
    }


enum class ShaderEffect {
    RIPPLE,
    KALEIDOSCOPE
}
