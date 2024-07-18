package com.example.myapplication.pages

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.features.bitmaps.BitmapLoaderVm
import androidx.compose.ui.zIndex
import com.example.myapplication.features.bitmaps.BitmapUIEvent
import com.example.myapplication.features.bitmaps.BitmapUiState
import com.example.myapplication.ui.components.CustomShaderBackground
import com.example.myapplication.ui.components.ShaderEffect
import com.example.myapplication.ui.theme.AppTheme

enum class BoxState {
    Collapsed,
    Preview,
    Expanded
}
data class ShaderArgs(
    val shaderEffect: ShaderEffect = ShaderEffect.RIPPLE,
    val timeMultiplier: Float = 0.5f,
    val rippleMultiplier: Float = 0.5f,
    val zoomMultiplier: Float = 0.5f,
    val dirMultiplier: Float = 1f,
)
data class BitmapPreview(val bitmap: Bitmap? = null)

sealed interface ActivityEvent {
    data object UploadPicture : ActivityEvent
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePage(
    vm: BitmapLoaderVm,
    bitmapsState: BitmapUiState,
) {
    var showDetailsModal by remember {
        mutableStateOf(BitmapPreview())
    }

    var currentBoxState by remember { mutableStateOf(BoxState.Collapsed) }

    val transition = updateTransition(currentBoxState, label = "box state")

    val heightPct by transition.animateFloat(label = "") {
        when (it) {
            BoxState.Collapsed -> 0f
            BoxState.Preview -> 0.6f
            BoxState.Expanded -> 1f
        }
    }
    val roundedCornersPct by transition.animateFloat(label = "") {
        when (it) {
            BoxState.Collapsed,
            BoxState.Expanded,
            -> 0f

            BoxState.Preview -> 48f
        }
    }
    val titleAlpha by transition.animateFloat(label = "") {
        when (it) {
            BoxState.Collapsed,
            BoxState.Preview,
            -> 1f

            BoxState.Expanded -> 0f
        }
    }
    val bkgAlpha by transition.animateFloat(label = "main content background") {
        when (it) {
            BoxState.Collapsed -> 1f
            BoxState.Preview -> 0.35f
            BoxState.Expanded -> 0f
        }
    }
    var shaderArgs by remember {
        mutableStateOf(ShaderArgs())
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.onEvent(BitmapUIEvent.OpenNativeImageGallery) }
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "\uD83D\uDCF7"
                )
            }
        }
    ) { paddingValues ->
        paddingValues
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ShaderConfigMenu(
                   bitmap =  bitmapsState.bitmaps[10],
                    onShaderArgsUpdated = { shaderArgs = it }
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .alpha(bkgAlpha)
        ) {
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxSize()
            ) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    // apply a gradient fade out of 56.dp with following color: (CardDefaults.elevatedCardColors().containerColor
                    val containerColor = CardDefaults.elevatedCardColors().containerColor

                    val radialFade =
                        Brush.radialGradient(
                            0f to Color.Red, 0.5f to Color.Transparent,
                            radius = LocalConfiguration.current.screenWidthDp.dp.toPx()
                        )

                    val topBottomFade =
                        Brush
                            .verticalGradient(
                                -1f to containerColor,
                                .2f to Color.Transparent,
                            )



                    Box(modifier = Modifier) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .zIndex(2f)
                                .fadingEdge(topBottomFade)
                                .background(containerColor)
                        ) { /* EMPTY CONTENT */ }
                        BitmapLib(
                            modifier = Modifier.zIndex(1f),
                            bitmapsState = bitmapsState,
                            onBitmapSelected = { bitmap, clickType ->
                                showDetailsModal = BitmapPreview(bitmap)
                                currentBoxState = when (clickType) {
                                    ClickType.LongClick -> BoxState.Expanded
                                    ClickType.ShortClick -> BoxState.Preview
                                }
                            }
                        )
                    }
                }
            }
        }

        val screenHeight = LocalConfiguration.current.screenHeightDp
        AnimatedVisibility(
            visible = showDetailsModal.bitmap != null,
            enter = slideInVertically(
                initialOffsetY = { screenHeight * 2 },
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { screenHeight * 2 },
            ) + fadeOut(),
        ) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxSize()
            ) {
                ModalBox(
                    cornersPct = roundedCornersPct,
                    heightPct = heightPct,
                    modifier = Modifier
                        .animateContentSize()
                        .combinedClickable(
                            onClick = {
                                currentBoxState = when (currentBoxState) {
                                    BoxState.Preview,
                                    BoxState.Expanded,
                                    -> BoxState.Collapsed

                                    BoxState.Collapsed -> BoxState.Preview
                                }
                            },
                            onLongClick = {
                                when (currentBoxState) {
                                    BoxState.Expanded -> { /* open editor? */
                                    }

                                    BoxState.Preview -> {
                                        currentBoxState = BoxState.Expanded
                                    }

                                    BoxState.Collapsed -> { /* no op */
                                    }
                                }
                            }
                        )
                ) {
                    CustomShaderBackground(
                        shaderArgs = shaderArgs,
                        modifier = Modifier.fillMaxSize(),
                        bitmapState = bitmapsState,
                        index = bitmapsState.bitmaps.indexOf(showDetailsModal.bitmap)
                    ) { /* Empty Content */ }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShaderConfigMenu(
    bitmap: Bitmap,
    onShaderArgsUpdated: (ShaderArgs) -> Unit,
) {
    var shaderArgs by remember { mutableStateOf(ShaderArgs()) }
    var sliderPositionTime by remember { mutableFloatStateOf(0f) }
    var sliderPositionTime2 by remember { mutableFloatStateOf(0.25f) }
    var sliderPositionTime3 by remember { mutableFloatStateOf(0.5f) }
    var sliderPositionTime4 by remember { mutableFloatStateOf(0.75f) }
    ElevatedCard(
        Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 24.dp
            )
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f)
                .clickable {
                    when (shaderArgs.shaderEffect) {
                        ShaderEffect.RIPPLE -> {
                            shaderArgs = shaderArgs.copy(shaderEffect = ShaderEffect.KALEIDOSCOPE)
                            onShaderArgsUpdated(shaderArgs)
                        }

                        ShaderEffect.KALEIDOSCOPE -> {
                            shaderArgs = shaderArgs.copy(shaderEffect = ShaderEffect.RIPPLE)
                            onShaderArgsUpdated(shaderArgs)
                        }
                    }
                }
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .weight(1f)
                ) {
                    Slider(
                        value = sliderPositionTime,
                        onValueChange = {
                            sliderPositionTime = it
                            shaderArgs = shaderArgs.copy(
                                timeMultiplier = it
                            )
                            onShaderArgsUpdated(shaderArgs)
                        },
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .rotate(270f)
                    )
                   Text(
                       text = "⏱\uFE0F",
                       style = MaterialTheme.typography.bodyLarge
                   )
                }
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .weight(1f)
                ) {
                    Slider(
                        value = sliderPositionTime2,
                        onValueChange = {
                            sliderPositionTime2 = it
                            shaderArgs = shaderArgs.copy(
                                rippleMultiplier = it * 100f
                            )
                            onShaderArgsUpdated(shaderArgs)
                        },
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .rotate(270f)
                    )
                    Text(
                        text = "⏱\uFE0F",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .weight(1f)
                ) {
                    Slider(
                        value = sliderPositionTime3,
                        onValueChange = {
                            sliderPositionTime3 = it
                            shaderArgs = shaderArgs.copy(
                                zoomMultiplier = it * 1f
                            )
                            onShaderArgsUpdated(shaderArgs)
                        },
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .rotate(270f)
                    )
                    Text(
                        text = "⏱\uFE0F",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .weight(1f)
                ) {
                    Slider(
                        value = sliderPositionTime4,
                        onValueChange = {
                            sliderPositionTime4 = it
                            shaderArgs = shaderArgs.copy(
                                dirMultiplier = it * 1f
                            )
                            onShaderArgsUpdated(shaderArgs)
                        },
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .rotate(270f)
                    )
                    Text(
                        text = "⏱\uFE0F",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}


@Composable
private fun ModalBox(
    cornersPct: Float = 48f,
    heightPct: Float = 0.6f,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(heightPct)
            .clip(
                RoundedCornerShape(
                    topEnd = cornersPct,
                    topStart = cornersPct,
                )
            )
            .shadow(16.dp)
            .background(MaterialTheme.colorScheme.surface)
            .then(modifier)
    ) {
        content?.invoke()
    }
}

@Preview
@Composable
private fun PreviewModalBox() {
    AppTheme {
        Surface {
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxSize()
            ) {
                ModalBox(heightPct = 0.45f) {
                    Text(
                        text = "Hello",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

enum class ClickType {
    LongClick,
    ShortClick,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BitmapLib(
    modifier: Modifier = Modifier,
    bitmapsState: BitmapUiState,
    onBitmapSelected: (Bitmap, ClickType) -> Unit,
) {
    val bitmaps = bitmapsState.bitmaps
    val totalItems = bitmaps.size
    val isUneven = totalItems % 2 != 0

    val evenIndices = bitmaps.indices.filter { it % 2 == 0 }.take(totalItems / 2)
    val oddIndices = bitmaps.indices.filter { it % 2 != 0 }.take(totalItems / 2)


    val heightVariations = listOf(75, 100, 125, 150, 175, 200, 225)

    // Helper function to generate valid random heights ensuring no two neighboring heights are the same
    fun generateValidHeights(size: Int): List<Int> {
        val heights = mutableListOf<Int>()
        while (heights.size < size) {
            val height = heightVariations.random()
            if (heights.isEmpty() || heights.last() != height) {
                heights.add(height)
            } else {
                var newHeight = heightVariations.random()
                while (newHeight == heights.last()) {
                    newHeight = heightVariations.random()
                }
                heights.add(newHeight)
            }
        }
        return heights
    }

    val evenHeights = generateValidHeights(evenIndices.size)
    // Generate heights for odd items using "spicy two-sum" approach
    val oddHeights = mutableListOf<Int>()
    for (i in evenHeights.indices step 2) {
        if (i + 1 < evenHeights.size) {
            val totalHeight = evenHeights[i] + evenHeights[i + 1]
            var height1 = heightVariations.random()
            var height2 = totalHeight - height1
            while (height2 !in heightVariations || height2 == height1) {
                height1 = heightVariations.random()
                height2 = totalHeight - height1
            }
            oddHeights.add(height1)
            oddHeights.add(height2)
        } else {
            oddHeights.add(evenHeights[i])
        }
    }

    // Ensure no same height for even[0] and odd[0]
    if (oddHeights.isNotEmpty() && evenHeights.isNotEmpty() && oddHeights[0] == evenHeights[0]) {
        val temp = oddHeights[0]
        oddHeights[0] = oddHeights[1]
        oddHeights[1] = temp
    }


    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .height((LocalConfiguration.current.screenHeightDp * 0.6f).dp)
            .verticalScroll(rememberScrollState())
            .then(modifier)
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // 1) EVEN ITEMS
                Column(
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    evenIndices.forEachIndexed { index, evenIndex ->
                        ElevatedCard(
                            Modifier.padding(16.dp)
                        ) {
                            CustomShaderBackground(
                                modifier = Modifier
                                    .height(evenHeights[index].dp)
                                    .combinedClickable(
                                        onLongClick = {
                                            onBitmapSelected(
                                                bitmaps[evenIndex],
                                                ClickType.LongClick
                                            )
                                        },
                                        onClick = {
                                            onBitmapSelected(
                                                bitmaps[evenIndex],
                                                ClickType.ShortClick
                                            )
                                        }
                                    ),
                                bitmapState = bitmapsState,
                                index = evenIndex,
                                shaderArgs = ShaderArgs()
                            ) { /* EMPTY CONTENT */ }
                        }
                    }
                }
                // 2) ODD ITEMS
                Column(
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .fillMaxWidth()
                ) {
                    oddIndices.forEachIndexed { index, oddIndex ->
                        ElevatedCard(Modifier.padding(16.dp)) {
                            CustomShaderBackground(
                                modifier = Modifier
                                    .height(oddHeights[index].dp)
                                    .combinedClickable(
                                        onLongClick = {
                                            onBitmapSelected(
                                                bitmaps[oddIndex],
                                                ClickType.LongClick
                                            )
                                        },
                                        onClick = {
                                            onBitmapSelected(
                                                bitmaps[oddIndex],
                                                ClickType.ShortClick
                                            )
                                        }
                                    ),
                                bitmapState = bitmapsState,
                                index = oddIndex,
                                shaderArgs = ShaderArgs()
                            ) { /* EMPTY CONTENT */ }
                        }
                    }
                }
            }

            // 3) Last item is rendered differently if the sum of bitmaps are uneven
            if (isUneven) {
                val lastIndex = totalItems - 1
                Row {
                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 32.dp
                            )
                            .combinedClickable(
                                onLongClick = {
                                    onBitmapSelected(
                                        bitmaps[lastIndex],
                                        ClickType.LongClick
                                    )
                                },
                                onClick = {
                                    onBitmapSelected(
                                        bitmaps[lastIndex],
                                        ClickType.ShortClick
                                    )
                                }
                            )
                    ) {
                        CustomShaderBackground(
                            shaderArgs = ShaderArgs(),
                            bitmapState = bitmapsState,
                            index = lastIndex,
                        ) { /* EMPTY CONTENT */ }
                    }
                }
            }
        }
    }
}

@Composable
fun Dp.toPx(): Float {
    val density = LocalDensity.current.density
    return remember(this, density) {
        this.value * density
    }
}

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }