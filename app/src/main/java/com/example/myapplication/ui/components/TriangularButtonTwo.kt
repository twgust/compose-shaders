package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

data class TrianglePartTwo(val path: Path, val brush: Brush)

@Composable
private fun TriangularButton(
    modifier: Modifier = Modifier,
    side: Dp = 200.dp,
    onRedClick: () -> Unit,
    onWhiteClick: () -> Unit,
    onOrangeClick: () -> Unit
) {
    var redPosition by remember { mutableStateOf(Offset.Zero) }
    var whitePosition by remember { mutableStateOf(Offset.Zero) }

    val heightPx = (with(LocalDensity.current) { side.toPx() } * sqrt(3.0) / 2).toFloat()
    val sideLength = (with(LocalDensity.current) {side.toPx()})
    val redPart = remember {
        TrianglePartTwo(
            path = Path().apply {
                moveTo(sideLength / 2, 0f)
                lineTo(0f, heightPx)
                lineTo(sideLength / 2, heightPx / 2)
                close()
            },
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFEF4444),Color(0xFFC39595), Color(0xFFFCFBFB)),
                start = Offset(0f, heightPx /2),
                end = Offset(0f, sideLength)
            )
        )
    }

    val whitePart = remember {
        TrianglePartTwo(
            path = Path().apply {
                moveTo(sideLength / 2, 0f)
                lineTo(sideLength, heightPx)
                lineTo(sideLength/ 2, heightPx / 2)
                close()
            },
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFF9800), Color(0xFFEBC987),Color(0xFFDEB87E)),
                start = Offset(0f, heightPx /2),
                end = Offset(0f, sideLength / 1.25f)
            )
        )
    }

    val orangePart = remember {
        TrianglePartTwo(
            path = Path().apply {
                moveTo(0f, heightPx)
                lineTo(sideLength, heightPx)
                lineTo(sideLength / 2, heightPx / 2)
                close()
            },
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF090909), Color(0xFF323030), Color(0xFF000000)),
                start = Offset.Infinite,
                end = Offset(0f, heightPx / 2)
            )
        )
    }

    Box(modifier = modifier.size(sideLength.dp)) {
        Part(
            modifier = Modifier
                .size(sideLength.dp)
                .onGloballyPositioned { coordinates ->
                    redPosition = coordinates.positionInParent()
                }
                .clickable(onClick = onRedClick),
            part = redPart
        )
        Part(
            modifier = Modifier
                .size(sideLength.dp)
                .onGloballyPositioned { coordinates ->
                    whitePosition = coordinates.positionInParent()
                }
                .clickable(onClick = onWhiteClick),
            part = whitePart
        )
        Part(
            modifier = Modifier
                .size(sideLength.dp)
                .clickable(onClick = onOrangeClick),
            part = orangePart
        )
    }
}

@Composable
private fun Part(modifier: Modifier, part: TrianglePartTwo) {
    Canvas(modifier = modifier) {
        drawPath(
            path = part.path,
            brush = part.brush
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TriangularButtonTwoPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.size(200.dp)
        ) {
            TriangularButton(
                onRedClick = { /* handle red part click */ },
                onWhiteClick = { /* handle white part click */ },
                onOrangeClick = { /* handle orange part click */ }
            )
        }
    }
}
